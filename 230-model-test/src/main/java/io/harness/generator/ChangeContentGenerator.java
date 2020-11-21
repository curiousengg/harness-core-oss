package io.harness.generator;

import software.wings.beans.Account;
import software.wings.beans.EntityYamlRecord;
import software.wings.beans.EntityYamlRecord.EntityYamlRecordBuilder;
import software.wings.dl.WingsPersistence;

import com.google.inject.Inject;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ChangeContentGenerator {
  @Inject AccountGenerator accountGenerator;
  @Inject WingsPersistence wingsPersistence;
  @Inject ChangeSetGenerator changeSetGenerator;

  public List<EntityYamlRecord> ensureYamlTest(Randomizer.Seed seed, OwnerManager.Owners owners) {
    changeSetGenerator.ensureUserChangeSetTest(seed, owners);
    final Account account = accountGenerator.ensurePredefined(seed, owners, AccountGenerator.Accounts.GENERIC_TEST);
    EntityYamlRecordBuilder builder = EntityYamlRecord.builder();
    EntityYamlRecord oldRecord = builder.accountId(account.getUuid())
                                     .entityId("resourceId")
                                     .uuid("oldYamlId")
                                     .yamlContent("harnessApiVersion: '1.0'\n"
                                         + "type: NOTIFICATION_GROUP\n"
                                         + "defaultNotificationGroupForAccount: 'true'\n")
                                     .yamlSha("yamlSha")
                                     .build();

    EntityYamlRecord newRecord = builder.accountId(account.getUuid())
                                     .entityId("resourceId")
                                     .uuid("newYamlId")
                                     .yamlContent("harnessApiVersion: '1.0'\n"
                                         + "type: NOTIFICATION_GROUP\n"
                                         + "defaultNotificationGroupForAccount: 'true'\n")
                                     .yamlSha("yamlSha")
                                     .build();

    return new LinkedList<>(Arrays.asList(ensureYaml(oldRecord), ensureYaml(newRecord)));
  }

  private EntityYamlRecord ensureYaml(EntityYamlRecord yamlRecord) {
    if (yamlRecord == null) {
      return null;
    }
    return wingsPersistence.get(EntityYamlRecord.class, wingsPersistence.save(yamlRecord));
  }
}
