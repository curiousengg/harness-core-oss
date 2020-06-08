package io.harness.cdng.artifact.bean.artifactsource;

import com.github.reinert.jjschema.SchemaIgnore;
import io.harness.annotation.HarnessEntity;
import io.harness.beans.EmbeddedUser;
import io.harness.persistence.AccountAccess;
import io.harness.persistence.CreatedAtAware;
import io.harness.persistence.CreatedByAware;
import io.harness.persistence.PersistentEntity;
import io.harness.persistence.UpdatedAtAware;
import io.harness.persistence.UpdatedByAware;
import io.harness.persistence.UuidAware;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Indexes;

import javax.validation.constraints.NotNull;

/**
 * Artifacts Streams like Docker Hub, Nexus, GCR, etc.
 * This is the resolved artifact stream, can be used for Artifact Collection, etc.has
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Indexes({ @Index(options = @IndexOptions(name = "uniqueHash", unique = true), fields = { @Field("uniqueHash") }) })
@FieldNameConstants(innerTypeName = "ArtifactSourceKeys")
@Entity(value = "artifactSourceNG")
@HarnessEntity(exportable = true)
public abstract class ArtifactSource implements PersistentEntity, UuidAware, CreatedAtAware, CreatedByAware,
                                                UpdatedAtAware, UpdatedByAware, AccountAccess {
  @Id private String uuid;
  @NotNull private String accountId;
  @SchemaIgnore private EmbeddedUser createdBy;
  @SchemaIgnore @Indexed private long createdAt;
  @SchemaIgnore private EmbeddedUser lastUpdatedBy;
  @SchemaIgnore @NotNull private long lastUpdatedAt;
  /** It gives the artifact source type.*/
  @NotNull private String sourceType;
  /** This uniquely identifies one artifact stream based on its parameters.*/
  @NotNull private String uniqueHash;
}
