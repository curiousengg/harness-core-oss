package io.harness.secrets.validation.validators;

import static io.harness.annotations.dev.HarnessTeam.PL;
import static io.harness.data.structure.EmptyPredicate.isNotEmpty;
import static io.harness.eraro.ErrorCode.GCP_SECRET_OPERATION_ERROR;
import static io.harness.exception.WingsException.USER_SRE;

import io.harness.annotations.dev.OwnedBy;
import io.harness.beans.EncryptedData;
import io.harness.beans.SecretFile;
import io.harness.beans.SecretManagerConfig;
import io.harness.beans.SecretText;
import io.harness.exception.SecretManagementException;
import io.harness.secrets.SecretsDao;
import io.harness.secrets.validation.BaseSecretValidator;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.regex.Pattern;
import javax.validation.executable.ValidateOnExecution;

@ValidateOnExecution
@Singleton
@OwnedBy(PL)
public class GcpSecretManagerValidator extends BaseSecretValidator {
  private static final Pattern GCP_SECRET_NAME_PATTERN = Pattern.compile("^[\\w-_]+$");
  private static final int GCP_SECRET_CONTENT_SIZE_LIMIT = 65536;
  private static final String GCP_SECRET_NAME_ERROR =
      "Secret names can only contain English letters (A-Z), numbers (0-9), dashes (-), and underscores (_)";
  private static final String GCP_SECRET_FILE_SIZE_ERROR = "File size limit is 64 KiB";
  private static final String GCP_SECRET_CONTENT_SIZE_ERROR =
      "Gcp Secrets Manager limits secret value to " + GCP_SECRET_CONTENT_SIZE_LIMIT + " bytes.";

  @Inject
  public GcpSecretManagerValidator(SecretsDao secretsDao) {
    super(secretsDao);
  }

  private void verifyInlineSecret(SecretText secretText) {
    validateSecretName(secretText.getName());
    verifyValueSizeWithinLimit(secretText.getValue());
  }

  private void validateSecretName(String name) {
    if (!GCP_SECRET_NAME_PATTERN.matcher(name).find()) {
      throw new SecretManagementException(GCP_SECRET_OPERATION_ERROR, GCP_SECRET_NAME_ERROR, USER_SRE);
    }
  }

  private void verifyFileSizeWithinLimit(byte[] fileContent) {
    if (isNotEmpty(fileContent) && fileContent.length > GCP_SECRET_CONTENT_SIZE_LIMIT) {
      throw new SecretManagementException(GCP_SECRET_OPERATION_ERROR, GCP_SECRET_FILE_SIZE_ERROR, USER_SRE);
    }
  }

  private void verifyValueSizeWithinLimit(String secretText) {
    if (isNotEmpty(secretText) && secretText.getBytes().length > GCP_SECRET_CONTENT_SIZE_LIMIT) {
      throw new SecretManagementException(GCP_SECRET_OPERATION_ERROR, GCP_SECRET_CONTENT_SIZE_ERROR, USER_SRE);
    }
  }

  @Override
  public void validateSecretText(String accountId, SecretText secretText, SecretManagerConfig secretManagerConfig) {
    super.validateSecretText(accountId, secretText, secretManagerConfig);
    if (secretText.isInlineSecret()) {
      verifyInlineSecret(secretText);
    }
  }

  @Override
  public void validateSecretTextUpdate(
      SecretText secretText, EncryptedData existingRecord, SecretManagerConfig secretManagerConfig) {
    super.validateSecretTextUpdate(secretText, existingRecord, secretManagerConfig);
    if (secretText.isInlineSecret()) {
      verifyInlineSecret(secretText);
    }
  }

  @Override
  public void validateSecretFile(String accountId, SecretFile secretFile, SecretManagerConfig secretManagerConfig) {
    super.validateSecretFile(accountId, secretFile, secretManagerConfig);
    validateSecretName(secretFile.getName());
    verifyFileSizeWithinLimit(secretFile.getFileContent());
  }

  @Override
  public void validateSecretFileUpdate(
      SecretFile secretFile, EncryptedData existingRecord, SecretManagerConfig secretManagerConfig) {
    super.validateSecretFileUpdate(secretFile, existingRecord, secretManagerConfig);
    validateSecretName(secretFile.getName());
    verifyFileSizeWithinLimit(secretFile.getFileContent());
  }
}
