package software.wings.graphql.datafetcher.cloudProvider;

import static io.harness.exception.WingsException.USER;

import io.harness.exception.InvalidRequestException;
import io.harness.utils.RequestField;

import software.wings.beans.AwsConfig;
import software.wings.beans.AwsConfig.AwsConfigBuilder;
import software.wings.beans.AwsCrossAccountAttributes;
import software.wings.beans.AwsCrossAccountAttributes.AwsCrossAccountAttributesBuilder;
import software.wings.beans.SettingAttribute;
import software.wings.graphql.datafetcher.secrets.UsageScopeController;
import software.wings.graphql.schema.mutation.cloudProvider.aws.QLAwsCloudProviderInput;
import software.wings.graphql.schema.mutation.cloudProvider.aws.QLAwsManualCredentials;
import software.wings.graphql.schema.mutation.cloudProvider.aws.QLEc2IamCredentials;
import software.wings.graphql.schema.mutation.cloudProvider.aws.QLUpdateAwsCloudProviderInput;
import software.wings.graphql.schema.mutation.cloudProvider.aws.QLUpdateEc2IamCredentials;
import software.wings.graphql.schema.type.secrets.QLUsageScope;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.lang3.ObjectUtils;

@Singleton
public class AwsDataFetcherHelper {
  @Inject private UsageScopeController usageScopeController;

  public SettingAttribute toSettingAttribute(QLAwsCloudProviderInput input, String accountId) {
    AwsConfigBuilder configBuilder = AwsConfig.builder().accountId(accountId);

    SettingAttribute.Builder settingAttributeBuilder =
        SettingAttribute.Builder.aSettingAttribute().withAccountId(accountId).withCategory(
            SettingAttribute.SettingCategory.SETTING);

    if (input.getCredentialsType().isPresent() && input.getCredentialsType().getValue().isPresent()) {
      input.getCredentialsType().getValue().ifPresent(credentialsType -> {
        switch (credentialsType) {
          case EC2_IAM: {
            QLEc2IamCredentials credentials = input.getEc2IamCredentials().getValue().orElseThrow(
                () -> new InvalidRequestException("No ec2IamCredentials provided with the request."));

            configBuilder.useEc2IamCredentials(true);
            configBuilder.tag(credentials.getDelegateSelector().getValue().orElseThrow(
                () -> new InvalidRequestException("No delegateSelector provided with the request.")));
            RequestField<QLUsageScope> usageRestrictions = credentials.getUsageScope();
            if (usageRestrictions != null && usageRestrictions.isPresent()) {
              settingAttributeBuilder.withUsageRestrictions(
                  usageScopeController.populateUsageRestrictions(usageRestrictions.getValue().orElse(null), accountId));
            }
          } break;
          case MANUAL: {
            QLAwsManualCredentials credentials = input.getManualCredentials().getValue().orElseThrow(
                () -> new InvalidRequestException("No manualCredentials provided with the request."));

            configBuilder.useEc2IamCredentials(false);
            validateAccessKeyFields(credentials.getAccessKey(), credentials.getAccessKeySecretId(), false);
            credentials.getAccessKey().getValue().map(String::toCharArray).ifPresent(accessKey -> {
              configBuilder.accessKey(accessKey);
              configBuilder.useEncryptedAccessKey(false);
            });
            credentials.getAccessKeySecretId().getValue().ifPresent(accessKeySecretId -> {
              configBuilder.encryptedAccessKey(accessKeySecretId);
              configBuilder.useEncryptedAccessKey(true);
            });
            configBuilder.encryptedSecretKey(credentials.getSecretKeySecretId().getValue().orElseThrow(
                () -> new InvalidRequestException("No secretKeySecretId provided with the request.")));
          } break;
          default:
            throw new InvalidRequestException("Invalid credentials type");
        }
      });
    } else {
      throw new InvalidRequestException("No credentialsType provided with the request.");
    }

    if (input.getCrossAccountAttributes().isPresent()) {
      input.getCrossAccountAttributes().getValue().ifPresent(crossAccountAttributes -> {
        crossAccountAttributes.getAssumeCrossAccountRole().getValue().ifPresent(configBuilder::assumeCrossAccountRole);

        AwsCrossAccountAttributesBuilder builder = AwsCrossAccountAttributes.builder();

        builder.crossAccountRoleArn(crossAccountAttributes.getCrossAccountRoleArn().getValue().orElseThrow(
            () -> new InvalidRequestException("No crossAccountRoleArn provided with the request.")));

        crossAccountAttributes.getExternalId().getValue().ifPresent(builder::externalId);

        configBuilder.crossAccountAttributes(builder.build());
      });
    }
    settingAttributeBuilder.withValue(configBuilder.build());

    if (input.getName().isPresent() && input.getName().getValue().isPresent()) {
      input.getName().getValue().ifPresent(settingAttributeBuilder::withName);
    } else {
      throw new InvalidRequestException("No name provided with the request.");
    }

    return settingAttributeBuilder.build();
  }

