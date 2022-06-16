/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.delegate.task.azure.appservice;

import static io.harness.annotations.dev.HarnessTeam.CDP;
import static io.harness.azure.model.AzureConstants.SHIFT_TRAFFIC_SLOT_NAME_BLANK_ERROR_MSG;
import static io.harness.azure.model.AzureConstants.TRAFFIC_WEIGHT_IN_PERCENTAGE_INVALID_ERROR_MSG;
import static io.harness.azure.model.AzureConstants.WEB_APP_NAME_BLANK_ERROR_MSG;

import static org.apache.commons.lang3.StringUtils.isBlank;

import io.harness.annotations.dev.OwnedBy;
import io.harness.azure.model.AzureAppServiceApplicationSetting;
import io.harness.azure.model.AzureAppServiceConnectionString;
import io.harness.azure.model.AzureConfig;
import io.harness.delegate.beans.azure.registry.AzureRegistry;
import io.harness.delegate.beans.azure.registry.AzureRegistryFactory;
import io.harness.delegate.beans.azure.registry.AzureRegistryType;
import io.harness.delegate.beans.connector.ConnectorConfigDTO;
import io.harness.delegate.beans.connector.azureconnector.AzureContainerRegistryConnectorDTO;
import io.harness.delegate.task.azure.common.AzureContainerRegistryService;
import io.harness.exception.InvalidArgumentsException;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@OwnedBy(CDP)
@Singleton
public class AzureAppServiceResourceUtilities {
  @Inject private AzureContainerRegistryService azureContainerRegistryService;

  public Map<String, AzureAppServiceApplicationSetting> getAppSettingsToAdd(
      List<AzureAppServiceApplicationSetting> applicationSettings) {
    return applicationSettings.stream().collect(
        Collectors.toMap(AzureAppServiceApplicationSetting::getName, Function.identity()));
  }

  public Map<String, AzureAppServiceConnectionString> getConnectionSettingsToAdd(
      List<AzureAppServiceConnectionString> connectionStrings) {
    return connectionStrings.stream().collect(
        Collectors.toMap(AzureAppServiceConnectionString::getName, Function.identity()));
  }

  public Map<String, AzureAppServiceApplicationSetting> getDockerSettings(
      ConnectorConfigDTO connectorConfigDTO, AzureRegistryType azureRegistryType, AzureConfig azureConfig) {
    AzureRegistry azureRegistry = AzureRegistryFactory.getAzureRegistry(azureRegistryType);
    Map<String, AzureAppServiceApplicationSetting> dockerSettings =
        azureRegistry.getContainerSettings(connectorConfigDTO);

    if (AzureRegistryType.ACR == azureRegistryType) {
      azureContainerRegistryService.updateACRDockerSettingByCredentials(
          (AzureContainerRegistryConnectorDTO) connectorConfigDTO, azureConfig, dockerSettings);
    }

    return dockerSettings;
  }

  public void validateSlotShiftTrafficParameters(String webAppName, String deploymentSlot, double trafficPercent) {
    if (isBlank(webAppName)) {
      throw new InvalidArgumentsException(WEB_APP_NAME_BLANK_ERROR_MSG);
    }

    if (isBlank(deploymentSlot)) {
      throw new InvalidArgumentsException(SHIFT_TRAFFIC_SLOT_NAME_BLANK_ERROR_MSG);
    }

    if (trafficPercent > 100.0 || trafficPercent < 0) {
      throw new InvalidArgumentsException(TRAFFIC_WEIGHT_IN_PERCENTAGE_INVALID_ERROR_MSG);
    }
  }
}
