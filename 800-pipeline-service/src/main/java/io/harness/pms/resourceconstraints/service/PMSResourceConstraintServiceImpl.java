/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.pms.resourceconstraints.service;

import static io.harness.distribution.constraint.Consumer.State.ACTIVE;
import static io.harness.distribution.constraint.Consumer.State.BLOCKED;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.engine.executions.plan.PlanExecutionService;
import io.harness.exception.InvalidRequestException;
import io.harness.execution.PlanExecution;
import io.harness.pms.pipeline.PipelineEntity;
import io.harness.pms.pipeline.service.PMSPipelineService;
import io.harness.pms.resourceconstraints.response.ResourceConstraintDetailDTO;
import io.harness.pms.resourceconstraints.response.ResourceConstraintExecutionInfoDTO;
import io.harness.pms.utils.PmsConstants;
import io.harness.steps.resourcerestraint.beans.ResourceRestraint;
import io.harness.steps.resourcerestraint.beans.ResourceRestraintInstance;
import io.harness.steps.resourcerestraint.service.ResourceRestraintInstanceService;
import io.harness.steps.resourcerestraint.service.ResourceRestraintService;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor(onConstructor = @__({ @Inject }))
@Slf4j
@OwnedBy(HarnessTeam.PIPELINE)
public class PMSResourceConstraintServiceImpl implements PMSResourceConstraintService {
  public static final String NOT_FOUND_WITH_ARGUMENTS = "Resource Constraint not found for accountId : %s";

  private final ResourceRestraintService resourceRestraintService;
  private final ResourceRestraintInstanceService resourceRestraintInstanceService;
  private final PlanExecutionService planExecutionService;
  private final PMSPipelineService pipelineService;

  public ResourceConstraintExecutionInfoDTO getResourceConstraintExecutionInfo(String accountId, String resourceUnit) {
    ResourceRestraint resourceConstraint =
        resourceRestraintService.getByNameAndAccountId(PmsConstants.QUEUING_RC_NAME, accountId);
    if (resourceConstraint == null) {
      throw new InvalidRequestException(String.format(NOT_FOUND_WITH_ARGUMENTS, accountId));
    }

    List<ResourceRestraintInstance> instances =
        resourceRestraintInstanceService.getAllByRestraintIdAndResourceUnitAndStates(
            resourceConstraint.getUuid(), resourceUnit, Arrays.asList(ACTIVE, BLOCKED));
    instances.sort(Comparator.comparingInt(ResourceRestraintInstance::getOrder));

    Map<String, PlanExecution> planExecutionMap =
        planExecutionService
            .findAllByPlanExecutionIdIn(
                instances.stream().map(ResourceRestraintInstance::getReleaseEntityId).collect(Collectors.toList()))
            .stream()
            .collect(Collectors.toMap(PlanExecution::getUuid, Function.identity()));

    return ResourceConstraintExecutionInfoDTO.builder()
        .name(resourceConstraint.getName())
        .capacity(resourceConstraint.getCapacity())
        .resourceConstraints(createResourceConstraintDetails(instances, planExecutionMap))
        .build();
  }

  private List<ResourceConstraintDetailDTO> createResourceConstraintDetails(
      List<ResourceRestraintInstance> instances, Map<String, PlanExecution> planExecutionMap) {
    // SHORT LIVED PIPELINE ENTITY CACHE
    Map<String, PipelineEntity> cache = new HashMap<>();

    return instances.stream()
        .map(instance -> {
          PlanExecution planExecution = planExecutionMap.get(instance.getReleaseEntityId());
          return ResourceConstraintDetailDTO.builder()
              .pipelineName(getPipelineName(cache, planExecution))
              .pipelineIdentifier(planExecution.getMetadata().getPipelineIdentifier())
              .startTs(planExecution.getStartTs())
              .planExecutionId(instance.getReleaseEntityId())
              .state(instance.getState())
              .build();
        })
        .collect(Collectors.toList());
  }

  @VisibleForTesting
  public String getPipelineName(Map<String, PipelineEntity> cache, PlanExecution planExecution) {
    Map<String, String> setupAbstractions = planExecution.getSetupAbstractions();
    String accountId = setupAbstractions.get("accountId");
    String orgIdentifier = setupAbstractions.get("orgIdentifier");
    String projectIdentifier = setupAbstractions.get("projectIdentifier");
    String pipelineIdentifier = planExecution.getMetadata().getPipelineIdentifier();

    // TRYING TO REDUCE THE TIMES WE GO TO OTHER SERVICE TO GET PIPELINE ENTITY
    // CAN BE HELPFUL IF WE HAVE A BIG NUMBER OF RESOURCE RESTRAINT INSTANCES
    String cacheKey = String.format("%s_%s_%s_%s", accountId, orgIdentifier, projectIdentifier, pipelineIdentifier);
    return cache
        .computeIfAbsent(cacheKey,
            k
            -> pipelineService.get(accountId, orgIdentifier, projectIdentifier, pipelineIdentifier, false)
                   .orElseGet(() -> PipelineEntity.builder().build()))
        .getName();
  }
}