  public void updateSettingAttribute(
      SettingAttribute settingAttribute, QLUpdateAwsCloudProviderInput input, String accountId) {
    AwsConfig config = (AwsConfig) settingAttribute.getValue();

    if (input.getCredentialsType().isPresent() && input.getCredentialsType().getValue().isPresent()) {
      input.getCredentialsType().getValue().ifPresent(credentialsType -> {
        switch (credentialsType) {
          case EC2_IAM: {
            config.setUseEc2IamCredentials(true);
            config.setAccessKey(null);
            config.setEncryptedSecretKey(null);
            input.getEc2IamCredentials()
                .getValue()
                .flatMap(credentials -> credentials.getDelegateSelector().getValue())
                .ifPresent(config::setTag);
            QLUpdateEc2IamCredentials ec2IamCredentials = input.getEc2IamCredentials().getValue().orElseThrow(
                () -> new InvalidRequestException("No EC2 IAM Credentials provided"));
            if (ec2IamCredentials != null) {
              RequestField<QLUsageScope> usageRestrictions = ec2IamCredentials.getUsageScope();
              if (usageRestrictions != null && usageRestrictions.isPresent()) {
                settingAttribute.setUsageRestrictions(usageScopeController.populateUsageRestrictions(
                    usageRestrictions.getValue().orElse(null), accountId));
              }
            }
          } break;
          case MANUAL: {
            config.setUseEc2IamCredentials(false);
            config.setTag(null);
            input.getManualCredentials().getValue().ifPresent(credentials -> {
              validateAccessKeyFields(credentials.getAccessKey(), credentials.getAccessKeySecretId(), true);
              credentials.getAccessKey().getValue().map(String::toCharArray).ifPresent(accessKey -> {
                config.setAccessKey(accessKey);
                config.setEncryptedAccessKey(null);
                config.setUseEncryptedAccessKey(false);
              });
              credentials.getAccessKeySecretId().getValue().ifPresent(accessKeySecretId -> {
                config.setEncryptedAccessKey(accessKeySecretId);
                config.setAccessKey(null);
                config.setUseEncryptedAccessKey(true);
              });
              credentials.getSecretKeySecretId().getValue().ifPresent(config::setEncryptedSecretKey);
            });
          } break;
          default:
            throw new InvalidRequestException("Invalid credentials type");
        }
      });
    }

    if (input.getCrossAccountAttributes().isPresent()) {
      input.getCrossAccountAttributes().getValue().ifPresent(crossAccountAttributes -> {
        crossAccountAttributes.getAssumeCrossAccountRole().getValue().ifPresent(config::setAssumeCrossAccountRole);

        AwsCrossAccountAttributes awsCrossAccountAttributes =
            ObjectUtils.defaultIfNull(config.getCrossAccountAttributes(), AwsCrossAccountAttributes.builder().build());

        crossAccountAttributes.getCrossAccountRoleArn().getValue().ifPresent(
            awsCrossAccountAttributes::setCrossAccountRoleArn);
        crossAccountAttributes.getExternalId().getValue().ifPresent(awsCrossAccountAttributes::setExternalId);

        config.setCrossAccountAttributes(awsCrossAccountAttributes);
      });
    }

    settingAttribute.setValue(config);

    if (input.getName().isPresent()) {
      input.getName().getValue().ifPresent(settingAttribute::setName);
    }
  }

  private void validateAccessKeyFields(
      RequestField<String> accessKey, RequestField<String> accessKeySecretId, boolean isUpdate) {
    if (isFieldValuePresent(accessKey) && isFieldValuePresent(accessKeySecretId)) {
      throw new InvalidRequestException("Cannot set both value and secret reference for accessKey field", USER);
    }

    if (!isUpdate && !isFieldValuePresent(accessKey) && !isFieldValuePresent(accessKeySecretId)) {
      throw new InvalidRequestException("One of fields 'accessKey' or 'accessKeySecretId' is required", USER);
    }
  }

  private <T> boolean isFieldValuePresent(RequestField<T> field) {
    return field.isPresent() && field.getValue().isPresent();
  }
}
