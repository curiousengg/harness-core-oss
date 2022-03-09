package io.harness.ng.validator.service;

import static io.harness.connector.HostValidationResult.HostValidationStatus.FAILED;
import static io.harness.data.structure.EmptyPredicate.isNotEmpty;
import static io.harness.delegate.beans.NgSetupFields.NG;
import static io.harness.delegate.beans.NgSetupFields.OWNER;
import static io.harness.delegate.task.utils.PhysicalDataCenterConstants.DEFAULT_HOST_VALIDATION_FAILED_MSG;
import static io.harness.delegate.task.utils.PhysicalDataCenterConstants.HOSTS_NUMBER_VALIDATION_LIMIT;
import static io.harness.delegate.task.utils.PhysicalDataCenterConstants.TRUE_STR;
import static io.harness.exception.WingsException.USER;
import static io.harness.exception.WingsException.USER_SRE;

import static org.apache.commons.lang3.StringUtils.isBlank;

import io.harness.beans.DelegateTaskRequest;
import io.harness.connector.HostValidationResult;
import io.harness.delegate.beans.DelegateResponseData;
import io.harness.delegate.beans.ErrorNotifyResponseData;
import io.harness.delegate.beans.RemoteMethodReturnValueData;
import io.harness.delegate.beans.SSHTaskParams;
import io.harness.delegate.beans.secrets.SSHConfigValidationTaskResponse;
import io.harness.delegate.task.utils.PhysicalDataCenterConstants;
import io.harness.delegate.utils.TaskSetupAbstractionHelper;
import io.harness.eraro.ErrorCode;
import io.harness.exception.InvalidArgumentsException;
import io.harness.exception.InvalidRequestException;
import io.harness.exception.ngexception.ConnectorValidationException;
import io.harness.manage.ManagedExecutorService;
import io.harness.ng.core.BaseNGAccess;
import io.harness.ng.core.api.NGSecretServiceV2;
import io.harness.ng.core.dto.ErrorDetail;
import io.harness.ng.core.dto.secrets.SSHKeySpecDTO;
import io.harness.ng.core.models.Secret;
import io.harness.ng.validator.HostValidationService;
import io.harness.pms.utils.CompletableFutures;
import io.harness.secretmanagerclient.SecretType;
import io.harness.secretmanagerclient.services.SshKeySpecDTOHelper;
import io.harness.security.encryption.EncryptedDataDetail;
import io.harness.service.DelegateGrpcClientWrapper;

import software.wings.beans.TaskType;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Singleton
@Slf4j
public class HostValidationServiceImpl implements HostValidationService {
  @Inject private SshKeySpecDTOHelper sshKeySpecDTOHelper;
  @Inject private NGSecretServiceV2 ngSecretServiceV2;
  @Inject private TaskSetupAbstractionHelper taskSetupAbstractionHelper;
  @Inject private DelegateGrpcClientWrapper delegateGrpcClientWrapper;
  private final Executor executor = new ManagedExecutorService(Executors.newFixedThreadPool(4));

  @Override
  public List<HostValidationResult> validateSSHHosts(@NotNull List<String> hostNames,
      @Nullable String accountIdentifier, @Nullable String orgIdentifier, @Nullable String projectIdentifier,
      @NotNull String secretIdentifier) {
    if (hostNames.isEmpty()) {
      return Collections.emptyList();
    }
    if (isBlank(secretIdentifier)) {
      throw new InvalidArgumentsException("Secret identifier cannot be null or empty", USER_SRE);
    }

    log.info("Start validation hosts: {}", StringUtils.join(hostNames));
    CompletableFutures<HostValidationResult> validateHostTasks = new CompletableFutures<>(executor);
    for (String host : limitHosts(hostNames)) {
      validateHostTasks.supplyAsyncExceptionally(
          ()
              -> validateSSHHost(host, accountIdentifier, orgIdentifier, projectIdentifier, secretIdentifier),
          ex
          -> HostValidationResult.builder()
                 .host(host)
                 .status(FAILED)
                 .error(buildErrorDetailsWithMsg(ex.getMessage()))
                 .build());
    }

    CompletableFuture<List<HostValidationResult>> hostValidationResults = validateHostTasks.allOf();
    try {
      return new ArrayList<>(hostValidationResults.get());
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new InvalidRequestException(ex.getMessage(), USER);
    } catch (ExecutionException ex) {
      throw new InvalidRequestException(ex.getMessage(), USER);
    }
  }

