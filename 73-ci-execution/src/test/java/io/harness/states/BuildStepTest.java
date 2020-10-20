package io.harness.states;

import static io.harness.rule.OwnerRule.ALEKSANDAR;
import static io.harness.rule.OwnerRule.HARSH;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.inject.Inject;

import io.harness.ambiance.Ambiance;
import io.harness.beans.steps.stepinfo.BuildStepInfo;
import io.harness.beans.sweepingoutputs.K8PodDetails;
import io.harness.category.element.UnitTests;
import io.harness.engine.outputs.ExecutionSweepingOutputService;
import io.harness.executionplan.CIExecutionPlanTestHelper;
import io.harness.executionplan.CIExecutionTest;
import io.harness.rule.Owner;
import io.harness.service.DelegateGrpcClientWrapper;
import io.harness.stateutils.buildstate.ConnectorUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import software.wings.beans.ci.pod.ConnectorDetails;
import software.wings.helpers.ext.k8s.response.K8sTaskExecutionResponse;

import java.io.IOException;

public class BuildStepTest extends CIExecutionTest {
  @Inject private CIExecutionPlanTestHelper ciExecutionPlanTestHelper;
  @Mock private Ambiance ambiance;
  @Mock private ExecutionSweepingOutputService executionSweepingOutputResolver;
  @Mock private ConnectorUtils connectorUtils;
  @Mock private DelegateGrpcClientWrapper delegateGrpcClientWrapper;

  @InjectMocks private BuildStep buildStep;

  @Before
  public void setUp() {}

  @Test
  @Owner(developers = HARSH)
  @Category(UnitTests.class)
  public void shouldExecuteCIBuildTask() throws IOException {
    when(delegateGrpcClientWrapper.executeSyncTask(any())).thenReturn(K8sTaskExecutionResponse.builder().build());
    when(executionSweepingOutputResolver.resolve(any(), any()))
        .thenReturn(K8PodDetails.builder().clusterName("cluster").namespace("namespace").build());
    when(connectorUtils.getConnectorDetails(any(), any())).thenReturn(ConnectorDetails.builder().build());

    buildStep.executeSync(ambiance,
        BuildStepInfo.builder().scriptInfos(ciExecutionPlanTestHelper.getBuildCommandSteps()).build(), null, null);

    verify(delegateGrpcClientWrapper, times(1)).executeSyncTask(any());
  }

  @Test
  @Owner(developers = ALEKSANDAR)
  @Category(UnitTests.class)
  public void shouldNotExecuteCIBuildTask() throws IOException {
    when(delegateGrpcClientWrapper.executeSyncTask(any())).thenReturn(K8sTaskExecutionResponse.builder().build());
    when(delegateGrpcClientWrapper.executeSyncTask(any())).thenThrow(new RuntimeException());
    when(executionSweepingOutputResolver.resolve(any(), any()))
        .thenReturn(K8PodDetails.builder().clusterName("cluster").namespace("namespace").build());
    when(connectorUtils.getConnectorDetails(any(), any())).thenReturn(ConnectorDetails.builder().build());

    buildStep.executeSync(ambiance,
        BuildStepInfo.builder().scriptInfos(ciExecutionPlanTestHelper.getBuildCommandSteps()).build(), null, null);

    verify(delegateGrpcClientWrapper, times(1)).executeSyncTask(any());
  }
}
