package software.wings.scheduler;

import static io.harness.rule.OwnerRule.UTKARSH;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static software.wings.beans.Application.GLOBAL_APP_ID;
import static software.wings.beans.alert.AlertType.InvalidKMS;

import com.google.inject.Inject;

import io.harness.category.element.UnitTests;
import io.harness.rule.Owner;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import software.wings.WingsBaseTest;
import software.wings.beans.VaultConfig;
import software.wings.beans.alert.KmsSetupAlert;
import software.wings.service.intfc.AlertService;
import software.wings.service.intfc.security.VaultService;

import java.time.Duration;

public class VaultSecretManagerRenewalHandlerTest extends WingsBaseTest {
  @Mock private VaultService vaultService;
  @Mock private AlertService alertService;
  @Inject @InjectMocks private VaultSecretManagerRenewalHandler vaultSecretManagerRenewalHandler;

  @Test
  @Owner(developers = UTKARSH)
  @Category(UnitTests.class)
  public void testRenewalForAppRoleVaultConfig_customInterval_shouldSucceed() {
    VaultConfig vaultConfig = getVaultConfigWithAppRole("appRoleId", "secretId");
    vaultConfig.setAccountId("accountId");
    vaultConfig.setRenewalInterval(5);
    vaultConfig.setRenewedAt(System.currentTimeMillis() - Duration.ofMinutes(5).toMillis());
    vaultSecretManagerRenewalHandler.handle(vaultConfig);
    verify(vaultService, times(1)).renewAppRoleClientToken(vaultConfig);
    verify(vaultService, times(0)).renewToken(any());
    verifySuccessAlertInteraction(vaultConfig.getAccountId());
  }

  @Test
  @Owner(developers = UTKARSH)
  @Category(UnitTests.class)
  public void testRenewalForAppRoleVaultConfig_customInterval_shouldNotHappen() {
    VaultConfig vaultConfig = getVaultConfigWithAppRole("appRoleId", "secretId");
    vaultConfig.setAccountId("accountId");
    vaultConfig.setRenewalInterval(5);
    vaultConfig.setRenewedAt(System.currentTimeMillis() - Duration.ofMinutes(2).toMillis());
    vaultSecretManagerRenewalHandler.handle(vaultConfig);
    verifyInteractionWithNoMocks();
  }

  @Test
  @Owner(developers = UTKARSH)
  @Category(UnitTests.class)
  public void testRenewalForAppRoleVaultConfig_defaultInterval_shouldSucceed() {
    VaultConfig vaultConfig = getVaultConfigWithAppRole("appRoleId", "secretId");
    vaultConfig.setAccountId("accountId");
    vaultConfig.setRenewalInterval(0);
    vaultConfig.setRenewedAt(System.currentTimeMillis() - Duration.ofMinutes(15).toMillis());
    vaultSecretManagerRenewalHandler.handle(vaultConfig);
    verify(vaultService, times(1)).renewAppRoleClientToken(vaultConfig);
    verify(vaultService, times(0)).renewToken(any());
    verifySuccessAlertInteraction(vaultConfig.getAccountId());
  }

  @Test
  @Owner(developers = UTKARSH)
  @Category(UnitTests.class)
  public void testRenewalForAppRoleVaultConfig_defaultInterval_shouldNotHappen() {
    VaultConfig vaultConfig = getVaultConfigWithAppRole("appRoleId", "secretId");
    vaultConfig.setRenewalInterval(0);
    vaultConfig.setRenewedAt(System.currentTimeMillis() - Duration.ofMinutes(10).toMillis());
    vaultSecretManagerRenewalHandler.handle(vaultConfig);
    verifyInteractionWithNoMocks();
  }

  @Test
  @Owner(developers = UTKARSH)
  @Category(UnitTests.class)
  public void testRenewalForTokenVaultConfig_disabled_shouldNotHappen() {
    VaultConfig vaultConfig = getVaultConfigWithAuthToken("authToken");
    vaultSecretManagerRenewalHandler.handle(vaultConfig);
    verifyInteractionWithNoMocks();
  }

