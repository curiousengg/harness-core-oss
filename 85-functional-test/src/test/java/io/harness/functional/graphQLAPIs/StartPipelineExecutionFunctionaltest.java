package io.harness.functional.graphQLAPIs;

import static io.harness.functional.WorkflowUtils.getTemplateExpressionsForEnv;
import static io.harness.functional.WorkflowUtils.getTemplateExpressionsForInfraDefinition;
import static io.harness.functional.WorkflowUtils.getTemplateExpressionsForService;
import static io.harness.generator.EnvironmentGenerator.Environments.GENERIC_TEST;
import static io.harness.rule.OwnerRule.POOJA;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import io.harness.category.element.FunctionalTests;
import io.harness.functional.AbstractFunctionalTest;
import io.harness.functional.WorkflowUtils;
import io.harness.generator.ApplicationGenerator;
import io.harness.generator.EnvironmentGenerator;
import io.harness.generator.InfrastructureDefinitionGenerator;
import io.harness.generator.OwnerManager;
import io.harness.generator.Randomizer;
import io.harness.generator.ServiceGenerator;
import io.harness.rule.Owner;
import io.harness.testframework.framework.utils.PipelineUtils;
import io.harness.testframework.restutils.ArtifactRestUtils;
import io.harness.testframework.restutils.GraphQLRestUtils;
import io.harness.testframework.restutils.PipelineRestUtils;
import io.harness.testframework.restutils.WorkflowRestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import software.wings.beans.Application;
import software.wings.beans.Environment;
import software.wings.beans.FeatureName;
import software.wings.beans.InfrastructureType;
import software.wings.beans.Pipeline;
import software.wings.beans.PipelineStage;
import software.wings.beans.Service;
import software.wings.beans.Workflow;
import software.wings.beans.artifact.Artifact;
import software.wings.infra.InfrastructureDefinition;
import software.wings.service.intfc.FeatureFlagService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class StartPipelineExecutionFunctionaltest extends AbstractFunctionalTest {
  @Inject private OwnerManager ownerManager;
  @Inject private ApplicationGenerator applicationGenerator;
  @Inject private ServiceGenerator serviceGenerator;
  @Inject private EnvironmentGenerator environmentGenerator;
  @Inject private InfrastructureDefinitionGenerator infrastructureDefinitionGenerator;
  @Inject private WorkflowUtils workflowUtils;

  private Application application;
  private Service service;
  private Environment environment;
  private InfrastructureDefinition infrastructureDefinition;
  @Inject private FeatureFlagService featureFlagService;

  final Randomizer.Seed seed = new Randomizer.Seed(0);
  OwnerManager.Owners owners;

  @Before
  public void setUp() {
    owners = ownerManager.create();
    application = applicationGenerator.ensurePredefined(seed, owners, ApplicationGenerator.Applications.GENERIC_TEST);
    assertThat(application).isNotNull();

    if (!featureFlagService.isEnabled(FeatureName.GRAPHQL_DEV, application.getAccountId())) {
      featureFlagService.enableAccount(FeatureName.GRAPHQL_DEV, application.getAccountId());
    }
    if (!featureFlagService.isEnabled(FeatureName.GRAPHQL, application.getAccountId())) {
      featureFlagService.enableAccount(FeatureName.GRAPHQL, application.getAccountId());
    }

    service = serviceGenerator.ensureK8sTest(seed, owners, "k8s-service");
    environment = environmentGenerator.ensurePredefined(seed, owners, GENERIC_TEST);
    infrastructureDefinition = infrastructureDefinitionGenerator.ensurePredefined(
        seed, owners, InfrastructureType.GCP_KUBERNETES_ENGINE, bearerToken);
  }

  @Test
  @Owner(developers = POOJA)
  @Category(FunctionalTests.class)
  public void shouldTriggerTemplatisedWorkflow() {
    Workflow workflow =
        workflowUtils.getRollingK8sWorkflow("gcp-k8s-templatized-pooja-", service, infrastructureDefinition);
    Workflow savedWorkflow =
        WorkflowRestUtils.createWorkflow(bearerToken, application.getAccountId(), application.getUuid(), workflow);
    assertThat(savedWorkflow).isNotNull();

    // Templatize env and infra of  the workflow
    savedWorkflow.setTemplateExpressions(Arrays.asList(getTemplateExpressionsForEnv(),
        getTemplateExpressionsForService(), getTemplateExpressionsForInfraDefinition("${InfraDefinition_Kubernetes}")));

    Workflow templatizedWorkflow =
        WorkflowRestUtils.updateWorkflow(bearerToken, application.getAccountId(), application.getUuid(), savedWorkflow);
    assertThat(templatizedWorkflow.isEnvTemplatized()).isTrue();
    assertThat(templatizedWorkflow.isTemplatized()).isTrue();

    String pipelineName = "GraphQLAPI Test - " + System.currentTimeMillis();

    Pipeline pipeline = new Pipeline();
    pipeline.setName(pipelineName);
    pipeline.setDescription("description");

    Pipeline createdPipeline =
        PipelineRestUtils.createPipeline(application.getAppId(), pipeline, getAccount().getUuid(), bearerToken);
    assertThat(createdPipeline).isNotNull();

    ImmutableMap<String, String> workflowVariables = ImmutableMap.<String, String>builder()
                                                         .put("Environment", "${env}")
                                                         .put("Service", "${service}")
                                                         .put("InfraDefinition_Kubernetes", "${infra}")
                                                         .build();

    List<PipelineStage> pipelineStages = new ArrayList<>();
    PipelineStage executionStage =
        PipelineUtils.prepareExecutionStage(environment.getUuid(), templatizedWorkflow.getUuid(), workflowVariables);
    pipelineStages.add(executionStage);
    createdPipeline.setPipelineStages(pipelineStages);

    createdPipeline = PipelineRestUtils.updatePipeline(application.getAppId(), createdPipeline, bearerToken);
    assertThat(createdPipeline).isNotNull();

    Artifact artifact = getArtifact(service, service.getAppId());

    ImmutableMap<String, String> pipelineVariables = ImmutableMap.<String, String>builder()
                                                         .put("env", environment.getName())
                                                         .put("service", service.getName())
                                                         .put("infra", infrastructureDefinition.getName())
                                                         .build();

    String mutation = getGraphqlQueryForPipeline("123", application.getAppId(), createdPipeline.getUuid(),
        pipelineVariables, artifact.getUuid(), service.getName());
    Map<Object, Object> response =
        GraphQLRestUtils.executeMutationGraphQLQuery(bearerToken, application.getAccountId(), mutation);

    assertThat(response).isNotEmpty();
    assertThat(response.get("startExecution")).isNotNull();
    Map<String, Object> executionData = (Map<String, Object>) response.get("startExecution");
    assertThat(executionData.get("clientMutationId")).isEqualTo("123");
    assertThat(executionData.get("execution")).isNotNull();
  }

  private String getGraphqlQueryForPipeline(String clientMutationId, String appId, String pipelineId,
      ImmutableMap<String, String> workflowVariables, String artifactId, String serviceName) {
    List<String> variableInputs = new ArrayList<>();
    for (Map.Entry<String, String> entry : workflowVariables.entrySet()) {
      String queryVariableInput = $GQL(/*{
      name: "%s"
      variableValue: {
          value: "%s"
          type: NAME
          }}*/ entry.getKey(), entry.getValue());
      variableInputs.add(queryVariableInput);
    }
    String variableInputsQuery = "[" + String.join(",", variableInputs) + "]";
    String serviceInputQuery =
        $GQL(/*[{
name: "%s"
artifactValueInput: {
valueType: ARTIFACT_ID
artifactId: {
artifactId: "%s"
}}}]*/ serviceName, artifactId);

    String mutationInputQuery = $GQL(/*
{
entityId: "%s",
applicationId: "%s",
executionType: PIPELINE,
variableInputs: %s,
serviceInputs: %s,
clientMutationId: "%s"
}*/ pipelineId, appId, variableInputsQuery, serviceInputQuery, clientMutationId);

    return $GQL(/*
mutation{
startExecution(input:%s) {
clientMutationId
execution {
 status
}
}
}*/ mutationInputQuery);
  }

  private Artifact getArtifact(Service service, String appId) {
    return ArtifactRestUtils.waitAndFetchArtifactByArtfactStream(
        bearerToken, appId, service.getArtifactStreamIds().get(0), 0);
  }
}
