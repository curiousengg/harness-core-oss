package io.harness.batch.processing.pricing.service.impl;

import static io.harness.rule.OwnerRule.HITESH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import io.harness.CategoryTest;
import io.harness.batch.processing.ccm.InstanceState;
import io.harness.batch.processing.ccm.InstanceType;
import io.harness.batch.processing.entities.InstanceData;
import io.harness.batch.processing.pricing.aws.athena.AwsAthenaQueryHelperService;
import io.harness.batch.processing.pricing.data.AccountComputePricingData;
import io.harness.batch.processing.pricing.data.AccountFargatePricingData;
import io.harness.batch.processing.pricing.data.EcsFargatePricingInfo;
import io.harness.batch.processing.pricing.data.VMComputePricingInfo;
import io.harness.batch.processing.writer.constants.InstanceMetaDataConstants;
import io.harness.category.element.UnitTests;
import io.harness.rule.OwnerRule.Owner;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class AwsCustomPricingServiceImplTest extends CategoryTest {
  @InjectMocks private AwsCustomPricingServiceImpl awsCustomPricingService;
  @Mock private AwsAthenaQueryHelperService awsAthenaQueryHelperService;
  private static final String RUNNING_INSTANCE_ID = "running_instance_id";
  private static final String INSTANCE_NAME = "instance_name";
  private static final String ACCOUNT_ID = "account_id";
  private static final String CLOUD_PROVIDER_ID = "cloud_provider_id";
  private static final String CLUSTER_NAME = "cluster_name";
  private final String REGION = "us-east-1";
  private final String DEFAULT_INSTANCE_FAMILY = "c4.8xlarge";
  private final String DEFAULT_OPERATING_SYSTEM = "linux";
  private final double DEFAULT_BLENDED_RATE = 1.60;
  private final double DEFAULT_BLENDED_COST = 4.80;
  private final double DEFAULT_UNBLENDED_RATE = 1.40;
  private final double DEFAULT_UNBLENDED_COST = 4.20;
  private final double DEFAULT_INSTANCE_CPU = 36;
  private final double DEFAULT_INSTANCE_MEMORY = 60;
  private final double DEFAULT_INSTANCE_PRICE = 1.60;
  private final Instant NOW = Instant.now();
  private final Instant START_INSTANT = NOW.truncatedTo(ChronoUnit.DAYS);

  @Test
  @Owner(developers = HITESH)
  @Category(UnitTests.class)
  public void testGetAwsCustomComputeVMPricingInfo() throws InterruptedException {
    when(awsAthenaQueryHelperService.fetchComputePriceRate(any(), any())).thenReturn(createAccountPricingData());
    VMComputePricingInfo computeVMPricingInfo = awsCustomPricingService.getComputeVMPricingInfo(
        instanceData(RUNNING_INSTANCE_ID, InstanceState.RUNNING), START_INSTANT);
    assertThat(computeVMPricingInfo.getCpusPerVm()).isEqualTo(DEFAULT_INSTANCE_CPU);
    assertThat(computeVMPricingInfo.getMemPerVm()).isEqualTo(DEFAULT_INSTANCE_MEMORY);
    assertThat(computeVMPricingInfo.getOnDemandPrice()).isEqualTo(DEFAULT_INSTANCE_PRICE);
    assertThat(computeVMPricingInfo.getType()).isEqualTo(DEFAULT_INSTANCE_FAMILY);
    VMComputePricingInfo computeVMPricingInfoCached = awsCustomPricingService.getComputeVMPricingInfo(
        instanceData(RUNNING_INSTANCE_ID, InstanceState.RUNNING), START_INSTANT);
    assertThat(computeVMPricingInfoCached).isNotNull();
  }

  @Test
  @Owner(developers = HITESH)
  @Category(UnitTests.class)
  public void testGetAwsFargateVMPricingInfo() throws InterruptedException {
    when(awsAthenaQueryHelperService.fetchEcsFargatePriceRate(any(), any()))
        .thenReturn(createAccountFargatePricingData());
    EcsFargatePricingInfo fargateVMPricingInfo = awsCustomPricingService.getFargateVMPricingInfo(
        instanceData(RUNNING_INSTANCE_ID, InstanceState.RUNNING), START_INSTANT);
    assertThat(fargateVMPricingInfo.getRegion()).isEqualTo(REGION);
    assertThat(fargateVMPricingInfo.getCpuPrice()).isEqualTo(DEFAULT_BLENDED_RATE);
    assertThat(fargateVMPricingInfo.getMemoryPrice()).isEqualTo(DEFAULT_UNBLENDED_RATE);
    EcsFargatePricingInfo fargateVMPricingInfoCached = awsCustomPricingService.getFargateVMPricingInfo(
        instanceData(RUNNING_INSTANCE_ID, InstanceState.RUNNING), START_INSTANT);
    assertThat(fargateVMPricingInfoCached).isNotNull();
  }

  @Test
  @Owner(developers = HITESH)
  @Category(UnitTests.class)
  public void testShouldReturnNullVMPricingInfo() throws InterruptedException {
    when(awsAthenaQueryHelperService.fetchComputePriceRate(any(), any())).thenThrow(InterruptedException.class);
    VMComputePricingInfo computeVMPricingInfo = awsCustomPricingService.getComputeVMPricingInfo(
        instanceData(RUNNING_INSTANCE_ID, InstanceState.RUNNING), START_INSTANT);
    assertThat(computeVMPricingInfo).isNull();
  }

  @Test
  @Owner(developers = HITESH)
  @Category(UnitTests.class)
  public void testShouldReturnNullFargatePricingInfo() throws InterruptedException {
    when(awsAthenaQueryHelperService.fetchEcsFargatePriceRate(any(), any())).thenThrow(InterruptedException.class);
    EcsFargatePricingInfo fargateVMPricingInfo = awsCustomPricingService.getFargateVMPricingInfo(
        instanceData(RUNNING_INSTANCE_ID, InstanceState.RUNNING), START_INSTANT);
    assertThat(fargateVMPricingInfo).isNull();
  }

  private InstanceData instanceData(String instanceId, InstanceState instanceState) {
    return InstanceData.builder()
        .instanceId(instanceId)
        .instanceName(INSTANCE_NAME)
        .accountId(ACCOUNT_ID)
        .settingId(CLOUD_PROVIDER_ID)
        .instanceState(instanceState)
        .clusterName(CLUSTER_NAME)
        .instanceType(InstanceType.EC2_INSTANCE)
        .instanceState(InstanceState.RUNNING)
        .usageStartTime(START_INSTANT)
        .metaData(metaData())
        .build();
  }

  private List<AccountComputePricingData> createAccountPricingData() {
    List<AccountComputePricingData> accountComputePricingDataList = new ArrayList<>();
    AccountComputePricingData accountComputePricingData = AccountComputePricingData.builder()
                                                              .cpusPerVm(DEFAULT_INSTANCE_CPU)
                                                              .memPerVm(DEFAULT_INSTANCE_MEMORY)
                                                              .blendedCost(DEFAULT_INSTANCE_PRICE)
                                                              .instanceType(DEFAULT_INSTANCE_FAMILY)
                                                              .operatingSystem(DEFAULT_OPERATING_SYSTEM)
                                                              .blendedCost(DEFAULT_BLENDED_COST)
                                                              .blendedRate(DEFAULT_BLENDED_RATE)
                                                              .unBlendedCost(DEFAULT_UNBLENDED_COST)
                                                              .unBlendedRate(DEFAULT_UNBLENDED_RATE)
                                                              .region(REGION)
                                                              .build();
    accountComputePricingDataList.add(accountComputePricingData);
    return accountComputePricingDataList;
  }

  private List<AccountFargatePricingData> createAccountFargatePricingData() {
    List<AccountFargatePricingData> accountFargatePricingDataList = new ArrayList<>();

    accountFargatePricingDataList.add(
        accountFargatePricingData(DEFAULT_BLENDED_COST, DEFAULT_BLENDED_RATE, true, false));
    accountFargatePricingDataList.add(
        accountFargatePricingData(DEFAULT_UNBLENDED_COST, DEFAULT_UNBLENDED_RATE, false, true));
    return accountFargatePricingDataList;
  }

  private AccountFargatePricingData accountFargatePricingData(
      double cost, double rate, boolean cpuPriceType, boolean memoryPriceType) {
    return AccountFargatePricingData.builder()
        .blendedCost(cost)
        .blendedRate(rate)
        .unBlendedCost(cost)
        .unBlendedRate(rate)
        .cpuPriceType(cpuPriceType)
        .memoryPriceType(memoryPriceType)
        .region(REGION)
        .build();
  }

  private Map<String, String> metaData() {
    Map<String, String> metaData = new HashMap<>();
    metaData.put(InstanceMetaDataConstants.INSTANCE_FAMILY, DEFAULT_INSTANCE_FAMILY);
    metaData.put(InstanceMetaDataConstants.REGION, REGION);
    metaData.put(InstanceMetaDataConstants.OPERATING_SYSTEM, DEFAULT_OPERATING_SYSTEM);
    return metaData;
  }
}
