package software.wings.graphql.schema.mutation.cloudProvider.k8s;

import io.harness.utils.RequestField;

import software.wings.graphql.schema.type.cloudProvider.k8s.QLClusterDetailsType;
import software.wings.security.PermissionAttribute;
import software.wings.security.annotations.Scope;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Scope(PermissionAttribute.ResourceType.SETTING)
@JsonIgnoreProperties(ignoreUnknown = true)
public class QLK8sCloudProviderInput {
  private RequestField<String> name;

  private RequestField<QLClusterDetailsType> clusterDetailsType;
  private RequestField<QLInheritClusterDetails> inheritClusterDetails;
  private RequestField<QLManualClusterDetails> manualClusterDetails;

  private RequestField<Boolean> skipValidation;
}