  @Test
  @Owner(developers = UTKARSH)
  @Category(UnitTests.class)
  public void testRenewalForTokenVaultConfig_alreadyRenewed_shouldNotHappen() {
    VaultConfig vaultConfig = getVaultConfigWithAuthToken("authToken");
    vaultConfig.setRenewalInterval(5);
    vaultConfig.setRenewedAt(System.currentTimeMillis() - Duration.ofMinutes(2).toMillis());
    vaultSecretManagerRenewalHandler.handle(vaultConfig);
    verifyInteractionWithNoMocks();
  }

  @Test
  @Owner(developers = UTKARSH)
  @Category(UnitTests.class)
  public void testRenewalForTokenVaultConfig_shouldSucceed() {
    VaultConfig vaultConfig = getVaultConfigWithAuthToken("authToken");
    vaultConfig.setAccountId("accountId");
    vaultConfig.setRenewalInterval(5);
    vaultConfig.setRenewedAt(System.currentTimeMillis() - Duration.ofMinutes(10).toMillis());
    vaultSecretManagerRenewalHandler.handle(vaultConfig);
    verify(vaultService, times(0)).renewAppRoleClientToken(any());
    verify(vaultService, times(1)).renewToken(vaultConfig);
    verifySuccessAlertInteraction(vaultConfig.getAccountId());
  }

  @Test
  @Owner(developers = UTKARSH)
  @Category(UnitTests.class)
  public void testRenewalForTokenVaultConfig_shouldFail() {
    VaultConfig vaultConfig = getVaultConfigWithAuthToken("authToken");
    vaultConfig.setAccountId("accountId");
    vaultConfig.setRenewalInterval(5);
    vaultConfig.setRenewedAt(System.currentTimeMillis() - Duration.ofMinutes(10).toMillis());
    doThrow(new RuntimeException()).when(vaultService).renewToken(vaultConfig);
    vaultSecretManagerRenewalHandler.handle(vaultConfig);
    verify(vaultService, times(0)).renewAppRoleClientToken(any());
    verify(vaultService, times(1)).renewToken(vaultConfig);
    verifyFailureAlertInteraction(vaultConfig.getAccountId());
  }

  @Test
  @Owner(developers = UTKARSH)
  @Category(UnitTests.class)
  public void testRenewalForAppRoleConfig_shouldFail() {
    VaultConfig vaultConfig = getVaultConfigWithAppRole("appRole", "secretId");
    vaultConfig.setAccountId("accountId");
    vaultConfig.setRenewalInterval(5);
    vaultConfig.setRenewedAt(System.currentTimeMillis() - Duration.ofMinutes(10).toMillis());
    doThrow(new RuntimeException()).when(vaultService).renewAppRoleClientToken(vaultConfig);
    vaultSecretManagerRenewalHandler.handle(vaultConfig);
    verify(vaultService, times(1)).renewAppRoleClientToken(any());
    verify(vaultService, times(0)).renewToken(vaultConfig);
    verifyFailureAlertInteraction(vaultConfig.getAccountId());
  }

  private void verifyInteractionWithNoMocks() {
    verify(vaultService, times(0)).renewAppRoleClientToken(any());
    verify(vaultService, times(0)).renewToken(any());
    verify(alertService, times(0)).closeAlert(any(), any(), any(), any());
    verify(alertService, times(0)).openAlert(any(), any(), any(), any());
  }

  private void verifySuccessAlertInteraction(String accountId) {
    verify(alertService, times(1))
        .closeAlert(eq(accountId), eq(GLOBAL_APP_ID), eq(InvalidKMS), any(KmsSetupAlert.class));
    verify(alertService, times(0)).openAlert(any(), any(), any(), any());
  }

  private void verifyFailureAlertInteraction(String accountId) {
    verify(alertService, times(1))
        .openAlert(eq(accountId), eq(GLOBAL_APP_ID), eq(InvalidKMS), any(KmsSetupAlert.class));
    verify(alertService, times(0)).closeAlert(any(), any(), any(), any());
  }
}
