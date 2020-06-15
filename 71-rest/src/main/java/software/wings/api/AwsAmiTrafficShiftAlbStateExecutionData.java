package software.wings.api;

import io.harness.delegate.beans.DelegateTaskNotifyResponseData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import software.wings.service.impl.aws.model.AwsConstants;
import software.wings.sm.StateExecutionData;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AwsAmiTrafficShiftAlbStateExecutionData
    extends StateExecutionData implements DelegateTaskNotifyResponseData {
  private String activityId;
  private String newAutoScalingGroupName;
  private String oldAutoScalingGroupName;
  private int newAutoscalingGroupWeight;

  @Override
  public Map<String, ExecutionDataValue> getExecutionSummary() {
    return getInternalExecutionDetails();
  }

  @Override
  public Map<String, ExecutionDataValue> getExecutionDetails() {
    return getInternalExecutionDetails();
  }

  private Map<String, ExecutionDataValue> getInternalExecutionDetails() {
    Map<String, ExecutionDataValue> executionDetails = super.getExecutionDetails();
    putNotNull(executionDetails, AwsConstants.VARIABLE_ACTIVITY_ID,
        ExecutionDataValue.builder().displayName(AwsConstants.DISPLAY_ACTIVITY_ID).value(activityId).build());

    if (oldAutoScalingGroupName != null) {
      putNotNull(executionDetails, AwsConstants.OLD_AUTOSCALING_GROUP,
          ExecutionDataValue.builder()
              .displayName(AwsConstants.OLD_AUTOSCALING_GROUP)
              .value(oldAutoScalingGroupName)
              .build());
    }
    if (newAutoScalingGroupName != null) {
      putNotNull(executionDetails, AwsConstants.NEW_AUTOSCALING_GROUP,
          ExecutionDataValue.builder()
              .displayName(AwsConstants.NEW_AUTOSCALING_GROUP)
              .value(newAutoScalingGroupName)
              .build());
    }
    putNotNull(executionDetails, AwsConstants.NEW_AUTOSCALING_GROUP_WEIGHT,
        ExecutionDataValue.builder()
            .value(newAutoscalingGroupWeight)
            .displayName(AwsConstants.NEW_AUTOSCALING_GROUP_WEIGHT)
            .build());
    return executionDetails;
  }
}
