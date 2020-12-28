package io.harness.steps;

import static io.harness.data.structure.EmptyPredicate.isNotEmpty;

import static java.util.stream.Collectors.toList;

import io.harness.data.algorithm.HashGenerator;
import io.harness.delegate.Capability;
import io.harness.delegate.TaskDetails;
import io.harness.delegate.TaskLogAbstractions;
import io.harness.delegate.TaskMode;
import io.harness.delegate.TaskSetupAbstractions;
import io.harness.delegate.TaskType;
import io.harness.delegate.beans.TaskData;
import io.harness.delegate.beans.executioncapability.ExecutionCapability;
import io.harness.delegate.beans.executioncapability.ExecutionCapabilityDemander;
import io.harness.delegate.task.SimpleHDelegateTask;
import io.harness.delegate.task.TaskParameters;
import io.harness.pms.contracts.ambiance.Ambiance;
import io.harness.pms.contracts.ambiance.Level;
import io.harness.pms.contracts.execution.Status;
import io.harness.pms.contracts.execution.tasks.DelegateTaskRequest;
import io.harness.pms.contracts.execution.tasks.TaskCategory;
import io.harness.pms.contracts.execution.tasks.TaskRequest;
import io.harness.pms.sdk.core.steps.io.StepResponse;
import io.harness.pms.sdk.core.steps.io.StepResponse.StepResponseBuilder;
import io.harness.pms.sdk.core.steps.io.StepResponseNotifyData;
import io.harness.serializer.KryoSerializer;
import io.harness.tasks.Cd1SetupFields;
import io.harness.tasks.ResponseData;
import io.harness.tasks.Task;

import com.google.common.base.Preconditions;
import com.google.protobuf.ByteString;
import com.google.protobuf.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;

public class StepUtils {
  private StepUtils() {}

  public static StepResponse createStepResponseFromChildResponse(Map<String, ResponseData> responseDataMap) {
    StepResponseBuilder responseBuilder = StepResponse.builder().status(Status.SUCCEEDED);
    for (ResponseData responseData : responseDataMap.values()) {
      Status executionStatus = ((StepResponseNotifyData) responseData).getStatus();
      if (executionStatus != Status.SUCCEEDED && executionStatus != Status.SUSPENDED) {
        responseBuilder.status(executionStatus);
      }
    }
    return responseBuilder.build();
  }

  public static Task prepareDelegateTaskInput(String accountId, TaskData taskData,
      Map<String, String> setupAbstractions, LinkedHashMap<String, String> logAbstractions) {
    return createHDelegateTask(accountId, taskData, setupAbstractions, logAbstractions);
  }

  public static Task prepareDelegateTaskInput(
      String accountId, TaskData taskData, Map<String, String> setupAbstractions) {
    return createHDelegateTask(accountId, taskData, setupAbstractions, new LinkedHashMap<>());
  }

  private static Task createHDelegateTask(String accountId, TaskData taskData, Map<String, String> setupAbstractions,
      LinkedHashMap<String, String> logAbstractions) {
    return SimpleHDelegateTask.builder()
        .accountId(accountId)
        .data(taskData)
        .setupAbstractions(setupAbstractions)
        .logStreamingAbstractions(logAbstractions)
        .build();
  }

  @Nonnull
  public static LinkedHashMap<String, String> generateLogAbstractions(Ambiance ambiance) {
    LinkedHashMap<String, String> logAbstractions = new LinkedHashMap<>();
    logAbstractions.put("pipelineExecutionId", ambiance.getPlanExecutionId());
    Level stageLevel = ambiance.getLevels(2);
    logAbstractions.put("stageIdentifier", stageLevel.getIdentifier());
    Level currentStepLevel = ambiance.getLevels(ambiance.getLevelsCount() - 1);
    logAbstractions.put("stepId", currentStepLevel.getRuntimeId());

    return logAbstractions;
  }

  public static TaskRequest prepareTaskRequest(Ambiance ambiance, TaskData taskData, KryoSerializer kryoSerializer) {
    return prepareTaskRequest(ambiance, taskData, kryoSerializer, new LinkedHashMap<>(), TaskCategory.DELEGATE_TASK_V2);
  }

  public static TaskRequest prepareTaskRequest(Ambiance ambiance, TaskData taskData, KryoSerializer kryoSerializer,
      LinkedHashMap<String, String> logAbstractions, TaskCategory taskCategory) {
    String accountId = Preconditions.checkNotNull(ambiance.getSetupAbstractionsMap().get("accountId"));
    TaskParameters taskParameters = (TaskParameters) taskData.getParameters()[0];
    List<ExecutionCapability> capabilities = new ArrayList<>();
    if (taskParameters instanceof ExecutionCapabilityDemander) {
      capabilities = ListUtils.emptyIfNull(
          ((ExecutionCapabilityDemander) taskParameters).fetchRequiredExecutionCapabilities(null));
    }
    DelegateTaskRequest.Builder requestBuilder =
        DelegateTaskRequest.newBuilder()
            .setAccountId(accountId)
            .setDetails(
                TaskDetails.newBuilder()
                    .setKryoParameters(ByteString.copyFrom(kryoSerializer.asDeflatedBytes(taskParameters) == null
                            ? new byte[] {}
                            : kryoSerializer.asDeflatedBytes(taskParameters)))
                    .setExecutionTimeout(Duration.newBuilder().setSeconds(taskData.getTimeout() * 1000).build())
                    // TODO : Change this spmehow and obtain from ambiance
                    .setExpressionFunctorToken(HashGenerator.generateIntegerHash())
                    .setMode(taskData.isAsync() ? TaskMode.ASYNC : TaskMode.SYNC)
                    .setParked(taskData.isParked())
                    .setType(TaskType.newBuilder().setType(taskData.getTaskType()).build())
                    .build())
            .setLogAbstractions(TaskLogAbstractions.newBuilder()
                                    .putAllValues(logAbstractions)
                                    .putValues(Cd1SetupFields.APP_ID_FIELD, accountId)
                                    .build())
            .setSetupAbstractions(TaskSetupAbstractions.newBuilder()
                                      .putAllValues((MapUtils.emptyIfNull(ambiance.getSetupAbstractionsMap())))
                                      .build());

    if (isNotEmpty(capabilities)) {
      requestBuilder.addAllCapabilities(
          capabilities.stream()
              .map(capability
                  -> Capability.newBuilder()
                         .setKryoCapability(ByteString.copyFrom(kryoSerializer.asDeflatedBytes(capability) == null
                                 ? new byte[] {}
                                 : kryoSerializer.asDeflatedBytes(capability)))
                         .build())
              .collect(toList()));
    }

    return TaskRequest.newBuilder()
        .setDelegateTaskRequest(requestBuilder.build())
        .setTaskCategory(taskCategory)
        .build();
  }
}
