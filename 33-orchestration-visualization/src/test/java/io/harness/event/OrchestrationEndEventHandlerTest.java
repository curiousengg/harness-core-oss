package io.harness.event;

import static io.harness.data.structure.UUIDGenerator.generateUuid;
import static io.harness.execution.events.OrchestrationEventType.ORCHESTRATION_END;
import static io.harness.rule.OwnerRule.ALEXEI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.harness.OrchestrationVisualizationTestBase;
import io.harness.ambiance.Ambiance;
import io.harness.beans.OrchestrationGraph;
import io.harness.cache.SpringMongoStore;
import io.harness.category.element.UnitTests;
import io.harness.engine.executions.plan.PlanExecutionService;
import io.harness.exception.InvalidRequestException;
import io.harness.execution.PlanExecution;
import io.harness.execution.events.OrchestrationEvent;
import io.harness.pms.execution.Status;
import io.harness.rule.Owner;
import io.harness.service.GraphGenerationService;
import io.harness.testlib.RealMongo;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Test class for {@link OrchestrationEndEventHandler}
 */
public class OrchestrationEndEventHandlerTest extends OrchestrationVisualizationTestBase {
  @Inject private SpringMongoStore mongoStore;

  @Inject PlanExecutionService planExecutionService;
  @Inject GraphGenerationService graphGenerationService;
  @Inject OrchestrationEndEventHandler orchestrationEndEventHandler;

  @Test
  @Owner(developers = ALEXEI)
  @Category(UnitTests.class)
  @RealMongo
  public void shouldThrowInvalidRequestException() {
    String planExecutionId = generateUuid();
    OrchestrationEvent event = OrchestrationEvent.builder()
                                   .ambiance(Ambiance.builder().planExecutionId(planExecutionId).build())
                                   .eventType(ORCHESTRATION_END)
                                   .build();

    assertThatThrownBy(() -> orchestrationEndEventHandler.handleEvent(event))
        .isInstanceOf(InvalidRequestException.class);
  }

  @Test
  @Owner(developers = ALEXEI)
  @Category(UnitTests.class)
  @RealMongo
  public void shouldUpdateGraphWithStatusAndEndTs() {
    PlanExecution planExecution = PlanExecution.builder()
                                      .uuid(generateUuid())
                                      .startTs(System.currentTimeMillis())
                                      .endTs(System.currentTimeMillis())
                                      .status(Status.SUCCEEDED)
                                      .build();
    planExecutionService.save(planExecution);

    OrchestrationEvent event = OrchestrationEvent.builder()
                                   .ambiance(Ambiance.builder().planExecutionId(planExecution.getUuid()).build())
                                   .eventType(ORCHESTRATION_END)
                                   .build();

    OrchestrationGraph orchestrationGraph = OrchestrationGraph.builder()
                                                .rootNodeIds(Lists.newArrayList(generateUuid()))
                                                .status(Status.RUNNING)
                                                .startTs(planExecution.getStartTs())
                                                .planExecutionId(planExecution.getUuid())
                                                .cacheKey(planExecution.getUuid())
                                                .cacheContextOrder(System.currentTimeMillis())
                                                .build();
    mongoStore.upsert(orchestrationGraph, Duration.ofDays(10));

    orchestrationEndEventHandler.handleEvent(event);

    Awaitility.await().atMost(2, TimeUnit.SECONDS).pollInterval(500, TimeUnit.MILLISECONDS).until(() -> {
      OrchestrationGraph graphInternal = graphGenerationService.getCachedOrchestrationGraph(planExecution.getUuid());
      return graphInternal.getStatus() == Status.SUCCEEDED;
    });

    OrchestrationGraph updatedGraph = graphGenerationService.getCachedOrchestrationGraph(planExecution.getUuid());

    assertThat(updatedGraph).isNotNull();
    assertThat(updatedGraph.getPlanExecutionId()).isEqualTo(planExecution.getUuid());
    assertThat(updatedGraph.getStartTs()).isEqualTo(planExecution.getStartTs());
    assertThat(updatedGraph.getEndTs()).isEqualTo(planExecution.getEndTs());
    assertThat(updatedGraph.getStatus()).isEqualTo(planExecution.getStatus());
  }
}
