package io.harness.plancreator.strategy;

import io.harness.data.structure.EmptyPredicate;
import io.harness.exception.InvalidRequestException;
import io.harness.pms.contracts.facilitators.FacilitatorObtainment;
import io.harness.pms.contracts.facilitators.FacilitatorType;
import io.harness.pms.execution.OrchestrationFacilitatorType;
import io.harness.pms.plan.creation.PlanCreatorUtils;
import io.harness.pms.sdk.core.plan.PlanNode;
import io.harness.pms.sdk.core.plan.creation.beans.PlanCreationContext;
import io.harness.pms.sdk.core.plan.creation.beans.PlanCreationResponse;
import io.harness.pms.sdk.core.plan.creation.creators.ChildrenPlanCreator;
import io.harness.pms.sdk.core.plan.creation.yaml.StepOutcomeGroup;
import io.harness.pms.sdk.core.steps.io.StepParameters;
import io.harness.pms.yaml.YAMLFieldNameConstants;
import io.harness.serializer.KryoSerializer;
import io.harness.steps.matrix.StrategyConstants;
import io.harness.steps.matrix.StrategyMetadata;
import io.harness.steps.matrix.StrategyStep;
import io.harness.steps.matrix.StrategyStepParameters;

import com.google.inject.Inject;
import com.google.protobuf.ByteString;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StrategyConfigPlanCreator extends ChildrenPlanCreator<StrategyConfig> {
  @Inject KryoSerializer kryoSerializer;

  @Override
  public LinkedHashMap<String, PlanCreationResponse> createPlanForChildrenNodes(
      PlanCreationContext ctx, StrategyConfig config) {
    return new LinkedHashMap<>();
  }

  @Override
  public PlanNode createPlanForParentNode(
      PlanCreationContext ctx, StrategyConfig config, List<String> childrenNodeIds) {
    ByteString strategyMetadata = ctx.getDependency().getMetadataMap().get(
        StrategyConstants.STRATEGY_METADATA + ctx.getCurrentField().getNode().getUuid());
    StrategyMetadata metadata = (StrategyMetadata) kryoSerializer.asInflatedObject(strategyMetadata.toByteArray());
    String childNodeId = metadata.getChildNodeId();
    String strategyNodeId = metadata.getStrategyNodeId();
    if (EmptyPredicate.isEmpty(childNodeId) || EmptyPredicate.isEmpty(strategyNodeId)) {
      log.error("childNodeId and strategyNodeId not passed from parent. Please pass it.");
      throw new InvalidRequestException("Invalid use of strategy field. Please check");
    }

    StepParameters stepParameters =
        StrategyStepParameters.builder().childNodeId(childNodeId).strategyConfig(config).build();
    return PlanNode.builder()
        .uuid(strategyNodeId)
        .identifier(YAMLFieldNameConstants.STRATEGY)
        .stepType(StrategyStep.STEP_TYPE)
        .group(StepOutcomeGroup.STRATEGY.name())
        .name(YAMLFieldNameConstants.STRATEGY)
        .stepParameters(stepParameters)
        .facilitatorObtainment(
            FacilitatorObtainment.newBuilder()
                .setType(FacilitatorType.newBuilder().setType(OrchestrationFacilitatorType.CHILDREN).build())
                .build())
        .skipExpressionChain(true)
        .build();
  }

  @Override
  public Class<StrategyConfig> getFieldClass() {
    return StrategyConfig.class;
  }

  @Override
  public Map<String, Set<String>> getSupportedTypes() {
    return Collections.singletonMap(YAMLFieldNameConstants.STRATEGY, Collections.singleton(PlanCreatorUtils.ANY_TYPE));
  }
}
