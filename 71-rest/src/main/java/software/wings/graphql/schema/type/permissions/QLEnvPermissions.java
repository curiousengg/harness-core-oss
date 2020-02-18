package software.wings.graphql.schema.type.permissions;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import software.wings.graphql.schema.type.QLEnvFilterType;

import java.util.Set;

@Value
@Builder
@FieldNameConstants(innerTypeName = "QLEnvPermissionsKeys")
public class QLEnvPermissions {
  private Set<QLEnvFilterType> filterTypes;
  private Set<String> envIds;
}
