package software.wings.service.impl.yaml.handler.setting;

import static io.harness.data.structure.EmptyPredicate.isEmpty;

import static software.wings.beans.Application.GLOBAL_APP_ID;
import static software.wings.beans.Environment.GLOBAL_ENV_ID;
import static software.wings.beans.yaml.YamlConstants.PATH_DELIMITER;

import io.harness.exception.HarnessException;

import software.wings.beans.SettingAttribute;
import software.wings.beans.SettingAttribute.SettingCategory;
import software.wings.beans.yaml.ChangeContext;
import software.wings.beans.yaml.YamlType;
import software.wings.security.UsageRestrictions;
import software.wings.security.UsageRestrictionsYaml;
import software.wings.service.impl.SettingServiceHelper;
import software.wings.service.impl.yaml.handler.BaseYamlHandler;
import software.wings.service.impl.yaml.handler.usagerestrictions.UsageRestrictionsYamlHandler;
import software.wings.service.impl.yaml.service.YamlHelper;
import software.wings.service.intfc.SettingsService;
import software.wings.service.intfc.security.EncryptionService;
import software.wings.service.intfc.security.SecretManager;
import software.wings.settings.SettingValue;
import software.wings.settings.SettingValueYaml;

import com.google.inject.Inject;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * @author rktummala on 11/19/17
 */
@Slf4j
public abstract class SettingValueYamlHandler<Y extends SettingValueYaml, B extends SettingValue>
    extends BaseYamlHandler<Y, SettingAttribute> {
  @Inject protected SecretManager secretManager;
  @Inject protected SettingsService settingsService;
  @Inject protected SettingServiceHelper settingServiceHelper;
  @Inject private UsageRestrictionsYamlHandler usageRestrictionsYamlHandler;
  @Inject protected EncryptionService encryptionService;
  @Inject protected YamlHelper yamlHelper;

  @Override
  public SettingAttribute upsertFromYaml(ChangeContext<Y> changeContext, List<ChangeContext> changeSetContext)
      throws HarnessException {
    SettingAttribute previous = get(changeContext.getChange().getAccountId(), changeContext.getChange().getFilePath());
    SettingAttribute settingAttribute = toBean(previous, changeContext, changeSetContext);
    settingAttribute.setSyncFromGit(changeContext.getChange().isSyncFromGit());

    ChangeContext.Builder clonedContextBuilder =
        cloneFileChangeContext(changeContext, changeContext.getYaml().getUsageRestrictions());
    ChangeContext clonedContext = clonedContextBuilder.build();

    UsageRestrictions usageRestrictions = usageRestrictionsYamlHandler.upsertFromYaml(clonedContext, changeSetContext);
    settingAttribute.setUsageRestrictions(usageRestrictions);

    if (previous != null) {
      settingAttribute.setUuid(previous.getUuid());
      return settingsService.update(settingAttribute);
    } else {
      return settingsService.save(settingAttribute);
    }
  }

  protected abstract SettingAttribute toBean(SettingAttribute previous, ChangeContext<Y> changeContext,
      List<ChangeContext> changeSetContext) throws HarnessException;

  protected String getEncryptedYamlRef(String accountId, String secretId) {
    if (isEmpty(accountId) || isEmpty(secretId)) {
      return null;
    }
    return secretManager.getEncryptedYamlRef(accountId, secretId);
  }

  protected void toYaml(Y yaml, SettingAttribute settingAttribute, String appId) {
    UsageRestrictionsYaml usageRestrictionsYaml =
        usageRestrictionsYamlHandler.toYaml(settingServiceHelper.getUsageRestrictions(settingAttribute), appId);
    yaml.setUsageRestrictions(usageRestrictionsYaml);
  }

  @Override
  public void delete(ChangeContext<Y> changeContext) throws HarnessException {
    SettingAttribute settingAttribute =
        get(changeContext.getChange().getAccountId(), changeContext.getChange().getFilePath());
    if (settingAttribute != null) {
      settingsService.deleteByYamlGit(
          GLOBAL_APP_ID, settingAttribute.getUuid(), changeContext.getChange().isSyncFromGit());
    }
  }

  protected SettingAttribute buildSettingAttribute(
      String accountId, String yamlFilePath, String uuid, B config, SettingCategory category) {
    String name;
    YamlType yamlType = yamlHelper.getYamlTypeFromSettingAttributePath(yamlFilePath);
    if (yamlType == null || yamlType == YamlType.ARTIFACT_SERVER || yamlType == YamlType.CLOUD_PROVIDER) {
      name = yamlHelper.getNameFromYamlFilePath(yamlFilePath);
    } else {
      name = yamlHelper.extractParentEntityName(yamlType.getPrefixExpression(), yamlFilePath, PATH_DELIMITER);
    }
    return SettingAttribute.Builder.aSettingAttribute()
        .withAccountId(accountId)
        .withAppId(GLOBAL_APP_ID)
        .withCategory(category)
        .withEnvId(GLOBAL_ENV_ID)
        .withName(name)
        .withUuid(uuid)
        .withValue(config)
        .build();
  }
}
