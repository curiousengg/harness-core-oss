package io.harness.secretkey;

import static io.harness.rule.OwnerRule.MOHIT_GARG;

import static org.assertj.core.api.Assertions.assertThat;

import io.harness.SMCoreTestBase;
import io.harness.category.element.UnitTests;
import io.harness.rule.Owner;
import io.harness.security.encryption.SecretKeyDTO;

import com.google.inject.Inject;
import java.util.Optional;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class AESSecretKeyServiceTest extends SMCoreTestBase {
  @Inject AESSecretKeyServiceImpl aesSecretKeyService;

  @Test
  @Owner(developers = MOHIT_GARG)
  @Category(UnitTests.class)
  public void testAESSecretKeyAlgorithm() {
    assertThat(aesSecretKeyService.getAlgorithm()).isEqualTo(SecretKeyConstants.AES_ENCRYPTION_ALGORITHM);
  }

  @Test
  @Owner(developers = MOHIT_GARG)
  @Category(UnitTests.class)
  public void testCreateAESSecretKey() {
    SecretKeyDTO secretKey = aesSecretKeyService.createSecretKey();
    assertThat(secretKey.getSecretKeySpec().getAlgorithm()).isEqualTo(aesSecretKeyService.getAlgorithm());
  }

  @Test
  @Owner(developers = MOHIT_GARG)
  @Category(UnitTests.class)
  public void testGetAESSecretKey() {
    SecretKeyDTO createdSecretKey = aesSecretKeyService.createSecretKey();
    Optional<SecretKeyDTO> fetchedSecretKey = aesSecretKeyService.getSecretKey(createdSecretKey.getUuid());
    assertThat(fetchedSecretKey.isPresent()).isTrue();
  }
}
