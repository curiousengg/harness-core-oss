package software.wings.helpers.ext.k8s.request;

import static io.harness.annotations.dev.HarnessTeam.CDP;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.OwnedBy;
import io.harness.annotations.dev.TargetModule;
import io.harness.delegate.task.k8s.K8sTaskType;
import io.harness.k8s.model.HelmVersion;
import io.harness.k8s.model.KubernetesResourceId;

import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TargetModule(HarnessModule._950_DELEGATE_TASKS_BEANS)
@OwnedBy(CDP)
public class K8sRollingDeployRollbackTaskParameters extends K8sTaskParameters {
  Integer releaseNumber;
  List<KubernetesResourceId> prunedResourcesIds;
  boolean isPruningEnabled;
  @Builder
  public K8sRollingDeployRollbackTaskParameters(String accountId, String appId, String commandName, String activityId,
      K8sTaskType k8sTaskType, K8sClusterConfig k8sClusterConfig, String workflowExecutionId, String releaseName,
      Integer timeoutIntervalInMin, Integer releaseNumber, HelmVersion helmVersion, Set<String> delegateSelectors,
      List<KubernetesResourceId> prunedResourcesIds, boolean isPruningEnabled, boolean useLatestChartMuseumVersion) {
    super(accountId, appId, commandName, activityId, k8sClusterConfig, workflowExecutionId, releaseName,
        timeoutIntervalInMin, k8sTaskType, helmVersion, delegateSelectors, useLatestChartMuseumVersion);
    this.releaseNumber = releaseNumber;
    this.prunedResourcesIds = prunedResourcesIds;
    this.isPruningEnabled = isPruningEnabled;
  }
}
