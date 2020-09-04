package software.wings.service.impl;

import static io.harness.beans.PageRequest.PageRequestBuilder.aPageRequest;
import static io.harness.beans.SearchFilter.Operator.EQ;
import static io.harness.beans.SearchFilter.Operator.IN;
import static io.harness.data.structure.EmptyPredicate.isEmpty;
import static io.harness.exception.WingsException.USER;
import static io.harness.validation.Validator.notEmptyCheck;
import static io.harness.validation.Validator.notNullCheck;
import static java.util.stream.Collectors.toMap;
import static org.mongodb.morphia.mapping.Mapper.ID_KEY;
import static software.wings.beans.Application.GLOBAL_APP_ID;
import static software.wings.beans.SettingAttribute.SettingCategory.CONNECTOR;
import static software.wings.beans.yaml.YamlConstants.GIT_YAML_LOG_PREFIX;
import static software.wings.settings.SettingVariableTypes.GIT;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.harness.beans.DelegateTask;
import io.harness.beans.PageRequest;
import io.harness.delegate.beans.ErrorNotifyResponseData;
import io.harness.delegate.beans.RemoteMethodReturnValueData;
import io.harness.delegate.beans.ResponseData;
import io.harness.delegate.beans.TaskData;
import io.harness.eraro.ErrorCode;
import io.harness.exception.InvalidRequestException;
import io.harness.exception.WingsException;
import io.harness.security.encryption.EncryptedDataDetail;
import io.harness.tasks.Cd1SetupFields;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.transport.URIish;
import software.wings.beans.FeatureName;
import software.wings.beans.GitConfig;
import software.wings.beans.GitRepositoryInfo;
import software.wings.beans.HostConnectionAttributes;
import software.wings.beans.SettingAttribute;
import software.wings.beans.SettingAttribute.SettingAttributeKeys;
import software.wings.beans.TaskType;
import software.wings.beans.yaml.GitCommand.GitCommandType;
import software.wings.beans.yaml.GitCommandExecutionResponse;
import software.wings.beans.yaml.GitCommandExecutionResponse.GitCommandStatus;
import software.wings.service.intfc.DelegateService;
import software.wings.service.intfc.FeatureFlagService;
import software.wings.service.intfc.SettingsService;
import software.wings.service.intfc.security.ManagerDecryptionService;
import software.wings.service.intfc.security.SecretManager;
import software.wings.sm.ExecutionContext;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Singleton
@Slf4j
public class GitConfigHelperService {
  @Inject private DelegateService delegateService;
  @Inject private SettingsService settingsService;
  @Inject private ManagerDecryptionService managerDecryptionService;
  @Inject private SecretManager secretManager;
  @Inject private SettingValidationService settingValidationService;
  @Inject private FeatureFlagService featureFlagService;

