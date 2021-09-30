package io.harness.pms.plan.execution;

import static io.harness.annotations.dev.HarnessTeam.PIPELINE;
import static io.harness.rule.OwnerRule.NAMAN;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.harness.CategoryTest;
import io.harness.annotations.dev.OwnedBy;
import io.harness.category.element.UnitTests;
import io.harness.data.structure.EmptyPredicate;
import io.harness.execution.PlanExecution;
import io.harness.execution.PlanExecutionMetadata;
import io.harness.gitsync.sdk.EntityGitDetails;
import io.harness.pms.contracts.plan.ExecutionMetadata;
import io.harness.pms.contracts.plan.ExecutionTriggerInfo;
import io.harness.pms.ngpipeline.inputset.helpers.ValidateAndMergeHelper;
import io.harness.pms.pipeline.PipelineEntity;
import io.harness.pms.plan.execution.beans.ExecArgs;
import io.harness.pms.plan.execution.beans.dto.RunStageRequestDTO;
import io.harness.rule.Owner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@OwnedBy(PIPELINE)
public class PipelineExecutorTest extends CategoryTest {
  @InjectMocks PipelineExecutor pipelineExecutor;
  @Mock ExecutionHelper executionHelper;
  @Mock ValidateAndMergeHelper validateAndMergeHelper;

  String accountId = "accountId";
  String orgId = "orgId";
  String projectId = "projectId";
  String pipelineId = "pipelineId";
  String moduleType = "cd";
  String runtimeInputYaml = "pipeline:\n"
      + "  variables:\n"
      + "  - name: a\n"
      + "    type: String\n"
      + "    value: c";
  List<String> stageIdentifiers = Arrays.asList("a1", "a2", "s1");
  RunStageRequestDTO runStageRequestDTO =
      RunStageRequestDTO.builder().runtimeInputYaml(runtimeInputYaml).stageIdentifiers(stageIdentifiers).build();
  String originalExecutionId = "originalExecutionId";
  boolean useV2 = false;
  List<String> inputSetReferences = Arrays.asList("i1", "i2", "i3");
  String pipelineBranch = null;
  String pipelineRepoId = null;

  PipelineEntity pipelineEntity = PipelineEntity.builder().build();
  ExecutionTriggerInfo executionTriggerInfo = ExecutionTriggerInfo.newBuilder().build();
  ExecutionMetadata metadata = ExecutionMetadata.newBuilder().build();
  PlanExecutionMetadata planExecutionMetadata = PlanExecutionMetadata.builder().build();
  ExecArgs execArgs = ExecArgs.builder().metadata(metadata).planExecutionMetadata(planExecutionMetadata).build();
  PlanExecution planExecution = PlanExecution.builder().build();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  @Owner(developers = NAMAN)
  @Category(UnitTests.class)
  public void testRunPipelineWithInputSetPipelineYaml() {
    doReturnStatementsForFreshRun(null, false, null, null);

    PlanExecutionResponseDto planExecutionResponse = pipelineExecutor.runPipelineWithInputSetPipelineYaml(
        accountId, orgId, projectId, pipelineId, moduleType, runtimeInputYaml, useV2);
    assertThat(planExecutionResponse.getPlanExecution()).isEqualTo(planExecution);
    assertThat(planExecutionResponse.getGitDetails()).isEqualTo(EntityGitDetails.builder().build());

    verifyStatementsForFreshRun(null, false, null);
  }

  @Test
  @Owner(developers = NAMAN)
  @Category(UnitTests.class)
  public void testRunPipelineWithInputSetReferencesList() {
    doReturnStatementsForFreshRun(null, true, null, null);

    PlanExecutionResponseDto planExecutionResponse = pipelineExecutor.runPipelineWithInputSetReferencesList(
        accountId, orgId, projectId, pipelineId, moduleType, inputSetReferences, pipelineBranch, pipelineRepoId);
    assertThat(planExecutionResponse.getPlanExecution()).isEqualTo(planExecution);
    assertThat(planExecutionResponse.getGitDetails()).isEqualTo(EntityGitDetails.builder().build());

    verify(validateAndMergeHelper, times(1))
        .getMergeInputSetFromPipelineTemplate(
            accountId, orgId, projectId, pipelineId, inputSetReferences, pipelineBranch, pipelineRepoId);
    verifyStatementsForFreshRun(null, true, null);
  }

  @Test
  @Owner(developers = NAMAN)
  @Category(UnitTests.class)
  public void testRunStagesWithRuntimeInputYaml() {
    doReturnStatementsForFreshRun(null, false, stageIdentifiers, null);

    PlanExecutionResponseDto planExecutionResponse = pipelineExecutor.runStagesWithRuntimeInputYaml(
        accountId, orgId, projectId, pipelineId, moduleType, runStageRequestDTO, useV2);
    assertThat(planExecutionResponse.getPlanExecution()).isEqualTo(planExecution);
    assertThat(planExecutionResponse.getGitDetails()).isEqualTo(EntityGitDetails.builder().build());

    verifyStatementsForFreshRun(null, false, stageIdentifiers);
  }

