package software.wings.sm.states;

import static software.wings.beans.SearchFilter.Operator.EQ;
import static software.wings.beans.SearchFilter.Operator.EXISTS;
import static software.wings.beans.SearchFilter.Operator.NOT_EQ;
import static software.wings.dl.PageRequest.Builder.aPageRequest;

import com.google.inject.Inject;

import org.mongodb.morphia.annotations.Transient;
import software.wings.beans.Activity;
import software.wings.beans.artifact.Artifact;
import software.wings.dl.PageResponse;
import software.wings.service.intfc.ArtifactService;
import software.wings.sm.ExecutionStatus;
import software.wings.sm.StateType;
import software.wings.sm.WorkflowStandardParams;

public class AwsLambdaRollback extends AwsLambdaState {
  @Inject @Transient protected transient ArtifactService artifactService;

  public AwsLambdaRollback(String name) {
    super(name, StateType.AWS_LAMBDA_ROLLBACK.name());
  }

  @Override
  protected Artifact getArtifact(
      String appId, String serviceId, String workflowExecutionId, WorkflowStandardParams workflowStandardParams) {
    PageResponse<Activity> pageResponse =
        activityService.list(aPageRequest()
                                 .withLimit("1")
                                 .addFilter("appId", EQ, appId)
                                 .addFilter("serviceId", EQ, serviceId)
                                 .addFilter("status", EQ, ExecutionStatus.SUCCESS)
                                 .addFilter("workflowExecutionId", NOT_EQ, workflowExecutionId)
                                 .addFilter("artifactId", EXISTS)
                                 .build());
    if (pageResponse != null && !pageResponse.isEmpty()) {
      return artifactService.get(appId, pageResponse.getResponse().get(0).getArtifactId());
    }
    return null;
  }
}
