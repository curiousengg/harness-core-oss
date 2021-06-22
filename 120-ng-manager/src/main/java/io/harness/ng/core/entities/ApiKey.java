package io.harness.ng.core.entities;

import io.harness.annotation.HarnessEntity;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.mongo.index.CompoundMongoIndex;
import io.harness.mongo.index.MongoIndex;
import io.harness.ng.core.NGAccountAccess;
import io.harness.ng.core.NGOrgAccess;
import io.harness.ng.core.NGProjectAccess;
import io.harness.ng.core.common.beans.ApiKeyType;
import io.harness.persistence.PersistentEntity;
import io.harness.persistence.UuidAware;

import com.google.common.collect.ImmutableList;
import java.time.Duration;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.experimental.FieldNameConstants;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@FieldNameConstants(innerTypeName = "ApiKeyKeys")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(value = "ngApiKeys", noClassnameStored = true)
@Document("ngApiKeys")
@TypeAlias("ngApiKeys")
@HarnessEntity(exportable = true)
@OwnedBy(HarnessTeam.PL)
public class ApiKey implements PersistentEntity, UuidAware, NGAccountAccess, NGOrgAccess, NGProjectAccess {
  public static long DEFAULT_TTL_FOR_TOKEN = Duration.ofDays(30).toMillis();

  public static List<MongoIndex> mongoIndexes() {
    return ImmutableList.<MongoIndex>builder()
        .add(CompoundMongoIndex.builder()
                 .name("unique_idx")
                 .field(ApiKeyKeys.accountIdentifier)
                 .field(ApiKeyKeys.orgIdentifier)
                 .field(ApiKeyKeys.projectIdentifier)
                 .field(ApiKeyKeys.identifier)
                 .field(ApiKeyKeys.parentIdentifier)
                 .unique(true)
                 .build(),
            CompoundMongoIndex.builder()
                .name("list_keys_idx")
                .field(ApiKeyKeys.accountIdentifier)
                .field(ApiKeyKeys.orgIdentifier)
                .field(ApiKeyKeys.projectIdentifier)
                .field(ApiKeyKeys.parentIdentifier)
                .build())
        .build();
  }

  @org.springframework.data.annotation.Id @Id String uuid;
  @CreatedDate Long createdAt;
  @LastModifiedDate Long lastModifiedAt;

  String accountIdentifier;
  String orgIdentifier;
  String projectIdentifier;

  String identifier;
  String parentIdentifier;
  ApiKeyType apiKeyType;
  @Builder.Default long defaultTimeToExpireToken = DEFAULT_TTL_FOR_TOKEN;
}