  @Test
  @Owner(developers = NAMAN)
  @Category(UnitTests.class)
  public void testRerunPipelineWithInputSetPipelineYaml() {
    doReturnStatementsForFreshRun(originalExecutionId, false, null, null);

    PlanExecutionResponseDto planExecutionResponse = pipelineExecutor.rerunPipelineWithInputSetPipelineYaml(
        accountId, orgId, projectId, pipelineId, moduleType, originalExecutionId, runtimeInputYaml, useV2);
    assertThat(planExecutionResponse.getPlanExecution()).isEqualTo(planExecution);
    assertThat(planExecutionResponse.getGitDetails()).isEqualTo(EntityGitDetails.builder().build());

    verifyStatementsForFreshRun(originalExecutionId, false, null);
  }

  @Test
  @Owner(developers = NAMAN)
  @Category(UnitTests.class)
  public void testRerunPipelineWithInputSetReferencesList() {
    doReturnStatementsForFreshRun(originalExecutionId, true, null, null);

    PlanExecutionResponseDto planExecutionResponse =
        pipelineExecutor.rerunPipelineWithInputSetReferencesList(accountId, orgId, projectId, pipelineId, moduleType,
            originalExecutionId, inputSetReferences, pipelineBranch, pipelineRepoId);
    assertThat(planExecutionResponse.getPlanExecution()).isEqualTo(planExecution);
    assertThat(planExecutionResponse.getGitDetails()).isEqualTo(EntityGitDetails.builder().build());

    verifyStatementsForFreshRun(originalExecutionId, true, null);
  }

  private void doReturnStatementsForFreshRun(String originalExecutionId, boolean addValidateAndMergeHelperDoReturn,
      List<String> stageIdentifiers, List<String> uuidForSkipNodes) {
    if (addValidateAndMergeHelperDoReturn) {
      doReturn(runtimeInputYaml)
          .when(validateAndMergeHelper)
          .getMergeInputSetFromPipelineTemplate(
              accountId, orgId, projectId, pipelineId, inputSetReferences, pipelineBranch, pipelineRepoId);
    }

    doReturn(pipelineEntity).when(executionHelper).fetchPipelineEntity(accountId, orgId, projectId, pipelineId);
    doReturn(executionTriggerInfo).when(executionHelper).buildTriggerInfo(originalExecutionId);
    if (EmptyPredicate.isEmpty(stageIdentifiers)) {
      doReturn(execArgs)
          .when(executionHelper)
          .buildExecutionArgs(pipelineEntity, moduleType, runtimeInputYaml, Collections.emptyList(),
              executionTriggerInfo, originalExecutionId, false, null, null, null);
    } else {
      doReturn(execArgs)
          .when(executionHelper)
          .buildExecutionArgs(pipelineEntity, moduleType, runtimeInputYaml, stageIdentifiers, executionTriggerInfo,
              originalExecutionId, false, null, null, null);
    }

    doReturn(planExecution)
        .when(executionHelper)
        .startExecution(accountId, orgId, projectId, metadata, planExecutionMetadata, false, null);
  }

  private void verifyStatementsForFreshRun(
      String originalExecutionId, boolean verifyValidateAndMergeHelper, List<String> stageIdentifiers) {
    if (verifyValidateAndMergeHelper) {
      verify(validateAndMergeHelper, times(1))
          .getMergeInputSetFromPipelineTemplate(
              accountId, orgId, projectId, pipelineId, inputSetReferences, pipelineBranch, pipelineRepoId);
    }

    verify(executionHelper, times(1)).fetchPipelineEntity(accountId, orgId, projectId, pipelineId);
    verify(executionHelper, times(1)).buildTriggerInfo(originalExecutionId);
    if (EmptyPredicate.isEmpty(stageIdentifiers)) {
      verify(executionHelper, times(1))
          .buildExecutionArgs(pipelineEntity, moduleType, runtimeInputYaml, Collections.emptyList(),
              executionTriggerInfo, originalExecutionId, false, null, null, null);
    } else {
      verify(executionHelper, times(1))
          .buildExecutionArgs(pipelineEntity, moduleType, runtimeInputYaml, stageIdentifiers, executionTriggerInfo,
              originalExecutionId, false, null, null, null);
    }
    verify(executionHelper, times(1))
        .startExecution(accountId, orgId, projectId, metadata, planExecutionMetadata, false, null);
    verify(executionHelper, times(0))
        .startExecutionV2(anyString(), anyString(), anyString(), any(), any(), anyBoolean(), any());
  }
}