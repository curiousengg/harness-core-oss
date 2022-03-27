/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package io.harness.steps.shellscript;

import static io.harness.beans.SwaggerConstants.BOOLEAN_CLASSPATH;
import static io.harness.yaml.schema.beans.SupportedPossibleFieldTypes.runtime;
import static io.harness.yaml.schema.beans.SupportedPossibleFieldTypes.string;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.beans.SwaggerConstants;
import io.harness.plancreator.steps.TaskSelectorYaml;
import io.harness.pms.yaml.ParameterField;
import io.harness.yaml.YamlSchemaTypes;

import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

@OwnedBy(HarnessTeam.CDC)
@Data
@NoArgsConstructor
@AllArgsConstructor
@TypeAlias("ShellScriptBaseStepInfo")
public class ShellScriptBaseStepInfo {
  @NotNull ShellType shell;
  @NotNull ShellScriptSourceWrapper source;
  ExecutionTarget executionTarget;
  @NotNull
  @ApiModelProperty(dataType = BOOLEAN_CLASSPATH)
  @YamlSchemaTypes({string})
  ParameterField<Boolean> onDelegate;
  @ApiModelProperty(dataType = SwaggerConstants.STRING_LIST_CLASSPATH)
  @YamlSchemaTypes(value = {runtime})
  ParameterField<List<TaskSelectorYaml>> delegateSelectors;
}
