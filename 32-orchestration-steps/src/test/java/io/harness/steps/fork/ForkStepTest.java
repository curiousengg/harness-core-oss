package io.harness.steps.fork;

import static io.harness.data.structure.UUIDGenerator.generateUuid;
import static io.harness.rule.OwnerRule.PRASHANT;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import io.harness.OrchestrationStepsTest;
import io.harness.ambiance.Ambiance;
import io.harness.category.element.UnitTests;
import io.harness.execution.status.Status;
import io.harness.facilitator.modes.children.ChildrenExecutableResponse;
import io.harness.rule.Owner;
import io.harness.state.io.StepInputPackage;
import io.harness.state.io.StepResponse;
import io.harness.state.io.StepResponseNotifyData;
import io.harness.tasks.ResponseData;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ForkStepTest extends OrchestrationStepsTest {
  @Inject private ForkStep forkStep;

  private static final String FIRST_CHILD_ID = generateUuid();
  private static final String SECOND_CHILD_ID = generateUuid();

  @Test
  @Owner(developers = PRASHANT)
  @Category(UnitTests.class)
  public void shouldTestObtainChildren() {
    Ambiance ambiance = Ambiance.builder().build();
    StepInputPackage inputPackage = StepInputPackage.builder().build();
    ForkStepParameters stateParameters =
        ForkStepParameters.builder().parallelNodeId(FIRST_CHILD_ID).parallelNodeId(SECOND_CHILD_ID).build();
    ChildrenExecutableResponse childrenExecutableResponse =
        forkStep.obtainChildren(ambiance, stateParameters, inputPackage);
    assertThat(childrenExecutableResponse).isNotNull();
    assertThat(childrenExecutableResponse.getChildren()).hasSize(2);
    List<String> childIds = childrenExecutableResponse.getChildren()
                                .stream()
                                .map(ChildrenExecutableResponse.Child::getChildNodeId)
                                .collect(Collectors.toList());
    assertThat(childIds).hasSize(2);
    assertThat(childIds).containsExactlyInAnyOrder(FIRST_CHILD_ID, SECOND_CHILD_ID);
  }

  @Test
  @Owner(developers = PRASHANT)
  @Category(UnitTests.class)
  public void shouldTestHandleChildrenResponse() {
    Ambiance ambiance = Ambiance.builder().build();
    ForkStepParameters stateParameters =
        ForkStepParameters.builder().parallelNodeId(FIRST_CHILD_ID).parallelNodeId(SECOND_CHILD_ID).build();

    Map<String, ResponseData> responseDataMap =
        ImmutableMap.<String, ResponseData>builder()
            .put(FIRST_CHILD_ID, StepResponseNotifyData.builder().status(Status.SUCCEEDED).build())
            .put(SECOND_CHILD_ID, StepResponseNotifyData.builder().status(Status.FAILED).build())
            .build();
    StepResponse stepResponse = forkStep.handleChildrenResponse(ambiance, stateParameters, responseDataMap);
    assertThat(stepResponse.getStatus()).isEqualTo(Status.FAILED);
  }
}