package software.wings.integration.migration.legacy;

import static io.harness.data.structure.EmptyPredicate.isEmpty;
import static software.wings.dl.PageRequest.PageRequestBuilder.aPageRequest;
import static software.wings.dl.PageRequest.UNLIMITED;

import com.google.inject.Inject;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.wings.WingsBaseTest;
import software.wings.beans.SearchFilter.Operator;
import software.wings.beans.container.ContainerTask;
import software.wings.dl.PageRequest;
import software.wings.dl.PageResponse;
import software.wings.dl.WingsPersistence;
import software.wings.rules.Integration;

import java.util.List;

/**
 * Migration script to remove line separator (---\n) characters from the advanced yaml payload
 * @author rktummala on 1/24/18
 */
@Integration
@Ignore
public class AdvancedKubernetesPayloadMigratorUtil extends WingsBaseTest {
  private static final Logger logger = LoggerFactory.getLogger(AdvancedKubernetesPayloadMigratorUtil.class);

  @Inject private WingsPersistence wingsPersistence;

  @Test
  public void setNameFieldInArtifactStream() {
    PageRequest<ContainerTask> pageRequest = aPageRequest()
                                                 .withLimit(UNLIMITED)
                                                 .addFilter("deploymentType", Operator.EQ, "KUBERNETES")
                                                 .addFilter("advancedType", Operator.EQ, "YAML")
                                                 .addFieldsIncluded("_id", "advancedConfig")
                                                 .build();
    logger.info("Retrieving advanced kubernetes yaml deployment specifications");
    PageResponse<ContainerTask> pageResponse = wingsPersistence.query(ContainerTask.class, pageRequest);

    if (pageResponse.isEmpty() || isEmpty(pageResponse.getResponse())) {
      logger.info("No kubernetes yaml deployment specifications found");
      return;
    }

    updateAdvancedKubernetesConfig(pageResponse.getResponse());
  }

  private void updateAdvancedKubernetesConfig(List<ContainerTask> containerTaskList) {
    for (ContainerTask containerTask : containerTaskList) {
      String transformedAdvConfig = containerTask.getAdvancedConfig().replaceAll("\n---\n", "\n");
      wingsPersistence.updateField(
          ContainerTask.class, containerTask.getUuid(), "advancedConfig", transformedAdvConfig);
    }
  }
}
