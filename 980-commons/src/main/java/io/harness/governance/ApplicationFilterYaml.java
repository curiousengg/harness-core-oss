package io.harness.governance;

import io.harness.yaml.BaseYaml;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME, property = "filterType", include = JsonTypeInfo.As.EXISTING_PROPERTY, visible = true)
@JsonSubTypes({
  @JsonSubTypes.Type(value = AllAppFilterYaml.class, name = "ALL")
  , @JsonSubTypes.Type(value = CustomAppFilter.Yaml.class, name = "CUSTOM")
})
public abstract class ApplicationFilterYaml extends BaseYaml {
  private List<EnvironmentFilterYaml> envSelection;
  private BlackoutWindowFilterType filterType;

  public ApplicationFilterYaml(@JsonProperty("filterType") BlackoutWindowFilterType filterType,
      @JsonProperty("envSelection") List<EnvironmentFilterYaml> envSelection) {
    this.filterType = filterType;
    this.envSelection = envSelection;
  }
}