package io.harness.delegate.task.jira;

import io.harness.exception.InvalidRequestException;
import io.harness.security.encryption.SecretDecryptionService;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor(access = AccessLevel.PACKAGE, onConstructor = @__({ @Inject }))
@Slf4j
@Singleton
public class JiraTaskNGHelper {
  private final JiraTaskNGHandler jiraTaskNGHandler;
  private final SecretDecryptionService secretDecryptionService;

  public JiraTaskNGResponse getJiraTaskResponse(JiraTaskNGParameters params) {
    decryptRequestDTOs(params);
    switch (params.getAction()) {
      case VALIDATE_CREDENTIALS:
        return jiraTaskNGHandler.validateCredentials(params);
      case GET_PROJECTS:
        return jiraTaskNGHandler.getProjects(params);
      case GET_ISSUE:
        return jiraTaskNGHandler.getIssue(params);
      case GET_ISSUE_CREATE_METADATA:
        return jiraTaskNGHandler.getIssueCreateMetadata(params);
      case CREATE_ISSUE:
        return jiraTaskNGHandler.createIssue(params);
      case UPDATE_ISSUE:
        return jiraTaskNGHandler.updateIssue(params);
      default:
        throw new InvalidRequestException(String.format("Invalid jira action: %s", params.getAction()));
    }
  }

  private void decryptRequestDTOs(JiraTaskNGParameters jiraTaskNGParameters) {
    secretDecryptionService.decrypt(
        jiraTaskNGParameters.getJiraConnectorDTO(), jiraTaskNGParameters.getEncryptionDetails());
  }
}
