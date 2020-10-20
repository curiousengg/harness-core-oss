package software.wings.delegatetasks.citasks.cik8handler;

import static io.harness.data.encoding.EncodingUtils.encodeBase64;
import static org.mockito.Mockito.mock;
import static software.wings.beans.ci.pod.SecretParams.Type.FILE;
import static software.wings.beans.ci.pod.SecretParams.Type.TEXT;
import static software.wings.delegatetasks.citasks.cik8handler.SecretSpecBuilder.SECRET_KEY;

import io.harness.connector.apis.dto.ConnectorDTO;
import io.harness.connector.apis.dto.ConnectorInfoDTO;
import io.harness.delegate.beans.connector.ConnectorType;
import io.harness.delegate.beans.connector.docker.DockerAuthType;
import io.harness.delegate.beans.connector.docker.DockerAuthenticationDTO;
import io.harness.delegate.beans.connector.docker.DockerConnectorDTO;
import io.harness.delegate.beans.connector.docker.DockerUserNamePasswordDTO;
import io.harness.delegate.beans.connector.gitconnector.GitAuthType;
import io.harness.delegate.beans.connector.gitconnector.GitConfigDTO;
import io.harness.delegate.beans.connector.gitconnector.GitSSHAuthenticationDTO;
import io.harness.delegate.beans.connector.k8Connector.KubernetesClusterConfigDTO;
import io.harness.encryption.Scope;
import io.harness.encryption.SecretRefData;
import io.harness.k8s.model.ImageDetails;
import io.harness.security.encryption.EncryptedDataDetail;
import io.harness.security.encryption.EncryptedRecordData;
import io.harness.security.encryption.EncryptionType;
import software.wings.beans.KmsConfig;
import software.wings.beans.ci.CIK8BuildTaskParams;
import software.wings.beans.ci.pod.CIContainerType;
import software.wings.beans.ci.pod.CIK8ContainerParams;
import software.wings.beans.ci.pod.CIK8PodParams;
import software.wings.beans.ci.pod.CIK8ServicePodParams;
import software.wings.beans.ci.pod.ConnectorDetails;
import software.wings.beans.ci.pod.ContainerSecrets;
import software.wings.beans.ci.pod.ImageDetailsWithConnector;
import software.wings.beans.ci.pod.PVCParams;
import software.wings.beans.ci.pod.SecretParams;
import software.wings.beans.ci.pod.SecretVariableDTO;
import software.wings.beans.ci.pod.SecretVariableDetails;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CIK8BuildTaskHandlerTestHelper {
  public static final String containerName2 = "container2";
  private static final String namespace = "default";
  private static final String podName = "pod";
  private static final String podName1 = "pod1";
  private static final String imageName = "IMAGE";
  private static final String containerName1 = "container1";
  private static final String tag = "TAG";
  private static final String registryUrl = "https://index.docker.io/v1/";
  private static final String svcName = "service";
  public static final String commitId = "050ec93c9767b8759a07b7a99312974b7acb5d54";
  public static final String branch_name = "master";
  private static final String gitSshRepoUrl = "git@github.com:wings-software/portal.git";
  private static final String gitSshKey = "git_ssh_key";

  private static String storageClass = "test-storage";
  private static Integer storageMib = 100;
  private static String claimName = "pvc";
  private static String volume1 = "volume1";

  public static CIK8BuildTaskParams buildGitSecretErrorTaskParams() {
    CIK8PodParams<CIK8ContainerParams> cik8PodParams = CIK8PodParams.<CIK8ContainerParams>builder()
                                                           .name(podName)
                                                           .namespace(namespace)
                                                           .gitConnector(ConnectorDetails.builder().build())
                                                           .build();
    return CIK8BuildTaskParams.builder().cik8PodParams(cik8PodParams).build();
  }

  public static CIK8BuildTaskParams buildImageSecretErrorTaskParams() {
    ImageDetails imageDetails = ImageDetails.builder().name(imageName).tag(tag).registryUrl(registryUrl).build();
    ImageDetails imageDetailsWithoutRegistry = ImageDetails.builder().name(imageName).tag(tag).build();

    List<CIK8ContainerParams> containerParamsList = new ArrayList<>();
    containerParamsList.add(
        CIK8ContainerParams.builder()
            .imageDetailsWithConnector(ImageDetailsWithConnector.builder().imageDetails(imageDetails).build())
            .build());
    containerParamsList.add(
        CIK8ContainerParams.builder()
            .imageDetailsWithConnector(
                ImageDetailsWithConnector.builder().imageDetails(imageDetailsWithoutRegistry).build())
            .build());

    CIK8PodParams<CIK8ContainerParams> cik8PodParams = CIK8PodParams.<CIK8ContainerParams>builder()
                                                           .name(podName)
                                                           .namespace(namespace)
                                                           .gitConnector(ConnectorDetails.builder().build())
                                                           .containerParamsList(containerParamsList)
                                                           .build();

    return CIK8BuildTaskParams.builder().cik8PodParams(cik8PodParams).build();
  }

  public static CIK8BuildTaskParams buildPodCreateErrorTaskParams() {
    ImageDetails imageDetails = ImageDetails.builder().name(imageName).tag(tag).registryUrl(registryUrl).build();
    ImageDetails imageDetailsWithoutRegistry = ImageDetails.builder().name(imageName).tag(tag).build();

    List<CIK8ContainerParams> containerParamsList = new ArrayList<>();
    containerParamsList.add(
        CIK8ContainerParams.builder()
            .imageDetailsWithConnector(ImageDetailsWithConnector.builder().imageDetails(imageDetails).build())
            .build());
    containerParamsList.add(
        CIK8ContainerParams.builder()
            .imageDetailsWithConnector(
                ImageDetailsWithConnector.builder().imageDetails(imageDetailsWithoutRegistry).build())
            .build());

    CIK8PodParams<CIK8ContainerParams> cik8PodParams = CIK8PodParams.<CIK8ContainerParams>builder()
                                                           .name(podName)
                                                           .namespace(namespace)
                                                           .gitConnector(ConnectorDetails.builder().build())
                                                           .containerParamsList(containerParamsList)
                                                           .build();

    return CIK8BuildTaskParams.builder()
        .k8sConnector(ConnectorDetails.builder().build())
        .cik8PodParams(cik8PodParams)
        .build();
  }

  public static CIK8BuildTaskParams buildTaskParams() {
    ImageDetails imageDetails = ImageDetails.builder().name(imageName).tag(tag).registryUrl(registryUrl).build();
    ImageDetails imageDetailsWithoutRegistry = ImageDetails.builder().name(imageName).tag(tag).build();

    List<CIK8ContainerParams> containerParamsList = new ArrayList<>();
    containerParamsList.add(
        CIK8ContainerParams.builder()
            .name(containerName1)
            .containerType(CIContainerType.ADD_ON)
            .imageDetailsWithConnector(ImageDetailsWithConnector.builder()
                                           .imageConnectorDetails(getDockerConnectorDetails())
                                           .imageDetails(imageDetails)
                                           .build())
            .containerSecrets(
                ContainerSecrets.builder().publishArtifactConnectors(getPublishArtifactConnectorDetails()).build())
            .build());
    containerParamsList.add(
        CIK8ContainerParams.builder()
            .containerSecrets(ContainerSecrets.builder().secretVariableDetails(getSecretVariableDetails()).build())
            .name(containerName2)
            .containerType(CIContainerType.STEP_EXECUTOR)
            .imageDetailsWithConnector(
                ImageDetailsWithConnector.builder().imageDetails(imageDetailsWithoutRegistry).build())
            .build());

    CIK8PodParams<CIK8ContainerParams> cik8PodParams = CIK8PodParams.<CIK8ContainerParams>builder()
                                                           .name(podName)
                                                           .namespace(namespace)
                                                           .gitConnector(getGitConnector())
                                                           .containerParamsList(containerParamsList)
                                                           .build();

    return CIK8BuildTaskParams.builder().k8sConnector(getK8sConnector()).cik8PodParams(cik8PodParams).build();
  }

  private static ConnectorDetails getGitConnector() {
    return ConnectorDetails.builder()
        .connectorDTO(ConnectorDTO.builder()
                          .connectorInfo(
                              ConnectorInfoDTO.builder()
                                  .connectorType(ConnectorType.GIT)
                                  .connectorConfig(
                                      GitConfigDTO.builder()
                                          .gitAuthType(GitAuthType.SSH)
                                          .gitAuth(GitSSHAuthenticationDTO.builder().encryptedSshKey(gitSshKey).build())
                                          .url(gitSshRepoUrl)
                                          .build())
                                  .build())
                          .build())
        .build();
  }

  private static ConnectorDetails getK8sConnector() {
    return ConnectorDetails.builder()
        .connectorDTO(
            ConnectorDTO.builder()
                .connectorInfo(
                    ConnectorInfoDTO.builder().connectorConfig(KubernetesClusterConfigDTO.builder().build()).build())
                .build())
        .encryptedDataDetails(Collections.singletonList(EncryptedDataDetail.builder().build()))
        .build();
  }

  public static CIK8BuildTaskParams buildTaskParamsWithPodSvc() {
    List<EncryptedDataDetail> encryptionDetails = mock(List.class);
    ImageDetails imageDetails = ImageDetails.builder().name(imageName).tag(tag).registryUrl(registryUrl).build();
    ImageDetails imageDetailsWithoutRegistry = ImageDetails.builder().name(imageName).tag(tag).build();

    List<CIK8ContainerParams> containerParamsList = new ArrayList<>();
    containerParamsList.add(
        CIK8ContainerParams.builder()
            .name(containerName1)
            .containerType(CIContainerType.ADD_ON)
            .imageDetailsWithConnector(ImageDetailsWithConnector.builder().imageDetails(imageDetails).build())
            .containerSecrets(
                ContainerSecrets.builder().publishArtifactConnectors(getPublishArtifactConnectorDetails()).build())
            .build());
    containerParamsList.add(
        CIK8ContainerParams.builder()
            .containerSecrets(ContainerSecrets.builder().secretVariableDetails(getSecretVariableDetails()).build())
            .name(containerName2)
            .containerType(CIContainerType.STEP_EXECUTOR)
            .imageDetailsWithConnector(
                ImageDetailsWithConnector.builder().imageDetails(imageDetailsWithoutRegistry).build())
            .build());

    CIK8PodParams<CIK8ContainerParams> cik8PodParams = CIK8PodParams.<CIK8ContainerParams>builder()
                                                           .gitConnector(getGitConnector())
                                                           .branchName(branch_name)
                                                           .commitId(commitId)
                                                           .name(podName)
                                                           .namespace(namespace)
                                                           .containerParamsList(containerParamsList)
                                                           .build();

    CIK8PodParams<CIK8ContainerParams> cik8PodParams1 = CIK8PodParams.<CIK8ContainerParams>builder()
                                                            .name(podName1)
                                                            .namespace(namespace)
                                                            .containerParamsList(containerParamsList)
                                                            .build();

    List<Integer> ports = new ArrayList<>();
    ports.add(8000);
    Map<String, String> selector = new HashMap<>();
    CIK8ServicePodParams cik8ServicePodParams = CIK8ServicePodParams.builder()
                                                    .serviceName(svcName)
                                                    .ports(ports)
                                                    .selectorMap(selector)
                                                    .cik8PodParams(cik8PodParams1)
                                                    .build();

    return CIK8BuildTaskParams.builder()
        .k8sConnector(getK8sConnector())
        .cik8PodParams(cik8PodParams)
        .servicePodParams(Arrays.asList(cik8ServicePodParams))
        .build();
  }

  public static CIK8BuildTaskParams buildTaskParamsWithPVC() {
    ImageDetails imageDetails = ImageDetails.builder().name(imageName).tag(tag).registryUrl(registryUrl).build();
    ImageDetails imageDetailsWithoutRegistry = ImageDetails.builder().name(imageName).tag(tag).build();

    List<CIK8ContainerParams> containerParamsList = new ArrayList<>();
    containerParamsList.add(
        CIK8ContainerParams.builder()
            .name(containerName1)
            .containerType(CIContainerType.ADD_ON)
            .imageDetailsWithConnector(ImageDetailsWithConnector.builder().imageDetails(imageDetails).build())
            .containerSecrets(
                ContainerSecrets.builder().publishArtifactConnectors(getPublishArtifactConnectorDetails()).build())
            .build());
    containerParamsList.add(
        CIK8ContainerParams.builder()
            .containerSecrets(ContainerSecrets.builder().secretVariableDetails(getSecretVariableDetails()).build())
            .name(containerName2)
            .containerType(CIContainerType.STEP_EXECUTOR)
            .imageDetailsWithConnector(
                ImageDetailsWithConnector.builder().imageDetails(imageDetailsWithoutRegistry).build())
            .build());

    CIK8PodParams<CIK8ContainerParams> cik8PodParams = CIK8PodParams.<CIK8ContainerParams>builder()
                                                           .name(podName)
                                                           .namespace(namespace)
                                                           .gitConnector(ConnectorDetails.builder().build())
                                                           .containerParamsList(containerParamsList)
                                                           .pvcParamList(Arrays.asList(PVCParams.builder()
                                                                                           .volumeName(volume1)
                                                                                           .claimName(claimName)
                                                                                           .storageClass(storageClass)
                                                                                           .isPresent(false)
                                                                                           .sizeMib(storageMib)
                                                                                           .build()))
                                                           .build();

    return CIK8BuildTaskParams.builder()
        .k8sConnector(ConnectorDetails.builder().build())
        .cik8PodParams(cik8PodParams)
        .build();
  }

  public static Map<String, ConnectorDetails> getPublishArtifactConnectorDetails() {
    return Collections.singletonMap("docker", getDockerConnectorDetails());
  }

  public static ConnectorDetails getDockerConnectorDetails() {
    return ConnectorDetails.builder()
        .encryptedDataDetails(Collections.singletonList(
            EncryptedDataDetail.builder()
                .encryptedData(EncryptedRecordData.builder().encryptionType(EncryptionType.KMS).build())
                .encryptionConfig(KmsConfig.builder()
                                      .accessKey("accessKey")
                                      .region("us-east-1")
                                      .secretKey("secretKey")
                                      .kmsArn("kmsArn")
                                      .build())
                .build()))
        .connectorDTO(
            ConnectorDTO.builder()
                .connectorInfo(
                    ConnectorInfoDTO.builder()
                        .connectorType(ConnectorType.DOCKER)
                        .connectorConfig(
                            DockerConnectorDTO.builder()
                                .dockerRegistryUrl("https://index.docker.io/v1/")
                                .auth(DockerAuthenticationDTO.builder()
                                          .authType(DockerAuthType.USER_PASSWORD)
                                          .credentials(DockerUserNamePasswordDTO.builder()
                                                           .username("uName")
                                                           .passwordRef(SecretRefData.builder()
                                                                            .decryptedValue("pWord".toCharArray())
                                                                            .build())
                                                           .build())
                                          .build())
                                .build())
                        .build())
                .build())
        .build();
  }

  public static List<SecretVariableDetails> getSecretVariableDetails() {
    List<SecretVariableDetails> secretVariableDetailsList = new ArrayList<>();

    SecretVariableDetails secretVariableDetails =
        SecretVariableDetails.builder()
            .secretVariableDTO(SecretVariableDTO.builder()
                                   .type(SecretVariableDTO.Type.TEXT)
                                   .name("abc")
                                   .secret(SecretRefData.builder().scope(Scope.ACCOUNT).identifier("secretId").build())
                                   .build())
            .encryptedDataDetailList(Collections.singletonList(
                EncryptedDataDetail.builder()
                    .encryptedData(EncryptedRecordData.builder().encryptionType(EncryptionType.KMS).build())
                    .encryptionConfig(KmsConfig.builder()
                                          .accessKey("accessKey")
                                          .region("us-east-1")
                                          .secretKey("secretKey")
                                          .kmsArn("kmsArn")
                                          .build())
                    .build()))
            .build();

    secretVariableDetailsList.add(secretVariableDetails);
    return secretVariableDetailsList;
  }

  public static Map<String, SecretParams> getCustomVarSecret() {
    Map<String, SecretParams> decryptedSecrets = new HashMap<>();
    decryptedSecrets.put("docker",
        SecretParams.builder().type(TEXT).secretKey(SECRET_KEY + "docker").value(encodeBase64("pass")).build());
    return decryptedSecrets;
  }

  public static Map<String, SecretParams> getGcpSecret() {
    Map<String, SecretParams> decryptedSecrets = new HashMap<>();
    decryptedSecrets.put("SECRET_PATH_gcp",
        SecretParams.builder().type(FILE).secretKey("SECRET_PATH_gcp").value(encodeBase64("configFile:{}")).build());
    return decryptedSecrets;
  }
  public static Map<String, SecretParams> getDockerSecret() {
    Map<String, SecretParams> decryptedSecrets = new HashMap<>();
    decryptedSecrets.put("USERNAME_docker",
        SecretParams.builder().type(TEXT).secretKey("USERNAME_docker").value(encodeBase64("uname")).build());
    decryptedSecrets.put("PASSWORD_docker",
        SecretParams.builder().type(TEXT).secretKey("PASSWORD_docker").value(encodeBase64("passw")).build());
    decryptedSecrets.put("ENDPOINT_docker",
        SecretParams.builder().type(TEXT).secretKey("ENDPOINT_docker").value(encodeBase64("endpoint")).build());
    return decryptedSecrets;
  }
  public static Map<String, SecretParams> getPublishArtifactSecrets() {
    Map<String, SecretParams> decryptedSecrets = new HashMap<>();
    decryptedSecrets.putAll(getDockerSecret());
    decryptedSecrets.putAll(getGcpSecret());
    return decryptedSecrets;
  }
}
