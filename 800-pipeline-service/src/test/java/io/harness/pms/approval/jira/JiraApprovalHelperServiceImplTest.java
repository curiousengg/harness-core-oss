package io.harness.pms.approval.jira;

import static io.harness.annotations.dev.HarnessTeam.PIPELINE;
import static io.harness.rule.OwnerRule.BRIJESH;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.harness.CategoryTest;
import io.harness.annotations.dev.OwnedBy;
import io.harness.category.element.UnitTests;
import io.harness.connector.ConnectorDTO;
import io.harness.connector.ConnectorInfoDTO;
import io.harness.connector.ConnectorResourceClient;
import io.harness.delegate.beans.connector.awsconnector.AwsConnectorDTO;
import io.harness.delegate.beans.connector.jira.JiraConnectorDTO;
import io.harness.engine.pms.tasks.NgDelegate2TaskExecutor;
import io.harness.exception.HarnessJiraException;
import io.harness.logstreaming.ILogStreamingStepClient;
import io.harness.logstreaming.LogStreamingStepClientFactory;
import io.harness.pms.contracts.ambiance.Ambiance;
import io.harness.pms.gitsync.PmsGitSyncHelper;
import io.harness.pms.yaml.ParameterField;
import io.harness.remote.client.NGRestUtils;
import io.harness.rule.Owner;
import io.harness.secrets.remote.SecretNGManagerClient;
import io.harness.serializer.KryoSerializer;
import io.harness.steps.StepUtils;
import io.harness.steps.approval.step.beans.ApprovalType;
import io.harness.steps.approval.step.jira.beans.CriteriaSpecWrapperDTO;
import io.harness.steps.approval.step.jira.beans.KeyValuesCriteriaSpecDTO;
import io.harness.steps.approval.step.jira.entities.JiraApprovalInstance;
import io.harness.waiter.WaitNotifyEngine;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@OwnedBy(PIPELINE)
@PrepareForTest({NGRestUtils.class, StepUtils.class})
public class JiraApprovalHelperServiceImplTest extends CategoryTest {
  @Mock private NgDelegate2TaskExecutor ngDelegate2TaskExecutor;
  @Mock private ConnectorResourceClient connectorResourceClient;
  @Mock private KryoSerializer kryoSerializer;
  @Mock private SecretNGManagerClient secretManagerClient;
  @Mock private WaitNotifyEngine waitNotifyEngine;
  @Mock private LogStreamingStepClientFactory logStreamingStepClientFactory;
  private String publisherName = "publisherName";
  @Mock private PmsGitSyncHelper pmsGitSyncHelper;
  JiraApprovalHelperServiceImpl jiraApprovalHelperService;
  @Mock ILogStreamingStepClient iLogStreamingStepClient;
  private static String accountId = "accountId";
  private static String orgIdentifier = "orgIdentifier";
  private static String projectIdentifier = "projectIdentifier";
  private static String pipelineIdentifier = "pipelineIdentifier";

  @Before
  public void setUp() {
    jiraApprovalHelperService = spy(new JiraApprovalHelperServiceImpl(ngDelegate2TaskExecutor, connectorResourceClient,
        kryoSerializer, secretManagerClient, waitNotifyEngine, logStreamingStepClientFactory, publisherName,
        pmsGitSyncHelper, null));
  }

  @Test
  @Owner(developers = BRIJESH)
  @Category(UnitTests.class)
  public void testHandlePollingEvent() {
    PowerMockito.mockStatic(NGRestUtils.class);
    PowerMockito.mockStatic(StepUtils.class);
    Ambiance ambiance = Ambiance.newBuilder()
                            .putSetupAbstractions("accountId", accountId)
                            .putSetupAbstractions("orgIdentifier", orgIdentifier)
                            .putSetupAbstractions("projectIdentifier", projectIdentifier)
                            .putSetupAbstractions("pipelineIdentifier", pipelineIdentifier)
                            .build();
    doReturn(iLogStreamingStepClient).when(logStreamingStepClientFactory).getLogStreamingStepClient(ambiance);

    JiraApprovalInstance instance = getJiraApprovalInstance(ambiance);
    when(NGRestUtils.getResponse(any())).thenReturn(Collections.EMPTY_LIST);
    doReturn(JiraConnectorDTO.builder().build())
        .when(jiraApprovalHelperService)
        .getJiraConnector(eq(accountId), eq(orgIdentifier), eq(projectIdentifier), any());

    jiraApprovalHelperService.handlePollingEvent(instance);

    verify(ngDelegate2TaskExecutor, times(1)).queueTask(any(), any(), any());
  }

  @Test
  @Owner(developers = BRIJESH)
  @Category(UnitTests.class)
  public void testGetConnector() {
    PowerMockito.mockStatic(NGRestUtils.class);

    Optional<ConnectorDTO> connectorDTO = Optional.of(
        ConnectorDTO.builder()
            .connectorInfo(ConnectorInfoDTO.builder().connectorConfig(JiraConnectorDTO.builder().build()).build())
            .build());
    Optional<ConnectorDTO> connectorDTO1 = Optional.of(
        ConnectorDTO.builder()
            .connectorInfo(ConnectorInfoDTO.builder().connectorConfig(AwsConnectorDTO.builder().build()).build())
            .build());
    when(NGRestUtils.getResponse(any())).thenReturn(connectorDTO);
    jiraApprovalHelperService.getJiraConnector(accountId, orgIdentifier, projectIdentifier, "connectorRef");
    when(NGRestUtils.getResponse(any())).thenReturn(connectorDTO1);
    assertThatThrownBy(
        () -> jiraApprovalHelperService.getJiraConnector(accountId, orgIdentifier, projectIdentifier, "connectorRef"))
        .isInstanceOf(HarnessJiraException.class);
    when(NGRestUtils.getResponse(null)).thenReturn(Optional.empty());
    assertThatThrownBy(
        () -> jiraApprovalHelperService.getJiraConnector(accountId, orgIdentifier, projectIdentifier, "connectorRef"))
        .isInstanceOf(HarnessJiraException.class);
  }

  private JiraApprovalInstance getJiraApprovalInstance(Ambiance ambiance) {
    JiraApprovalInstance instance =
        JiraApprovalInstance.builder()
            .issueKey("issueKey")
            .delegateSelectors(ParameterField.<List<String>>builder().build())
            .connectorRef("connectorRed")
            .approvalCriteria(
                CriteriaSpecWrapperDTO.builder().criteriaSpecDTO(KeyValuesCriteriaSpecDTO.builder().build()).build())
            .build();
    instance.setAmbiance(ambiance);
    instance.setId("id");
    instance.setType(ApprovalType.JIRA_APPROVAL);
    return instance;
  }
}
