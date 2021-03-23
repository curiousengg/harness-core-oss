package io.harness.delegate.task.jira;

import static io.harness.rule.OwnerRule.GARVIT;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.harness.category.element.UnitTests;
import io.harness.jira.JiraActionNG;
import io.harness.rule.Owner;
import io.harness.security.encryption.SecretDecryptionService;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class JiraTaskNGHelperTest {
  @Mock private JiraTaskNGHandler jiraTaskNGHandler;
  @Mock private SecretDecryptionService secretDecryptionService;

  @InjectMocks private JiraTaskNGHelper jiraTaskNGHelper;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  @Owner(developers = GARVIT)
  @Category(UnitTests.class)
  public void shouldTValidateCredentials() {
    JiraTaskNGParameters params = setupMocksForAction(JiraActionNG.VALIDATE_CREDENTIALS);
    jiraTaskNGHelper.getJiraTaskResponse(params);
    verify(jiraTaskNGHandler).validateCredentials(params);
  }

  @Test
  @Owner(developers = GARVIT)
  @Category(UnitTests.class)
  public void shouldGetProjects() {
    JiraTaskNGParameters params = setupMocksForAction(JiraActionNG.GET_PROJECTS);
    jiraTaskNGHelper.getJiraTaskResponse(params);
    verify(jiraTaskNGHandler).getProjects(params);
  }

  @Test
  @Owner(developers = GARVIT)
  @Category(UnitTests.class)
  public void shouldGetIssue() {
    JiraTaskNGParameters params = setupMocksForAction(JiraActionNG.GET_ISSUE);
    jiraTaskNGHelper.getJiraTaskResponse(params);
    verify(jiraTaskNGHandler).getIssue(params);
  }

  @Test
  @Owner(developers = GARVIT)
  @Category(UnitTests.class)
  public void shouldGetIssueCreateMetadata() {
    JiraTaskNGParameters params = setupMocksForAction(JiraActionNG.GET_ISSUE_CREATE_METADATA);
    jiraTaskNGHelper.getJiraTaskResponse(params);
    verify(jiraTaskNGHandler).getIssueCreateMetadata(params);
  }

  @Test
  @Owner(developers = GARVIT)
  @Category(UnitTests.class)
  public void shouldCreateIssue() {
    JiraTaskNGParameters params = setupMocksForAction(JiraActionNG.CREATE_ISSUE);
    jiraTaskNGHelper.getJiraTaskResponse(params);
    verify(jiraTaskNGHandler).createIssue(params);
  }

  private JiraTaskNGParameters setupMocksForAction(JiraActionNG action) {
    JiraTaskNGResponse mockedResponse = JiraTaskNGResponse.builder().build();
    JiraTaskNGParameters params = JiraTaskNGParameters.builder().action(action).build();
    when(secretDecryptionService.decrypt(any(), any())).thenReturn(null);
    when(jiraTaskNGHandler.createIssue(params)).thenReturn(mockedResponse);
    return params;
  }
}
