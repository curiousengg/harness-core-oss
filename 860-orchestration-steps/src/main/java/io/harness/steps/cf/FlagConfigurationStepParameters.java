package io.harness.steps.cf;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.beans.ParameterField;
import io.harness.beans.SwaggerConstants;
import io.harness.plancreator.steps.common.SpecParameters;

import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import org.springframework.data.annotation.TypeAlias;

@OwnedBy(HarnessTeam.CF)
@Value
@Builder
@TypeAlias("flagConfigurationStepParameters")
public class FlagConfigurationStepParameters implements SpecParameters {
  String type = "FlagConfiguration";
  String identifier;
  String name;
  @NotNull @ApiModelProperty(dataType = SwaggerConstants.STRING_CLASSPATH) ParameterField<String> feature;
  @NotNull @ApiModelProperty(dataType = SwaggerConstants.STRING_CLASSPATH) ParameterField<String> environment;

  List<PatchInstruction> instructions;

  @NotNull @ApiModelProperty(dataType = SwaggerConstants.STRING_CLASSPATH) ParameterField<String> state;
}
