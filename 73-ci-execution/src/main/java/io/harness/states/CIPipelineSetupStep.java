package io.harness.states;

import static io.harness.cdng.orchestration.StepUtils.createStepResponseFromChildResponse;

import io.harness.ambiance.Ambiance;
import io.harness.beans.CIPipelineSetupParameters;
import io.harness.delegate.beans.ResponseData;
import io.harness.execution.status.Status;
import io.harness.facilitator.PassThroughData;
import io.harness.facilitator.modes.child.ChildExecutable;
import io.harness.facilitator.modes.child.ChildExecutableResponse;
import io.harness.facilitator.modes.sync.SyncExecutable;
import io.harness.state.Step;
import io.harness.state.StepType;
import io.harness.state.io.StepParameters;
import io.harness.state.io.StepResponse;
import io.harness.state.io.StepTransput;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class CIPipelineSetupStep implements Step, ChildExecutable, SyncExecutable {
  public static final StepType STEP_TYPE = StepType.builder().type("CI_PIPELINE_SETUP").build();

  @Override
  public ChildExecutableResponse obtainChild(
      Ambiance ambiance, StepParameters stepParameters, List<StepTransput> inputs) {
    CIPipelineSetupParameters parameters = (CIPipelineSetupParameters) stepParameters;
    logger.info("starting execution for ci pipeline [{}]", parameters);

    final Map<String, String> fieldToExecutionNodeIdMap = parameters.getFieldToExecutionNodeIdMap();
    final String stagesNodeId = fieldToExecutionNodeIdMap.get("stages");
    return ChildExecutableResponse.builder().childNodeId(stagesNodeId).build();
  }

  @Override
  public StepResponse handleChildResponse(
      Ambiance ambiance, StepParameters stepParameters, Map<String, ResponseData> responseDataMap) {
    final CIPipelineSetupParameters parameters = (CIPipelineSetupParameters) stepParameters;

    logger.info("executed pipeline =[{}]", parameters);

    return createStepResponseFromChildResponse(responseDataMap);
  }

  @Override
  public StepResponse executeSync(
      Ambiance ambiance, StepParameters stepParameters, List<StepTransput> inputs, PassThroughData passThroughData) {
    return StepResponse.builder().status(Status.SUCCEEDED).build();
  }
}
