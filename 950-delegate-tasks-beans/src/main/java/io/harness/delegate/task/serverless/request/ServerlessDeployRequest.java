/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.delegate.task.serverless.request;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.delegate.beans.logstreaming.CommandUnitsProgress;
import io.harness.delegate.task.serverless.*;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
@OwnedBy(HarnessTeam.CDP)
public class ServerlessDeployRequest implements ServerlessCommandRequest {
  String accountId;
  ServerlessCommandType serverlessCommandType;
  String commandName;
  ServerlessArtifactConfig serverlessArtifactConfig;
  CommandUnitsProgress commandUnitsProgress;
  ServerlessManifestConfig serverlessManifestConfig;
  ServerlessInfraConfig serverlessInfraConfig;
  ServerlessDeployConfig serverlessDeployConfig;
  Integer timeoutIntervalInMin;
}