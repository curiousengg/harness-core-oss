package io.harness.engine.interrupts;

import static io.harness.annotations.dev.HarnessTeam.PIPELINE;
import static io.harness.data.structure.EmptyPredicate.isEmpty;
import static io.harness.interrupts.Interrupt.State;
import static io.harness.interrupts.Interrupt.State.PROCESSING;
import static io.harness.interrupts.Interrupt.State.REGISTERED;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import io.harness.annotations.dev.OwnedBy;
import io.harness.engine.ExecutionCheck;
import io.harness.engine.executions.node.NodeExecutionService;
import io.harness.engine.interrupts.handlers.PauseAllInterruptHandler;
import io.harness.engine.interrupts.handlers.ResumeAllInterruptHandler;
import io.harness.exception.InvalidRequestException;
import io.harness.execution.ExecutionModeUtils;
import io.harness.execution.NodeExecution;
import io.harness.interrupts.Interrupt;
import io.harness.interrupts.Interrupt.InterruptKeys;
import io.harness.pms.contracts.interrupts.InterruptType;
import io.harness.repositories.InterruptRepository;

import com.google.inject.Inject;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@OwnedBy(PIPELINE)
@Slf4j
public class InterruptServiceImpl implements InterruptService {
  @Inject private InterruptRepository interruptRepository;
  @Inject private MongoTemplate mongoTemplate;
  @Inject private PauseAllInterruptHandler pauseAllInterruptHandler;
  @Inject private ResumeAllInterruptHandler resumeAllInterruptHandler;
  @Inject private NodeExecutionService nodeExecutionService;

  @Override
  public Interrupt get(String interruptId) {
    return interruptRepository.findById(interruptId)
        .orElseThrow(() -> new InvalidRequestException("Interrupt Not found for id: " + interruptId));
  }

  @Override
  public Interrupt save(Interrupt interrupt) {
    return interruptRepository.save(interrupt);
  }

  @Override
  public ExecutionCheck checkInterruptsPreInvocation(String planExecutionId, String nodeExecutionId) {
    List<Interrupt> interrupts = fetchActivePlanLevelInterrupts(planExecutionId);
    if (isEmpty(interrupts)) {
      return ExecutionCheck.builder().proceed(true).reason("[InterruptCheck] No Interrupts Found").build();
    }
    if (interrupts.size() > 1) {
      throw new InvalidRequestException("More than 2 active Plan Level Interrupts Present: "
          + interrupts.stream().map(interrupt -> interrupt.getType().toString()).collect(Collectors.joining("|")));
    }
    Interrupt interrupt = interrupts.get(0);

    switch (interrupt.getType()) {
      case PAUSE_ALL:
        if (pauseRequired(interrupt, nodeExecutionId)) {
          pauseAllInterruptHandler.handleInterruptForNodeExecution(interrupt, nodeExecutionId);
          return ExecutionCheck.builder().proceed(false).reason("[InterruptCheck] PAUSE_ALL interrupt found").build();
        }
        return ExecutionCheck.builder().proceed(true).reason("[InterruptCheck] No Interrupts Found").build();
      case RESUME_ALL:
        resumeAllInterruptHandler.handleInterruptForNodeExecution(interrupt, nodeExecutionId);
        return ExecutionCheck.builder().proceed(true).reason("[InterruptCheck] RESUME_ALL interrupt found").build();
      default:
        throw new InvalidRequestException("No Handler Present for interrupt type: " + interrupt.getType());
    }
  }

  private boolean pauseRequired(Interrupt interrupt, String nodeExecutionId) {
    NodeExecution nodeExecution = nodeExecutionService.get(nodeExecutionId);

    // Only Pausing leaf steps, It makes sense to let the execution flow to a leaf step and pause there
    // There is no pint pausing on parent (wrapper) steps (like stages/stage). More aesthetic for the execution graph
    // too
    if (ExecutionModeUtils.isParentMode(nodeExecution.getMode())) {
      return false;
    }

    // This is for PAUSE_ALL interrupt. If PAUSE ALL you have to pause for any node
    if (interrupt.getNodeExecutionId() == null) {
      return true;
    }

    // This case is for stage level PAUSE
    // Find All children for the stage (nodeExecutionId in interrupt) and check if the starting node is one of these. If
    // yes Pause the execution
    List<NodeExecution> targetExecutions =
        nodeExecutionService.findAllChildren(interrupt.getPlanExecutionId(), interrupt.getNodeExecutionId(), true);
    return targetExecutions.stream().anyMatch(ne -> ne.getUuid().equals(nodeExecutionId));
  }

  @Override
  public List<Interrupt> fetchActivePlanLevelInterrupts(String planExecutionId) {
    return interruptRepository.findByPlanExecutionIdAndStateInAndTypeInOrderByCreatedAtDesc(planExecutionId,
        EnumSet.of(REGISTERED, PROCESSING), EnumSet.of(InterruptType.PAUSE_ALL, InterruptType.RESUME_ALL));
  }

  @Override
  public Interrupt markProcessed(String interruptId, State finalState) {
    return updateInterruptState(interruptId, finalState);
  }

  @Override
  public Interrupt markProcessing(String interruptId) {
    return updateInterruptState(interruptId, PROCESSING);
  }

  private Interrupt updateInterruptState(String interruptId, State interruptState) {
    Update updateOps = new Update()
                           .set(InterruptKeys.state, interruptState)
                           .set(InterruptKeys.lastUpdatedAt, System.currentTimeMillis());
    Query query = query(where(InterruptKeys.uuid).is(interruptId));
    Interrupt seizedInterrupt = mongoTemplate.findAndModify(
        query, updateOps, new FindAndModifyOptions().upsert(false).returnNew(true), Interrupt.class);
    if (seizedInterrupt == null) {
      throw new InvalidRequestException("Cannot seize the interrupt {} with id :" + interruptId);
    }
    return seizedInterrupt;
  }

  @Override
  public List<Interrupt> fetchAllInterrupts(String planExecutionId) {
    return interruptRepository.findByPlanExecutionIdOrderByCreatedAtDesc(planExecutionId);
  }

  @Override
  public List<Interrupt> fetchActiveInterrupts(String planExecutionId) {
    return interruptRepository.findByPlanExecutionIdAndStateInOrderByCreatedAtDesc(
        planExecutionId, EnumSet.of(REGISTERED, PROCESSING));
  }

  @Override
  public List<Interrupt> fetchActiveInterruptsForNodeExecution(String planExecutionId, String nodeExecutionId) {
    return interruptRepository.findByPlanExecutionIdAndNodeExecutionIdAndStateInOrderByCreatedAtDesc(
        planExecutionId, nodeExecutionId, EnumSet.of(REGISTERED, PROCESSING));
  }
}
