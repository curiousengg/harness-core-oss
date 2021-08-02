package io.harness.cdng.service.beans;

import static io.harness.annotations.dev.HarnessTeam.CDC;

import io.harness.annotations.dev.OwnedBy;
import io.harness.beans.ParameterField;
import io.harness.beans.SwaggerConstants;
import io.harness.cdng.visitor.helpers.serviceconfig.ServiceEntityVisitorHelper;
import io.harness.walktree.visitor.SimpleVisitorHelper;
import io.harness.walktree.visitor.Visitable;

import io.swagger.annotations.ApiModelProperty;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Wither;

@Data
@Builder
@SimpleVisitorHelper(helperClass = ServiceEntityVisitorHelper.class)
@OwnedBy(CDC)
public class ServiceYaml implements Visitable {
  @NotNull @ApiModelProperty(dataType = SwaggerConstants.STRING_CLASSPATH) private String identifier;
  @NotNull @ApiModelProperty(dataType = SwaggerConstants.STRING_CLASSPATH) @Wither private String name;
  @ApiModelProperty(dataType = SwaggerConstants.STRING_CLASSPATH) @Wither private ParameterField<String> description;
  @Wither Map<String, String> tags;

  @Getter(onMethod_ = { @ApiModelProperty(hidden = true) }) @ApiModelProperty(hidden = true) String metadata;
}