  @Override
  public HostValidationResult validateSSHHost(@NotNull String hostName, String accountIdentifier,
      @Nullable String orgIdentifier, @Nullable String projectIdentifier, @NotNull String secretIdentifier) {
    if (isBlank(hostName)) {
      throw new InvalidArgumentsException("SSH host name cannot be null or empty", USER_SRE);
    }
    if (isBlank(secretIdentifier)) {
      throw new InvalidArgumentsException("Secret identifier cannot be null or empty", USER_SRE);
    }

    Optional<Secret> secretOptional =
        ngSecretServiceV2.get(accountIdentifier, orgIdentifier, projectIdentifier, secretIdentifier);
    if (!secretOptional.isPresent()) {
      throw new InvalidArgumentsException(
          String.format("No found secret for host validation, secret identifier: %s", secretIdentifier), USER_SRE);
    }
    if (SecretType.SSHKey != secretOptional.get().getType()) {
      throw new InvalidArgumentsException(
          String.format("Secret is not SSH type, secret identifier: %s", secretIdentifier), USER_SRE);
    }

    SSHKeySpecDTO secretSpecDTO = (SSHKeySpecDTO) secretOptional.get().getSecretSpec().toDTO();
    BaseNGAccess baseNGAccess = getBaseNGAccess(accountIdentifier, orgIdentifier, projectIdentifier);
    List<EncryptedDataDetail> encryptionDetails =
        sshKeySpecDTOHelper.getSSHKeyEncryptionDetails(secretSpecDTO, baseNGAccess);

    DelegateTaskRequest delegateTaskRequest =
        DelegateTaskRequest.builder()
            .accountId(accountIdentifier)
            .taskType(TaskType.NG_SSH_VALIDATION.name())
            .taskParameters(SSHTaskParams.builder()
                                .host(hostName)
                                .encryptionDetails(encryptionDetails)
                                .sshKeySpec(secretSpecDTO)
                                .build())
            .taskSetupAbstractions(setupTaskAbstractions(accountIdentifier, orgIdentifier, projectIdentifier))
            .executionTimeout(Duration.ofSeconds(PhysicalDataCenterConstants.EXECUTION_TIMEOUT_IN_SECONDS))
            .build();

    log.info("Start validation host: {}, secret identifier: {}", hostName, secretIdentifier);
    DelegateResponseData delegateResponseData = this.delegateGrpcClientWrapper.executeSyncTask(delegateTaskRequest);

    if (delegateResponseData instanceof SSHConfigValidationTaskResponse) {
      SSHConfigValidationTaskResponse responseData = (SSHConfigValidationTaskResponse) delegateResponseData;
      return HostValidationResult.builder()
          .host(hostName)
          .status(HostValidationResult.HostValidationStatus.fromBoolean(responseData.isConnectionSuccessful()))
          .error(buildErrorDetailsWithMsg(responseData.getErrorMessage()))
          .build();
    }

    return HostValidationResult.builder()
        .host(hostName)
        .status(FAILED)
        .error(buildErrorDetailsWithMsg(getErrorMessageFromDelegateResponseData(delegateResponseData)))
        .build();
  }

  @NotNull
  private List<String> limitHosts(@NotNull List<String> hosts) {
    int numberOfHosts = hosts.size();
    if (numberOfHosts > HOSTS_NUMBER_VALIDATION_LIMIT) {
      log.warn("Limiting validation hosts to {}", HOSTS_NUMBER_VALIDATION_LIMIT);
    }

    return hosts.subList(0, Math.min(HOSTS_NUMBER_VALIDATION_LIMIT, numberOfHosts));
  }

  private BaseNGAccess getBaseNGAccess(
      final String accountIdentifier, final String orgIdentifier, final String projectIdentifier) {
    return BaseNGAccess.builder()
        .accountIdentifier(accountIdentifier)
        .orgIdentifier(orgIdentifier)
        .projectIdentifier(projectIdentifier)
        .build();
  }

  private Map<String, String> setupTaskAbstractions(
      final String accountIdIdentifier, final String orgIdentifier, final String projectIdentifier) {
    Map<String, String> abstractions = new HashMap<>(2);
    final String owner = taskSetupAbstractionHelper.getOwner(accountIdIdentifier, orgIdentifier, projectIdentifier);
    if (isNotEmpty(owner)) {
      abstractions.put(OWNER, owner);
    }
    abstractions.put(NG, TRUE_STR);
    return abstractions;
  }

  private String getErrorMessageFromDelegateResponseData(DelegateResponseData delegateResponseData) {
    if (delegateResponseData instanceof ErrorNotifyResponseData) {
      ErrorNotifyResponseData errorNotifyResponseData = (ErrorNotifyResponseData) delegateResponseData;
      return errorNotifyResponseData.getErrorMessage();
    }

    return (delegateResponseData instanceof RemoteMethodReturnValueData)
        ? ((RemoteMethodReturnValueData) delegateResponseData).getException().getMessage()
        : DEFAULT_HOST_VALIDATION_FAILED_MSG;
  }

  private ErrorDetail buildErrorDetailsWithMsg(final String message) {
    return ErrorDetail.builder().message(message).build();
  }
}
