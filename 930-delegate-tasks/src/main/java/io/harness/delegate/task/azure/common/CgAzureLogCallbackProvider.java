/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.delegate.task.azure.common;

import static io.harness.annotations.dev.HarnessTeam.CDP;

import io.harness.annotations.dev.OwnedBy;
import io.harness.delegate.beans.logstreaming.ILogStreamingTaskClient;
import io.harness.logging.LogCallback;

@OwnedBy(CDP)
public class CgAzureLogCallbackProvider implements AzureLogCallbackProvider {
  private final ILogStreamingTaskClient logStreamingTaskClient;

  CgAzureLogCallbackProvider(ILogStreamingTaskClient logStreamingTaskClient) {
    this.logStreamingTaskClient = logStreamingTaskClient;
  }

  @Override
  public LogCallback obtainLogCallback(String commandUnitName) {
    return logStreamingTaskClient.obtainLogCallback(commandUnitName);
  }
}
