package software.wings.graphql.schema.type.user;

import io.harness.utils.RequestField;

import software.wings.graphql.schema.mutation.QLMutationInput;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class QLUpdateUserInput implements QLMutationInput {
  private String id;
  private String clientMutationId;
  private RequestField<String> name;
  private RequestField<List<String>> userGroupIds;
}
