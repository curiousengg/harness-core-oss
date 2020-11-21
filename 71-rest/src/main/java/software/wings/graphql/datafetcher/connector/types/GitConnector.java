package software.wings.graphql.datafetcher.connector.types;

import io.harness.exception.InvalidRequestException;
import io.harness.utils.RequestField;

import software.wings.beans.GitConfig;
import software.wings.beans.SettingAttribute;
import software.wings.graphql.datafetcher.connector.ConnectorsController;
import software.wings.graphql.schema.mutation.connector.input.QLConnectorInput;
import software.wings.graphql.schema.mutation.connector.input.QLUpdateConnectorInput;
import software.wings.graphql.schema.mutation.connector.input.git.QLCustomCommitDetailsInput;
import software.wings.graphql.schema.mutation.connector.input.git.QLGitConnectorInput;
import software.wings.graphql.schema.mutation.connector.input.git.QLUpdateGitConnectorInput;
import software.wings.service.intfc.SettingsService;
import software.wings.service.intfc.security.SecretManager;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class GitConnector extends Connector {
  private SecretManager secretManager;
  private SettingsService settingsService;
  private ConnectorsController connectorsController;

  @Override
  public SettingAttribute getSettingAttribute(QLConnectorInput input, String accountId) {
    QLGitConnectorInput gitConnectorInput = input.getGitConnector();
    GitConfig gitConfig = new GitConfig();
    gitConfig.setAccountId(accountId);
    handleSecrets(gitConnectorInput.getPasswordSecretId(), gitConnectorInput.getSshSettingId(), gitConfig);

    if (gitConnectorInput.getUserName().isPresent()) {
      gitConnectorInput.getUserName().getValue().ifPresent(gitConfig::setUsername);
    }
    if (gitConnectorInput.getURL().isPresent()) {
      gitConnectorInput.getURL().getValue().ifPresent(gitConfig::setRepoUrl);
    }
    if (gitConnectorInput.getUrlType().isPresent()) {
      gitConnectorInput.getUrlType().getValue().ifPresent(gitConfig::setUrlType);
    }
    if (gitConnectorInput.getBranch().isPresent()) {
      gitConnectorInput.getBranch().getValue().ifPresent(gitConfig::setBranch);
    }
    if (gitConnectorInput.getGenerateWebhookUrl().isPresent()) {
      gitConnectorInput.getGenerateWebhookUrl().getValue().ifPresent(gitConfig::setGenerateWebhookUrl);
    }
    if (gitConnectorInput.getCustomCommitDetails().isPresent()) {
      gitConnectorInput.getCustomCommitDetails().getValue().ifPresent(
          customCommitDetailsInput -> setCustomCommitDetails(gitConfig, customCommitDetailsInput));
    }

    SettingAttribute.Builder settingAttributeBuilder =
        SettingAttribute.Builder.aSettingAttribute().withValue(gitConfig).withAccountId(accountId).withCategory(
            SettingAttribute.SettingCategory.SETTING);

    if (gitConnectorInput.getName().isPresent()) {
      gitConnectorInput.getName().getValue().ifPresent(settingAttributeBuilder::withName);
    }

    return settingAttributeBuilder.build();
  }

  @Override
  public void updateSettingAttribute(SettingAttribute settingAttribute, QLUpdateConnectorInput input) {
    QLUpdateGitConnectorInput gitConnectorInput = input.getGitConnector();
    GitConfig gitConfig = (GitConfig) settingAttribute.getValue();

    handleSecrets(gitConnectorInput.getPasswordSecretId(), gitConnectorInput.getSshSettingId(), gitConfig);

    if (gitConnectorInput.getUserName().isPresent()) {
      gitConnectorInput.getUserName().getValue().ifPresent(gitConfig::setUsername);
    }
    if (gitConnectorInput.getURL().isPresent()) {
      gitConnectorInput.getURL().getValue().ifPresent(gitConfig::setRepoUrl);
    }
    if (gitConnectorInput.getBranch().isPresent()) {
      gitConnectorInput.getBranch().getValue().ifPresent(gitConfig::setBranch);
    }
    if (gitConnectorInput.getGenerateWebhookUrl().isPresent()) {
      gitConnectorInput.getGenerateWebhookUrl().getValue().ifPresent(gitConfig::setGenerateWebhookUrl);
    }
    if (gitConnectorInput.getCustomCommitDetails().isPresent()) {
      gitConnectorInput.getCustomCommitDetails().getValue().ifPresent(
          customCommitDetailsInput -> setCustomCommitDetails(gitConfig, customCommitDetailsInput));
    }

    settingAttribute.setValue(gitConfig);

    if (gitConnectorInput.getName().isPresent()) {
      gitConnectorInput.getName().getValue().ifPresent(settingAttribute::setName);
    }
  }

  @Override
  public void checkSecrets(QLConnectorInput input, String accountId) {
    boolean passwordSecretIsPresent = false;
    boolean sshSettingIdIsPresent = false;

    QLGitConnectorInput gitConnectorInput = input.getGitConnector();
    RequestField<String> passwordSecretId = gitConnectorInput.getPasswordSecretId();
    RequestField<String> sshSecretId = gitConnectorInput.getSshSettingId();

    if (passwordSecretId.isPresent() && passwordSecretId.getValue().isPresent()) {
      if (!gitConnectorInput.getUserName().isPresent() || !gitConnectorInput.getUserName().getValue().isPresent()) {
        throw new InvalidRequestException("userName should be specified");
      }
      passwordSecretIsPresent = true;
    }
    if (sshSecretId.isPresent()) {
      sshSettingIdIsPresent = sshSecretId.getValue().isPresent();
    }
    if (!passwordSecretIsPresent && !sshSettingIdIsPresent) {
      throw new InvalidRequestException("No secretId provided with the request for connector");
    }
    if (passwordSecretIsPresent && sshSettingIdIsPresent) {
      throw new InvalidRequestException("Just one secretId should be specified");
    }
    if (passwordSecretIsPresent) {
      passwordSecretId.getValue().ifPresent(secretId -> checkSecretExists(secretManager, accountId, secretId));
    }
    if (sshSettingIdIsPresent) {
      sshSecretId.getValue().ifPresent(secretId -> checkSSHSettingExists(settingsService, accountId, secretId));
    }
  }

  @Override
  public void checkSecrets(QLUpdateConnectorInput input, SettingAttribute settingAttribute) {
    QLUpdateGitConnectorInput updateGitConnectorInput = input.getGitConnector();
    boolean passwordSecretIsPresent = false;
    boolean sshSettingIdIsPresent = false;
    if (updateGitConnectorInput.getPasswordSecretId().isPresent()
        && updateGitConnectorInput.getPasswordSecretId().getValue().isPresent()) {
      throwExceptionIfUsernameShouldBeSpecified(updateGitConnectorInput, settingAttribute);
      passwordSecretIsPresent = true;
    }
    if (updateGitConnectorInput.getSshSettingId().isPresent()) {
      sshSettingIdIsPresent = updateGitConnectorInput.getSshSettingId().getValue().isPresent();
    }
    if (passwordSecretIsPresent && sshSettingIdIsPresent) {
      throw new InvalidRequestException("Just one secretId should be specified");
    }
    if (passwordSecretIsPresent) {
      checkSecretExists(secretManager, settingAttribute.getAccountId(),
          updateGitConnectorInput.getPasswordSecretId().getValue().get());
    }
    if (sshSettingIdIsPresent) {
      checkSSHSettingExists(
          settingsService, settingAttribute.getAccountId(), updateGitConnectorInput.getSshSettingId().getValue().get());
    }
  }

  private void throwExceptionIfUsernameShouldBeSpecified(
      QLUpdateGitConnectorInput gitConnectorInput, SettingAttribute settingAttribute) {
    if (null == ((GitConfig) settingAttribute.getValue()).getUsername()) {
      if (!gitConnectorInput.getUserName().isPresent() || !gitConnectorInput.getUserName().getValue().isPresent()) {
        throw new InvalidRequestException("userName should be specified");
      }
    }
  }

  @Override
  public void checkInputExists(QLConnectorInput input) {
    connectorsController.checkInputExists(input.getConnectorType(), input.getGitConnector());
  }

  @Override
  public void checkInputExists(QLUpdateConnectorInput input) {
    connectorsController.checkInputExists(input.getConnectorType(), input.getGitConnector());
  }

  private void setCustomCommitDetails(GitConfig gitConfig, QLCustomCommitDetailsInput customCommitDetailsInput) {
    if (customCommitDetailsInput.getAuthorName().isPresent()) {
      customCommitDetailsInput.getAuthorName().getValue().ifPresent(gitConfig::setAuthorName);
    }
    if (customCommitDetailsInput.getAuthorEmailId().isPresent()) {
      customCommitDetailsInput.getAuthorEmailId().getValue().ifPresent(gitConfig::setAuthorEmailId);
    }
    if (customCommitDetailsInput.getCommitMessage().isPresent()) {
      customCommitDetailsInput.getCommitMessage().getValue().ifPresent(gitConfig::setCommitMessage);
    }
  }

  private void handleSecrets(
      RequestField<String> passwordSecretId, RequestField<String> sshSettingId, GitConfig gitConfig) {
    if (passwordSecretId.isPresent() && passwordSecretId.getValue().isPresent()) {
      gitConfig.setEncryptedPassword(passwordSecretId.getValue().get());
      gitConfig.setKeyAuth(false);
      gitConfig.setSshSettingId(null);
    } else if (sshSettingId.isPresent() && sshSettingId.getValue().isPresent()) {
      gitConfig.setSshSettingId(sshSettingId.getValue().get());
      gitConfig.setKeyAuth(true);
      gitConfig.setEncryptedPassword(null);
    }
  }
}
