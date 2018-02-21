package software.wings.beans;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.SchemaIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import software.wings.beans.AwsInfrastructureMapping.AwsRegionDataProvider;
import software.wings.stencils.DefaultValue;
import software.wings.stencils.EnumData;
import software.wings.utils.Util;

import java.util.List;
import java.util.Optional;

/**
 * Created by anubhaw on 12/19/17.
 */
@JsonTypeName("AWS_AMI")
public class AwsAmiInfrastructureMapping extends InfrastructureMapping {
  @Attributes(title = "Region")
  @DefaultValue("us-east-1")
  @EnumData(enumDataProvider = AwsRegionDataProvider.class)
  private String region;

  private String autoScalingGroupName;
  private List<String> classicLoadBalancers;
  private List<String> targetGroupArns;
  private String hostNameConvention;

  public AwsAmiInfrastructureMapping() {
    super(InfrastructureMappingType.AWS_AMI.name());
  }

  @SchemaIgnore
  @Override
  @Attributes(title = "Connection Type")
  public String getHostConnectionAttrs() {
    return null;
  }

  @SchemaIgnore
  @Override
  public String getDefaultName() {
    return Util.normalize(String.format("%s (AWS_AMI) %s",
        Optional.ofNullable(this.getComputeProviderName()).orElse(this.getComputeProviderType().toLowerCase()),
        this.getRegion()));
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public String getAutoScalingGroupName() {
    return autoScalingGroupName;
  }

  public void setAutoScalingGroupName(String autoScalingGroupName) {
    this.autoScalingGroupName = autoScalingGroupName;
  }

  public List<String> getClassicLoadBalancers() {
    return classicLoadBalancers;
  }

  public void setClassicLoadBalancers(List<String> classicLoadBalancers) {
    this.classicLoadBalancers = classicLoadBalancers;
  }

  public List<String> getTargetGroupArns() {
    return targetGroupArns;
  }

  public void setTargetGroupArns(List<String> targetGroupArns) {
    this.targetGroupArns = targetGroupArns;
  }

  public String getHostNameConvention() {
    return hostNameConvention;
  }

  public void setHostNameConvention(String hostNameConvention) {
    this.hostNameConvention = hostNameConvention;
  }

  public static final class Builder {
    private AwsAmiInfrastructureMapping awsAmiInfrastructureMapping;

    private Builder() {
      awsAmiInfrastructureMapping = new AwsAmiInfrastructureMapping();
    }

    public static Builder anAwsAmiInfrastructureMapping() {
      return new Builder();
    }

    public Builder withRegion(String region) {
      awsAmiInfrastructureMapping.setRegion(region);
      return this;
    }

    public Builder withAutoScalingGroupName(String autoScalingGroupName) {
      awsAmiInfrastructureMapping.setAutoScalingGroupName(autoScalingGroupName);
      return this;
    }

    public Builder withClassicLoadBalancers(List<String> classicLoadBalancers) {
      awsAmiInfrastructureMapping.setClassicLoadBalancers(classicLoadBalancers);
      return this;
    }

    public Builder withTargetGroupArns(List<String> targetGroupArns) {
      awsAmiInfrastructureMapping.setTargetGroupArns(targetGroupArns);
      return this;
    }

    public Builder withUuid(String uuid) {
      awsAmiInfrastructureMapping.setUuid(uuid);
      return this;
    }

    public Builder withAppId(String appId) {
      awsAmiInfrastructureMapping.setAppId(appId);
      return this;
    }

    public Builder withCreatedBy(EmbeddedUser createdBy) {
      awsAmiInfrastructureMapping.setCreatedBy(createdBy);
      return this;
    }

    public Builder withCreatedAt(long createdAt) {
      awsAmiInfrastructureMapping.setCreatedAt(createdAt);
      return this;
    }

    public Builder withLastUpdatedBy(EmbeddedUser lastUpdatedBy) {
      awsAmiInfrastructureMapping.setLastUpdatedBy(lastUpdatedBy);
      return this;
    }

    public Builder withLastUpdatedAt(long lastUpdatedAt) {
      awsAmiInfrastructureMapping.setLastUpdatedAt(lastUpdatedAt);
      return this;
    }

    public Builder withComputeProviderSettingId(String computeProviderSettingId) {
      awsAmiInfrastructureMapping.setComputeProviderSettingId(computeProviderSettingId);
      return this;
    }

    public Builder withEnvId(String envId) {
      awsAmiInfrastructureMapping.setEnvId(envId);
      return this;
    }

    public Builder withServiceTemplateId(String serviceTemplateId) {
      awsAmiInfrastructureMapping.setServiceTemplateId(serviceTemplateId);
      return this;
    }

    public Builder withServiceId(String serviceId) {
      awsAmiInfrastructureMapping.setServiceId(serviceId);
      return this;
    }

    public Builder withComputeProviderType(String computeProviderType) {
      awsAmiInfrastructureMapping.setComputeProviderType(computeProviderType);
      return this;
    }

    public Builder withInfraMappingType(String infraMappingType) {
      awsAmiInfrastructureMapping.setInfraMappingType(infraMappingType);
      return this;
    }

    public Builder withDeploymentType(String deploymentType) {
      awsAmiInfrastructureMapping.setDeploymentType(deploymentType);
      return this;
    }

    public Builder withComputeProviderName(String computeProviderName) {
      awsAmiInfrastructureMapping.setComputeProviderName(computeProviderName);
      return this;
    }

    public Builder withName(String name) {
      awsAmiInfrastructureMapping.setName(name);
      return this;
    }

    public Builder withAccountId(String accountId) {
      awsAmiInfrastructureMapping.setAccountId(accountId);
      return this;
    }

    public Builder withHostNameConvention(String hostNameConvention) {
      awsAmiInfrastructureMapping.setHostNameConvention(hostNameConvention);
      return this;
    }

    public Builder but() {
      return anAwsAmiInfrastructureMapping()
          .withRegion(awsAmiInfrastructureMapping.getRegion())
          .withAutoScalingGroupName(awsAmiInfrastructureMapping.getAutoScalingGroupName())
          .withClassicLoadBalancers(awsAmiInfrastructureMapping.getClassicLoadBalancers())
          .withTargetGroupArns(awsAmiInfrastructureMapping.getTargetGroupArns())
          .withUuid(awsAmiInfrastructureMapping.getUuid())
          .withAppId(awsAmiInfrastructureMapping.getAppId())
          .withCreatedBy(awsAmiInfrastructureMapping.getCreatedBy())
          .withCreatedAt(awsAmiInfrastructureMapping.getCreatedAt())
          .withLastUpdatedBy(awsAmiInfrastructureMapping.getLastUpdatedBy())
          .withLastUpdatedAt(awsAmiInfrastructureMapping.getLastUpdatedAt())
          .withComputeProviderSettingId(awsAmiInfrastructureMapping.getComputeProviderSettingId())
          .withEnvId(awsAmiInfrastructureMapping.getEnvId())
          .withServiceTemplateId(awsAmiInfrastructureMapping.getServiceTemplateId())
          .withServiceId(awsAmiInfrastructureMapping.getServiceId())
          .withComputeProviderType(awsAmiInfrastructureMapping.getComputeProviderType())
          .withInfraMappingType(awsAmiInfrastructureMapping.getInfraMappingType())
          .withDeploymentType(awsAmiInfrastructureMapping.getDeploymentType())
          .withComputeProviderName(awsAmiInfrastructureMapping.getComputeProviderName())
          .withName(awsAmiInfrastructureMapping.getName())
          .withAccountId(awsAmiInfrastructureMapping.getAccountId())
          .withHostNameConvention(awsAmiInfrastructureMapping.getHostNameConvention());
    }

    public AwsAmiInfrastructureMapping build() {
      return awsAmiInfrastructureMapping;
    }
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  @NoArgsConstructor
  public static final class Yaml extends InfrastructureMapping.YamlWithComputeProvider {
    private String region;
    private String autoScalingGroupName;
    private List<String> classicLoadBalancers;
    private List<String> targetGroupArns;

    @lombok.Builder
    public Yaml(String type, String harnessApiVersion, String computeProviderType, String serviceName,
        String infraMappingType, String deploymentType, String computeProviderName, String region,
        String autoScalingGroupName, List<String> classicLoadBalancers, List<String> targetGroupArns) {
      super(type, harnessApiVersion, computeProviderType, serviceName, infraMappingType, deploymentType,
          computeProviderName);
      this.region = region;
      this.autoScalingGroupName = autoScalingGroupName;
      this.classicLoadBalancers = classicLoadBalancers;
      this.targetGroupArns = targetGroupArns;
    }
  }
}
