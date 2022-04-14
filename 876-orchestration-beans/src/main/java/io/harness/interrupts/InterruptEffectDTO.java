/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.interrupts;

import io.harness.pms.contracts.interrupts.InterruptType;

import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class InterruptEffectDTO {
  @NotNull String interruptId;
  @NotNull long tookEffectAt;
  @NotNull InterruptType interruptType;
  @NotNull InterruptConfig interruptConfig;
}