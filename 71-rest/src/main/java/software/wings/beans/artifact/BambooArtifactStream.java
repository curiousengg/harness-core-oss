package software.wings.beans.artifact;

import static java.lang.String.format;
import static software.wings.beans.artifact.ArtifactStreamAttributes.Builder.anArtifactStreamAttributes;
import static software.wings.beans.artifact.ArtifactStreamType.BAMBOO;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.harness.beans.EmbeddedUser;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;
import software.wings.utils.Util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@JsonTypeName("BAMBOO")
@Data
@EqualsAndHashCode(callSuper = false)
public class BambooArtifactStream extends ArtifactStream {
  @NotEmpty private String jobname;
  @NotEmpty private List<String> artifactPaths;

  public BambooArtifactStream() {
    super(BAMBOO.name());
  }

  @Builder
  public BambooArtifactStream(String uuid, String appId, EmbeddedUser createdBy, long createdAt,
      EmbeddedUser lastUpdatedBy, long lastUpdatedAt, String entityYamlPath, String sourceName, String settingId,
      String name, boolean autoPopulate, String serviceId, boolean metadataOnly, String jobname,
      List<String> artifactPaths) {
    super(uuid, appId, createdBy, createdAt, lastUpdatedBy, lastUpdatedAt, entityYamlPath, BAMBOO.name(), sourceName,
        settingId, name, autoPopulate, serviceId, metadataOnly);
    this.jobname = jobname;
    this.artifactPaths = artifactPaths;
  }

  @Override
  public String fetchArtifactDisplayName(String buildNo) {
    return format("%s_%s_%s", getSourceName(), buildNo, new SimpleDateFormat(dateFormat).format(new Date()));
  }

  @Override
  public String generateName() {
    return Util.normalize(generateSourceName());
  }

  @Override
  public String generateSourceName() {
    return getJobname();
  }

  @Override
  public ArtifactStreamAttributes fetchArtifactStreamAttributes() {
    return anArtifactStreamAttributes().withArtifactStreamType(getArtifactStreamType()).withJobName(jobname).build();
  }

  @Data
  @NoArgsConstructor
  @EqualsAndHashCode(callSuper = true)
  public static class Yaml extends ArtifactStream.Yaml {
    private String planName;
    private List<String> artifactPaths;

    @lombok.Builder
    public Yaml(String harnessApiVersion, String serverName, boolean metadataOnly, String planName,
        List<String> artifactPaths) {
      super(BAMBOO.name(), harnessApiVersion, serverName, metadataOnly);
      this.planName = planName;
      this.artifactPaths = artifactPaths;
    }
  }
}
