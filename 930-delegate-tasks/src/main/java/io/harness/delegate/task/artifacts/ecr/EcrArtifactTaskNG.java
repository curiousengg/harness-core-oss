package io.harness.delegate.task.artifacts.ecr;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.delegate.beans.DelegateResponseData;
import io.harness.delegate.beans.DelegateTaskPackage;
import io.harness.delegate.beans.DelegateTaskResponse;
import io.harness.delegate.beans.logstreaming.ILogStreamingTaskClient;
import io.harness.delegate.task.AbstractDelegateRunnableTask;
import io.harness.delegate.task.TaskParameters;
import io.harness.delegate.task.artifacts.request.ArtifactTaskParameters;

import com.google.inject.Inject;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;

@Slf4j
@OwnedBy(HarnessTeam.PIPELINE)
public class EcrArtifactTaskNG extends AbstractDelegateRunnableTask {
  @Inject EcrArtifactTaskHelper ecrArtifactTaskHelper;
  public EcrArtifactTaskNG(DelegateTaskPackage delegateTaskPackage, ILogStreamingTaskClient logStreamingTaskClient,
      Consumer<DelegateTaskResponse> consumer, BooleanSupplier preExecute) {
    super(delegateTaskPackage, logStreamingTaskClient, consumer, preExecute);
  }
  @Override
  public DelegateResponseData run(Object[] parameters) {
    throw new NotImplementedException("not implemented");
  }

  @Override
  public boolean isSupportingErrorFramework() {
    return true;
  }

  @Override
  public DelegateResponseData run(TaskParameters parameters) {
    ArtifactTaskParameters taskParameters = (ArtifactTaskParameters) parameters;
    return ecrArtifactTaskHelper.getArtifactCollectResponse(taskParameters);
  }
}
