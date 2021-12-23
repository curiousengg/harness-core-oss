package io.harness.gitsync.fullsync.dtos;

import io.harness.gitsync.sdk.GitSyncApiConstants;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@Schema(name = "GitFullSyncConfigRequest", description = "This contains details to trigger Full Sync")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GitFullSyncConfigRequestDTO {
  @Schema(description = "Branch on which Entities will be pushed") @NotNull String branch;
  @Schema(description = "PR Title") String message;
  @Schema(description = "Source Branch for pull request") String baseBranch;
  @Schema(description = "This checks whether to create a pull request. Its default value is False")
  boolean createPullRequest;
  @Schema(description = GitSyncApiConstants.REPOID_PARAM_MESSAGE) @NotNull String repoIdentifier;
}
