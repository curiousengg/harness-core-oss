package io.harness.governance;

import io.harness.governance.EnvironmentFilter.EnvironmentFilterType;
import io.harness.yaml.BaseYaml;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME, property = "filterType", include = JsonTypeInfo.As.EXISTING_PROPERTY, visible = true)
@JsonSubTypes({
  @JsonSubTypes.Type(value = CustomEnvFilter.Yaml.class, name = "CUSTOM")
  , @JsonSubTypes.Type(value = AllEnvFilterYaml.class, name = "ALL"),
      @JsonSubTypes.Type(value = AllProdEnvFilterYaml.class, name = "ALL_PROD"),
      @JsonSubTypes.Type(value = AllNonProdEnvFilterYaml.class, name = "ALL_NON_PROD")
})
public abstract class EnvironmentFilterYaml extends BaseYaml {
  private EnvironmentFilterType filterType;

  public EnvironmentFilterYaml(@JsonProperty("filterType") EnvironmentFilterType filterType) {
    this.filterType = filterType;
  }
}