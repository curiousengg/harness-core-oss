package io.harness.pms.pipeline;

import static io.harness.annotations.dev.HarnessTeam.PIPELINE;

import io.harness.annotation.HarnessEntity;
import io.harness.annotation.StoreIn;
import io.harness.annotations.dev.OwnedBy;
import io.harness.data.validator.Trimmed;
import io.harness.mongo.index.CompoundMongoIndex;
import io.harness.mongo.index.MongoIndex;
import io.harness.ng.DbAliases;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.Builder;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.NonFinal;
import org.hibernate.validator.constraints.NotEmpty;
import org.mongodb.morphia.annotations.Entity;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@OwnedBy(PIPELINE)
@Value
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldNameConstants(innerTypeName = "PipelineMetadataKeys")
@Entity(value = "pipelineMetadata", noClassnameStored = true)
@Document("pipelineMetadata")
@TypeAlias("pipelineMetadata")
@HarnessEntity(exportable = true)
@StoreIn(DbAliases.PMS)
public class PipelineMetadata {
  public static List<MongoIndex> mongoIndexes() {
    return ImmutableList.<MongoIndex>builder()
        .add(CompoundMongoIndex.builder()
                 .name("account_org_project_pipeline_idx")
                 .unique(true)
                 .field(PipelineMetadataKeys.accountId)
                 .field(PipelineMetadataKeys.orgIdentifier)
                 .field(PipelineMetadataKeys.projectIdentifier)
                 .field(PipelineMetadataKeys.pipelineIdentifier)
                 .build())
        .build();
  }

  @Setter @NonFinal @Id @org.mongodb.morphia.annotations.Id String uuid;
  @NotEmpty String accountId;
  @NotEmpty String orgIdentifier;
  @Trimmed @NotEmpty String projectIdentifier;
  @NotEmpty String pipelineIdentifier;

  ExecutionSummaryInfo executionSummaryInfo;
  int runSequence;
}