  public void validateGitConfig(GitConfig gitConfig, List<EncryptedDataDetail> encryptionDetails) {
    if (gitConfig.isKeyAuth()) {
      if (gitConfig.getSshSettingId() == null) {
        throw new InvalidRequestException("SSH SettingId can not be empty");
      }

      SettingAttribute sshSettingAttribute =
          settingValidationService.getAndDecryptSettingAttribute(gitConfig.getSshSettingId());
      if (sshSettingAttribute == null) {
        throw new InvalidRequestException("Could not find SettingAttribute for Id: " + gitConfig.getSshSettingId());
      } else {
        gitConfig.setSshSettingAttribute(sshSettingAttribute);
      }
    } else {
      if (gitConfig.getSshSettingId() != null) {
        SettingAttribute sshSettingAttribute = settingsService.get(gitConfig.getSshSettingId());
        if (sshSettingAttribute == null) {
          throw new InvalidRequestException("Could not find SettingAttribute for Id: " + gitConfig.getSshSettingId());
        }
        gitConfig.setSshSettingAttribute(sshSettingAttribute);
      } else if (gitConfig.getUsername() == null
          || (gitConfig.getPassword() == null && gitConfig.getEncryptedPassword() == null)) {
        throw new InvalidRequestException("Username and password can not be empty", USER);
      }
    }

    if (gitConfig.getUrlType() == GitConfig.UrlType.ACCOUNT) {
      if (!featureFlagService.isEnabled(FeatureName.GIT_ACCOUNT_SUPPORT, gitConfig.getAccountId())) {
        throw new InvalidRequestException("Account level git connector is not enabled", USER);
      }

      // Cannot throw exception here as validation is being called at many places and gitConfig.repoName is transient.
      if (isEmpty(gitConfig.getRepoName())) {
        return;
      }
    }

    convertToRepoGitConfig(gitConfig, gitConfig.getRepoName());

    try {
      ResponseData notifyResponseData = delegateService.executeTask(
          DelegateTask.builder()
              .accountId(gitConfig.getAccountId())
              .setupAbstraction(Cd1SetupFields.APP_ID_FIELD, GLOBAL_APP_ID)
              .data(TaskData.builder()
                        .async(false)
                        .taskType(TaskType.GIT_COMMAND.name())
                        .parameters(new Object[] {GitCommandType.VALIDATE, gitConfig, encryptionDetails})
                        .timeout(TimeUnit.SECONDS.toMillis(60))
                        .build())
              .build());

      if (notifyResponseData instanceof ErrorNotifyResponseData) {
        throw new WingsException(((ErrorNotifyResponseData) notifyResponseData).getErrorMessage());
      } else if ((notifyResponseData instanceof RemoteMethodReturnValueData)
          && (((RemoteMethodReturnValueData) notifyResponseData).getException() instanceof InvalidRequestException)) {
        throw(InvalidRequestException)((RemoteMethodReturnValueData) notifyResponseData).getException();
      } else if (!(notifyResponseData instanceof GitCommandExecutionResponse)) {
        throw new WingsException(ErrorCode.GENERAL_ERROR)
            .addParam("message", "Unknown Response from delegate")
            .addContext(ResponseData.class, notifyResponseData);
      }

      GitCommandExecutionResponse gitCommandExecutionResponse = (GitCommandExecutionResponse) notifyResponseData;

      logger.info(GIT_YAML_LOG_PREFIX + "GitConfigValidation [{}]", gitCommandExecutionResponse);

      if (gitCommandExecutionResponse.getGitCommandStatus() == GitCommandStatus.FAILURE) {
        throw new WingsException(ErrorCode.INVALID_CREDENTIAL).addParam("message", "Invalid git credentials.");
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new InvalidRequestException(
          "Thread was interrupted. Please try again. " + e.getMessage(), WingsException.USER);
    }
  }

  /**
   * If GitConfig has keyAuth enabled, and fetch SshKeySettingAttribute using sshSettingId
   * and set it into gitConfig.
   *
   * @param gitConfig
   */
  public void setSshKeySettingAttributeIfNeeded(GitConfig gitConfig) {
    if (gitConfig.isKeyAuth() && StringUtils.isNotBlank(gitConfig.getSshSettingId())) {
      SettingAttribute settingAttribute = settingsService.get(gitConfig.getSshSettingId());
      if (settingAttribute != null && settingAttribute.getValue() != null) {
        HostConnectionAttributes attributeValue = (HostConnectionAttributes) settingAttribute.getValue();
        List<EncryptedDataDetail> encryptionDetails =
            secretManager.getEncryptionDetails(attributeValue, GLOBAL_APP_ID, null);
        managerDecryptionService.decrypt(attributeValue, encryptionDetails);
        gitConfig.setSshSettingAttribute(settingAttribute);
      }
    }
  }

  public void renderGitConfig(ExecutionContext context, GitConfig gitConfig) {
    if (context == null) {
      return;
    }

    if (gitConfig.getBranch() != null) {
      gitConfig.setBranch(context.renderExpression(gitConfig.getBranch()).trim());
    }

    if (gitConfig.getReference() != null) {
      gitConfig.setReference(context.renderExpression(gitConfig.getReference().trim()));
    }

    if (gitConfig.getRepoUrl() != null) {
      gitConfig.setRepoUrl(context.renderExpression(gitConfig.getRepoUrl().trim()));
    }
  }

  public Map<String, String> getConnectorIdNameMap(List<String> connectorIds, String accountId) {
    if (isEmpty(connectorIds)) {
      return Collections.emptyMap();
    }
    PageRequest<SettingAttribute> settingAttributeQuery = aPageRequest()
                                                              .addFilter(SettingAttributeKeys.accountId, EQ, accountId)
                                                              .addFilter(SettingAttributeKeys.category, EQ, CONNECTOR)
                                                              .addFilter(SettingAttributeKeys.valueType, EQ, GIT)
                                                              .addFilter(ID_KEY, IN, connectorIds.toArray())
                                                              .build();
    List<SettingAttribute> settingAttributeList = settingsService.list(settingAttributeQuery, null, null);
    if (isEmpty(settingAttributeList)) {
      return Collections.emptyMap();
    }
    return settingAttributeList.stream().collect(toMap(SettingAttribute::getUuid, SettingAttribute::getName));
  }

  public void convertToRepoGitConfig(GitConfig gitConfig, String repoName) {
    notNullCheck("GitConfig provided cannot be null", gitConfig);
    if (GitConfig.UrlType.ACCOUNT == gitConfig.getUrlType()) {
      String repositoryUrl = getRepositoryUrl(gitConfig, repoName);
      gitConfig.setRepoUrl(repositoryUrl);
    }

    gitConfig.setRepoName(repoName);
    gitConfig.setUrlType(GitConfig.UrlType.REPO);
  }

  private String getRepositoryUrl(GitConfig gitConfig, String repoName) {
    notNullCheck("GitConfig provided cannot be null", gitConfig);
    if (GitConfig.UrlType.ACCOUNT == gitConfig.getUrlType()) {
      notEmptyCheck("Repo name cannot be null for Account level git connector", repoName);
      String purgedRepoUrl = gitConfig.getRepoUrl().replaceAll("/*$", "");
      String purgedRepoName = repoName.replaceAll("^/*", "");
      return purgedRepoUrl + "/" + purgedRepoName;
    }
    return gitConfig.getRepoUrl();
  }

  private String getDisplayRepositoryUrl(String repositoryUrl) {
    try {
      URIish uri = new URIish(repositoryUrl);
      String path = uri.getPath();
      path = StringUtils.removeEnd(path, "/");
      path = StringUtils.removeEnd(path, ".git");
      return StringUtils.removeStart(path, "/");
    } catch (URISyntaxException e) {
      logger.error("Failed to generate Display Repository Url {}", repositoryUrl, e);
    }
    return repositoryUrl;
  }

  private GitRepositoryInfo.GitProvider getGitProvider(String repositoryUrl) {
    try {
      URIish uri = new URIish(repositoryUrl);
      String host = uri.getHost();
      if (null != host) {
        Optional<GitRepositoryInfo.GitProvider> provider =
            Arrays.stream(GitRepositoryInfo.GitProvider.values())
                .filter(
                    p -> p != GitRepositoryInfo.GitProvider.UNKNOWN && StringUtils.containsIgnoreCase(host, p.name()))
                .findFirst();
        if (provider.isPresent()) {
          return provider.get();
        }
      }
    } catch (Exception e) {
      logger.error("Failed to generate Git Provider Repository Url {}", repositoryUrl, e);
    }
    return GitRepositoryInfo.GitProvider.UNKNOWN;
  }

  public GitRepositoryInfo createRepositoryInfo(GitConfig gitConfig, String repositoryName) {
    String repositoryUrl = getRepositoryUrl(gitConfig, repositoryName);
    String displayUrl = getDisplayRepositoryUrl(repositoryUrl);
    GitRepositoryInfo.GitProvider provider = getGitProvider(repositoryUrl);

    return GitRepositoryInfo.builder().url(repositoryUrl).displayUrl(displayUrl).provider(provider).build();
  }
}
