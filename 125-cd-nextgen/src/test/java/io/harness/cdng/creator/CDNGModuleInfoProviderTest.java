package io.harness.cdng.creator;

import static io.harness.data.structure.UUIDGenerator.generateUuid;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import io.harness.CategoryTest;
import io.harness.category.element.UnitTests;
import io.harness.cdng.artifact.GcrArtifactSummary;
import io.harness.cdng.artifact.outcome.ArtifactsOutcome;
import io.harness.cdng.artifact.outcome.GcrArtifactOutcome;
import io.harness.cdng.gitops.steps.GitopsClustersOutcome;
import io.harness.cdng.gitops.steps.Metadata;
import io.harness.cdng.infra.beans.K8sDirectInfrastructureOutcome;
import io.harness.cdng.pipeline.executions.beans.CDPipelineModuleInfo;
import io.harness.cdng.pipeline.executions.beans.CDStageModuleInfo;
import io.harness.cdng.service.beans.ServiceDefinitionType;
import io.harness.cdng.service.steps.ServiceStepOutcome;
import io.harness.executions.steps.ExecutionNodeType;
import io.harness.ng.core.environment.beans.EnvironmentType;
import io.harness.pms.contracts.ambiance.Ambiance;
import io.harness.pms.contracts.ambiance.Level;
import io.harness.pms.contracts.execution.Status;
import io.harness.pms.contracts.steps.StepCategory;
import io.harness.pms.contracts.steps.StepType;
import io.harness.pms.sdk.core.data.OptionalOutcome;
import io.harness.pms.sdk.core.events.OrchestrationEvent;
import io.harness.pms.sdk.core.resolver.RefObjectUtils;
import io.harness.pms.sdk.core.resolver.outcome.OutcomeService;
import io.harness.rule.Owner;
import io.harness.rule.OwnerRule;
import io.harness.steps.environment.EnvironmentOutcome;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CDNGModuleInfoProviderTest extends CategoryTest {
  private final String ACCOUNT_ID = "accountId";
  private final String APP_ID = "appId";

  @Mock OutcomeService outcomeService;
  @InjectMocks CDNGModuleInfoProvider provider;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @Owner(developers = OwnerRule.YOGESH)
  @Category(UnitTests.class)
  public void testGetPipelineLevelModuleInfo_Service() {
    Ambiance ambiance = buildAmbiance(StepType.newBuilder()
                                          .setType(ExecutionNodeType.SERVICE_SECTION.getName())
                                          .setStepCategory(StepCategory.STEP)
                                          .build());

    doReturn(OptionalOutcome.builder()
                 .found(true)
                 .outcome(ServiceStepOutcome.builder()
                              .identifier("s1")
                              .type(ServiceDefinitionType.KUBERNETES.getYamlName())
                              .build())
                 .build())
        .when(outcomeService)
        .resolveOptional(ambiance, RefObjectUtils.getOutcomeRefObject("service"));

    OrchestrationEvent event = OrchestrationEvent.builder().ambiance(ambiance).status(Status.SUCCEEDED).build();
    CDPipelineModuleInfo pipelineLevelModuleInfo = (CDPipelineModuleInfo) provider.getPipelineLevelModuleInfo(event);

    assertThat(pipelineLevelModuleInfo.getServiceIdentifiers()).containsExactlyInAnyOrder("s1");
  }

  @Test
  @Owner(developers = OwnerRule.YOGESH)
  @Category(UnitTests.class)
  public void testGetPipelineLevelModuleInfo_Env() {
    Ambiance ambiance = buildAmbiance(StepType.newBuilder()
                                          .setType(ExecutionNodeType.INFRASTRUCTURE.getName())
                                          .setStepCategory(StepCategory.STEP)
                                          .build());

    doReturn(
        OptionalOutcome.builder()
            .found(true)
            .outcome(K8sDirectInfrastructureOutcome.builder()
                         .environment(
                             EnvironmentOutcome.builder().identifier("env1").type(EnvironmentType.Production).build())
                         .build())
            .build())
        .when(outcomeService)
        .resolveOptional(ambiance, RefObjectUtils.getOutcomeRefObject("stage.spec.infrastructure.output"));

    OrchestrationEvent event = OrchestrationEvent.builder().ambiance(ambiance).status(Status.SUCCEEDED).build();
    CDPipelineModuleInfo pipelineLevelModuleInfo = (CDPipelineModuleInfo) provider.getPipelineLevelModuleInfo(event);

    assertThat(pipelineLevelModuleInfo.getEnvIdentifiers()).containsExactlyInAnyOrder("env1");
    assertThat(pipelineLevelModuleInfo.getEnvironmentTypes()).containsExactlyInAnyOrder(EnvironmentType.Production);
    assertThat(pipelineLevelModuleInfo.getInfrastructureTypes()).containsExactlyInAnyOrder("KubernetesDirect");
  }

  @Test
  @Owner(developers = OwnerRule.YOGESH)
  @Category(UnitTests.class)
  public void testGetPipelineLevelModuleInfo_Gitops_0() {
    Ambiance ambiance = buildAmbiance(StepType.newBuilder()
                                          .setType(ExecutionNodeType.GITOPS_CLUSTERS.getName())
                                          .setStepCategory(StepCategory.STEP)
                                          .build());

    doReturn(OptionalOutcome.builder().found(false).build())
        .when(outcomeService)
        .resolveOptional(ambiance, RefObjectUtils.getOutcomeRefObject("gitops"));

    OrchestrationEvent event = OrchestrationEvent.builder().ambiance(ambiance).status(Status.SUCCEEDED).build();
    assertThat(provider.getPipelineLevelModuleInfo(event)).isNotNull();

    doReturn(null).when(outcomeService).resolveOptional(ambiance, RefObjectUtils.getOutcomeRefObject("gitops"));
    assertThat(provider.getPipelineLevelModuleInfo(event)).isNotNull();
  }

  @Test
  @Owner(developers = OwnerRule.YOGESH)
  @Category(UnitTests.class)
  public void testGetPipelineLevelModuleInfo_Gitops_1() {
    Ambiance ambiance = buildAmbiance(StepType.newBuilder()
                                          .setType(ExecutionNodeType.GITOPS_CLUSTERS.getName())
                                          .setStepCategory(StepCategory.STEP)
                                          .build());

    doReturn(OptionalOutcome.builder()
                 .found(true)
                 .outcome(new GitopsClustersOutcome(new ArrayList<>())
                              .appendCluster(new Metadata("env1", "env1"), new Metadata("c1", "c1")))
                 .build())
        .when(outcomeService)
        .resolveOptional(ambiance, RefObjectUtils.getOutcomeRefObject("gitops"));

    OrchestrationEvent event = OrchestrationEvent.builder().ambiance(ambiance).status(Status.SUCCEEDED).build();
    CDPipelineModuleInfo pipelineLevelModuleInfo = (CDPipelineModuleInfo) provider.getPipelineLevelModuleInfo(event);

    assertThat(pipelineLevelModuleInfo.getEnvIdentifiers()).containsExactlyInAnyOrder("env1");
  }

  @Test
  @Owner(developers = OwnerRule.YOGESH)
  @Category(UnitTests.class)
  public void testGetPipelineLevelModuleInfo_Gitops_2() {
    Ambiance ambiance = buildAmbiance(StepType.newBuilder()
                                          .setType(ExecutionNodeType.GITOPS_CLUSTERS.getName())
                                          .setStepCategory(StepCategory.STEP)
                                          .build());

    doReturn(OptionalOutcome.builder()
                 .found(true)
                 .outcome(new GitopsClustersOutcome(new ArrayList<>())
                              .appendCluster(new Metadata("env1", "env1"), new Metadata("c1", "c1"))
                              .appendCluster(new Metadata("env2", "env2"), new Metadata("c2", "c2")))
                 .build())
        .when(outcomeService)
        .resolveOptional(ambiance, RefObjectUtils.getOutcomeRefObject("gitops"));

    OrchestrationEvent event = OrchestrationEvent.builder().ambiance(ambiance).status(Status.SUCCEEDED).build();
    CDPipelineModuleInfo pipelineLevelModuleInfo = (CDPipelineModuleInfo) provider.getPipelineLevelModuleInfo(event);

    assertThat(pipelineLevelModuleInfo.getEnvIdentifiers()).containsExactlyInAnyOrder("env1", "env2");
  }

  @Test
  @Owner(developers = OwnerRule.YOGESH)
  @Category(UnitTests.class)
  public void testGetPipelineLevelModuleInfo_GitopsEnvGroup() {
    Ambiance ambiance = buildAmbiance(StepType.newBuilder()
                                          .setType(ExecutionNodeType.GITOPS_CLUSTERS.getName())
                                          .setStepCategory(StepCategory.STEP)
                                          .build());

    doReturn(OptionalOutcome.builder()
                 .found(true)
                 .outcome(new GitopsClustersOutcome(new ArrayList<>())
                              .appendCluster(new Metadata("envgroup1", "envgroup1"), new Metadata("env1", "env1"),
                                  new Metadata("c1", "c1")))
                 .build())
        .when(outcomeService)
        .resolveOptional(ambiance, RefObjectUtils.getOutcomeRefObject("gitops"));

    OrchestrationEvent event = OrchestrationEvent.builder().ambiance(ambiance).status(Status.SUCCEEDED).build();
    CDPipelineModuleInfo pipelineLevelModuleInfo = (CDPipelineModuleInfo) provider.getPipelineLevelModuleInfo(event);

    assertThat(pipelineLevelModuleInfo.getEnvIdentifiers()).containsExactlyInAnyOrder("env1");
    assertThat(pipelineLevelModuleInfo.getEnvGroupIdentifiers()).containsExactlyInAnyOrder("envgroup1");
  }

  @Test
  @Owner(developers = OwnerRule.YOGESH)
  @Category(UnitTests.class)
  public void testGetStageLevelModuleInfo_Svc() {
    Ambiance ambiance = buildAmbiance(StepType.newBuilder()
                                          .setType(ExecutionNodeType.SERVICE_SECTION.getName())
                                          .setStepCategory(StepCategory.STEP)
                                          .build());

    doReturn(OptionalOutcome.builder()
                 .found(true)
                 .outcome(ServiceStepOutcome.builder()
                              .identifier("s1")
                              .type(ServiceDefinitionType.KUBERNETES.getYamlName())
                              .build())
                 .build())
        .when(outcomeService)
        .resolveOptional(ambiance, RefObjectUtils.getOutcomeRefObject("service"));

    doReturn(OptionalOutcome.builder()
                 .found(true)
                 .outcome(ArtifactsOutcome.builder().primary(GcrArtifactOutcome.builder().build()).build())
                 .build())
        .when(outcomeService)
        .resolveOptional(ambiance, RefObjectUtils.getOutcomeRefObject("artifacts"));

    OrchestrationEvent event = OrchestrationEvent.builder().ambiance(ambiance).status(Status.SUCCEEDED).build();
    CDStageModuleInfo stageLevelModuleInfo = (CDStageModuleInfo) provider.getStageLevelModuleInfo(event);

    assertThat(stageLevelModuleInfo.getServiceInfo().getIdentifier()).isEqualTo("s1");
    assertThat(stageLevelModuleInfo.getServiceInfo().getArtifacts().getPrimary())
        .isInstanceOf(GcrArtifactSummary.class);
  }

  @Test
  @Owner(developers = OwnerRule.YOGESH)
  @Category(UnitTests.class)
  public void testGetStageLevelModuleInfo_Env() {
    Ambiance ambiance = buildAmbiance(StepType.newBuilder()
                                          .setType(ExecutionNodeType.INFRASTRUCTURE.getName())
                                          .setStepCategory(StepCategory.STEP)
                                          .build());

    doReturn(
        OptionalOutcome.builder()
            .found(true)
            .outcome(K8sDirectInfrastructureOutcome.builder()
                         .environment(
                             EnvironmentOutcome.builder().identifier("env1").type(EnvironmentType.Production).build())
                         .build())
            .build())
        .when(outcomeService)
        .resolveOptional(ambiance, RefObjectUtils.getOutcomeRefObject("output"));

    OrchestrationEvent event = OrchestrationEvent.builder().ambiance(ambiance).status(Status.SUCCEEDED).build();
    CDStageModuleInfo stageLevelModuleInfo = (CDStageModuleInfo) provider.getStageLevelModuleInfo(event);

    assertThat(stageLevelModuleInfo.getInfraExecutionSummary().getIdentifier()).isEqualTo("env1");
  }

  @Test
  @Owner(developers = OwnerRule.YOGESH)
  @Category(UnitTests.class)
  public void testGetStageLevelModuleInfo_Gitops_0() {
    Ambiance ambiance = buildAmbiance(StepType.newBuilder()
                                          .setType(ExecutionNodeType.GITOPS_CLUSTERS.getName())
                                          .setStepCategory(StepCategory.STEP)
                                          .build());

    doReturn(OptionalOutcome.builder()
                 .found(true)
                 .outcome(new GitopsClustersOutcome(new ArrayList<>())
                              .appendCluster(new Metadata("env1", "env1"), new Metadata("c1", "c1")))
                 .build())
        .when(outcomeService)
        .resolveOptional(ambiance, RefObjectUtils.getOutcomeRefObject("gitops"));

    OrchestrationEvent event = OrchestrationEvent.builder().ambiance(ambiance).status(Status.SUCCEEDED).build();
    CDStageModuleInfo stageLevelModuleInfo = (CDStageModuleInfo) provider.getStageLevelModuleInfo(event);

    assertThat(stageLevelModuleInfo.getInfraExecutionSummary().getIdentifier()).isEqualTo("env1");
  }

  @Test
  @Owner(developers = OwnerRule.YOGESH)
  @Category(UnitTests.class)
  public void testGetStageLevelModuleInfo_Gitops_1() {
    Ambiance ambiance = buildAmbiance(StepType.newBuilder()
                                          .setType(ExecutionNodeType.GITOPS_CLUSTERS.getName())
                                          .setStepCategory(StepCategory.STEP)
                                          .build());

    doReturn(OptionalOutcome.builder()
                 .found(true)
                 .outcome(new GitopsClustersOutcome(new ArrayList<>())
                              .appendCluster(new Metadata("env1", "env1"), new Metadata("c1", "c1"))
                              .appendCluster(new Metadata("env2", "env2"), new Metadata("c2", "c2")))
                 .build())
        .when(outcomeService)
        .resolveOptional(ambiance, RefObjectUtils.getOutcomeRefObject("gitops"));

    OrchestrationEvent event = OrchestrationEvent.builder().ambiance(ambiance).status(Status.SUCCEEDED).build();
    CDStageModuleInfo stageLevelModuleInfo = (CDStageModuleInfo) provider.getStageLevelModuleInfo(event);

    assertThat(stageLevelModuleInfo.getInfraExecutionSummary()).isNull();
  }

  @Test
  @Owner(developers = OwnerRule.YOGESH)
  @Category(UnitTests.class)
  public void testGetStageLevelModuleInfo_Gitops_2() {
    Ambiance ambiance = buildAmbiance(StepType.newBuilder()
                                          .setType(ExecutionNodeType.GITOPS_CLUSTERS.getName())
                                          .setStepCategory(StepCategory.STEP)
                                          .build());

    doReturn(OptionalOutcome.builder()
                 .found(true)
                 .outcome(new GitopsClustersOutcome(new ArrayList<>())
                              .appendCluster(new Metadata("env1", "env1"), new Metadata("c1", "c1"))
                              .appendCluster(new Metadata("env2", "env2"), new Metadata("c2", "c2")))
                 .build())
        .when(outcomeService)
        .resolveOptional(ambiance, RefObjectUtils.getOutcomeRefObject("gitops"));

    OrchestrationEvent event = OrchestrationEvent.builder().ambiance(ambiance).status(Status.SUCCEEDED).build();
    CDStageModuleInfo stageLevelModuleInfo = (CDStageModuleInfo) provider.getStageLevelModuleInfo(event);

    assertThat(stageLevelModuleInfo.getInfraExecutionSummary().getIdentifier()).isEqualTo("env1");
  }

  @Test
  @Owner(developers = OwnerRule.YOGESH)
  @Category(UnitTests.class)
  public void testShouldRun() {}

  public Ambiance buildAmbiance(StepType stepType) {
    final String PHASE_RUNTIME_ID = generateUuid();
    final String PHASE_SETUP_ID = generateUuid();
    final String PLAN_EXECUTION_ID = generateUuid();
    List<Level> levels = new ArrayList<>();
    levels.add(
        Level.newBuilder().setRuntimeId(PHASE_RUNTIME_ID).setSetupId(PHASE_SETUP_ID).setStepType(stepType).build());
    return Ambiance.newBuilder()
        .setPlanExecutionId(PLAN_EXECUTION_ID)
        .putAllSetupAbstractions(Map.of("accountId", ACCOUNT_ID, "appId", APP_ID))
        .addAllLevels(levels)
        .setExpressionFunctorToken(1234)
        .build();
  }
}