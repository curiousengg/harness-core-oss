package io.harness.encryptors;

import static io.harness.annotations.dev.HarnessTeam.PL;
import static io.harness.eraro.ErrorCode.SECRET_MANAGEMENT_ERROR;
import static io.harness.exception.WingsException.USER;
import static io.harness.security.encryption.EncryptionType.AWS_SECRETS_MANAGER;
import static io.harness.security.encryption.EncryptionType.AZURE_VAULT;
import static io.harness.security.encryption.EncryptionType.CYBERARK;
import static io.harness.security.encryption.EncryptionType.VAULT;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

import io.harness.annotations.dev.OwnedBy;
import io.harness.exception.SecretManagementDelegateException;
import io.harness.security.encryption.EncryptionType;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

@Singleton
@OwnedBy(PL)
public class VaultEncryptorsRegistry {
  private final Injector injector;
  private final Map<EncryptionType, Encryptors> registeredEncryptors;

  @Inject
  public VaultEncryptorsRegistry(Injector injector) {
    this.injector = injector;
    registeredEncryptors = new EnumMap<>(EncryptionType.class);
    registeredEncryptors.put(VAULT, Encryptors.HASHICORP_VAULT_ENCRYPTOR);
    registeredEncryptors.put(AWS_SECRETS_MANAGER, Encryptors.AWS_VAULT_ENCRYPTOR);
    registeredEncryptors.put(AZURE_VAULT, Encryptors.AZURE_VAULT_ENCRYPTOR);
    registeredEncryptors.put(CYBERARK, Encryptors.CYBERARK_VAULT_ENCRYPTOR);
  }

  public VaultEncryptor getVaultEncryptor(EncryptionType encryptionType) {
    return Optional.ofNullable(registeredEncryptors.get(encryptionType))
        .flatMap(type -> Optional.of(injector.getInstance(Key.get(VaultEncryptor.class, Names.named(type.getName())))))
        .<SecretManagementDelegateException>orElseThrow(() -> {
          throw new SecretManagementDelegateException(SECRET_MANAGEMENT_ERROR,
              String.format("No encryptor is registered for encryption type %s", encryptionType), USER);
        });
  }
}
