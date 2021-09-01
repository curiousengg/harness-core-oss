package io.harness.cdng.k8s;

import static io.harness.annotations.dev.HarnessTeam.CDP;
import static io.harness.beans.SwaggerConstants.BOOLEAN_CLASSPATH;
import static io.harness.yaml.schema.beans.SupportedPossibleFieldTypes.string;

import io.harness.annotations.dev.OwnedBy;
import io.harness.delegate.task.k8s.DeleteResourcesType;
import io.harness.pms.yaml.ParameterField;
import io.harness.yaml.YamlSchemaTypes;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

@OwnedBy(CDP)
@Data
@JsonTypeName("ReleaseName")
@FieldNameConstants(innerTypeName = "DeleteReleaseNameSpecKeys")
public class DeleteReleaseNameSpec implements DeleteResourcesBaseSpec {
  @ApiModelProperty(dataType = BOOLEAN_CLASSPATH) @YamlSchemaTypes({string}) ParameterField<Boolean> deleteNamespace;

  @Override
  public DeleteResourcesType getType() {
    return DeleteResourcesType.ReleaseName;
  }

  @Override
  public String getResourceNamesValue() {
    return "";
  }

  @Override
  public String getManifestPathsValue() {
    return "";
  }

  @Override
  public Boolean getAllManifestPathsValue() {
    return Boolean.FALSE;
  }

  @Override
  public ParameterField<Boolean> getDeleteNamespaceParameterField() {
    return deleteNamespace;
  }
}
