package io.harness.cdng.manifest.yaml;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.harness.cdng.manifest.ManifestType;
import io.harness.cdng.manifest.yaml.kinds.K8sManifest;
import io.harness.cdng.manifest.yaml.kinds.ValuesManifest;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonTypeName("manifest")
public class ManifestConfig implements ManifestConfigWrapper {
  String identifier;
  @JsonIgnore ManifestAttributes manifestAttributes;

  @JsonProperty(ManifestType.K8Manifest)
  public void setK8Manifest(K8sManifest k8Manifest) {
    k8Manifest.setIdentifier(identifier);
    k8Manifest.setKind(ManifestType.K8Manifest);
    this.manifestAttributes = k8Manifest;
  }

  @JsonProperty(ManifestType.VALUES)
  public void setValuesManifest(ValuesManifest valuesManifest) {
    valuesManifest.setIdentifier(identifier);
    valuesManifest.setKind(ManifestType.VALUES);
    this.manifestAttributes = valuesManifest;
  }
}