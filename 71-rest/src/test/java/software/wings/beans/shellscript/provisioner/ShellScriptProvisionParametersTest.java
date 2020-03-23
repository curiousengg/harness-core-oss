package software.wings.beans.shellscript.provisioner;

import static io.harness.rule.OwnerRule.ANSHUL;
import static org.assertj.core.api.Assertions.assertThat;

import io.harness.category.element.UnitTests;
import io.harness.delegate.beans.executioncapability.ExecutionCapability;
import io.harness.delegate.beans.executioncapability.HttpConnectionExecutionCapability;
import io.harness.rule.Owner;
import io.harness.security.encryption.EncryptedDataDetail;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import software.wings.WingsBaseTest;
import software.wings.beans.KmsConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShellScriptProvisionParametersTest extends WingsBaseTest {
  @Test
  @Owner(developers = ANSHUL)
  @Category(UnitTests.class)
  public void testFetchRequiredExecutionCapabilities() {
    Map<String, EncryptedDataDetail> encryptedVariables = new HashMap<>();

    encryptedVariables.put("abc",
        EncryptedDataDetail.builder()
            .encryptionConfig(KmsConfig.builder()
                                  .accessKey("accessKey")
                                  .region("us-east-1")
                                  .secretKey("secretKey")
                                  .kmsArn("kmsArn")
                                  .build())
            .build());

    ShellScriptProvisionParameters shellScriptProvisionParameters =
        ShellScriptProvisionParameters.builder().encryptedVariables(encryptedVariables).build();

    List<ExecutionCapability> executionCapabilities =
        shellScriptProvisionParameters.fetchRequiredExecutionCapabilities();
    assertThat(executionCapabilities).isNotEmpty();
    assertThat(((HttpConnectionExecutionCapability) executionCapabilities.get(0)).getHost())
        .isEqualTo("kms.us-east-1.amazonaws.com");
  }
}
