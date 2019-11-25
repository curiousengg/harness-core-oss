package io.harness.batch.processing.billing.writer;

import com.google.inject.Singleton;

import io.harness.batch.processing.billing.service.BillingCalculationService;
import io.harness.batch.processing.billing.service.BillingData;
import io.harness.batch.processing.billing.timeseries.data.InstanceBillingData;
import io.harness.batch.processing.billing.timeseries.service.impl.BillingDataServiceImpl;
import io.harness.batch.processing.ccm.CCMJobConstants;
import io.harness.batch.processing.entities.InstanceData;
import io.harness.batch.processing.writer.constants.InstanceMetaDataConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import software.wings.beans.instance.HarnessServiceInfo;

import java.time.Instant;
import java.util.List;

@Slf4j
@Singleton
public class InstanceBillingDataWriter implements ItemWriter<InstanceData> {
  @Autowired private BillingCalculationService billingCalculationService;
  @Autowired private BillingDataServiceImpl billingDataService;

  private JobParameters parameters;

  @BeforeStep
  public void beforeStep(final StepExecution stepExecution) {
    parameters = stepExecution.getJobExecution().getJobParameters();
  }

  public void write(List<? extends InstanceData> instanceDataList) throws Exception {
    Instant startTime = getFieldValueFromJobParams(CCMJobConstants.JOB_START_DATE);
    Instant endTime = getFieldValueFromJobParams(CCMJobConstants.JOB_END_DATE);
    logger.info("Instance data list {} {} {} {}", instanceDataList.size(), startTime, endTime, parameters.toString());
    instanceDataList.forEach(instanceData -> {
      BillingData billingData = billingCalculationService.getInstanceBillingAmount(instanceData, startTime, endTime);
      logger.info("Instance detail {} :: {} ", instanceData.getInstanceId(), billingData.getBillingAmount());
      HarnessServiceInfo harnessServiceInfo = getHarnessServiceInfo(instanceData);
      InstanceBillingData instanceBillingData =
          InstanceBillingData.builder()
              .accountId(instanceData.getAccountId())
              .settingId(instanceData.getSettingId())
              .clusterId(instanceData.getClusterId())
              .instanceType(instanceData.getInstanceType().toString())
              .billingAccountId("BILLING_ACCOUNT_ID")
              .startTimestamp(startTime.toEpochMilli())
              .endTimestamp(endTime.toEpochMilli())
              .billingAmount(billingData.getBillingAmount())
              .usageDurationSeconds(billingData.getUsageDurationSeconds())
              .instanceId(instanceData.getInstanceId())
              .clusterName(instanceData.getClusterName())
              .appId(harnessServiceInfo.getAppId())
              .serviceId(harnessServiceInfo.getServiceId())
              .cloudProviderId(harnessServiceInfo.getCloudProviderId())
              .envId(harnessServiceInfo.getEnvId())
              .cpuUnitSeconds(billingData.getCpuUnitSeconds())
              .memoryMbSeconds(billingData.getMemoryMbSeconds())
              .parentInstanceId(
                  getValueForKeyFromInstanceMetaData(InstanceMetaDataConstants.PARENT_RESOURCE_ID, instanceData))
              .launchType(getValueForKeyFromInstanceMetaData(InstanceMetaDataConstants.LAUNCH_TYPE, instanceData))
              .namespace(getValueForKeyFromInstanceMetaData(InstanceMetaDataConstants.NAMESPACE, instanceData))
              .region(getValueForKeyFromInstanceMetaData(InstanceMetaDataConstants.REGION, instanceData))
              .clusterType(getValueForKeyFromInstanceMetaData(InstanceMetaDataConstants.CLUSTER_TYPE, instanceData))
              .cloudProvider(getValueForKeyFromInstanceMetaData(InstanceMetaDataConstants.CLOUD_PROVIDER, instanceData))
              .workloadName(getValueForKeyFromInstanceMetaData(InstanceMetaDataConstants.WORKLOAD_NAME, instanceData))
              .workloadType(getValueForKeyFromInstanceMetaData(InstanceMetaDataConstants.WORKLOAD_TYPE, instanceData))
              .build();
      billingDataService.create(instanceBillingData);
    });
  }

  HarnessServiceInfo getHarnessServiceInfo(InstanceData instanceData) {
    if (null != instanceData.getHarnessServiceInfo()) {
      return instanceData.getHarnessServiceInfo();
    }
    return new HarnessServiceInfo(null, null, null, null, null);
  }

  String getValueForKeyFromInstanceMetaData(String metaDataKey, InstanceData instanceData) {
    if (null != instanceData.getMetaData() && instanceData.getMetaData().containsKey(metaDataKey)) {
      return instanceData.getMetaData().get(metaDataKey);
    }
    return null;
  }

  private Instant getFieldValueFromJobParams(String fieldName) {
    return Instant.ofEpochMilli(Long.parseLong(parameters.getString(fieldName)));
  }
}
