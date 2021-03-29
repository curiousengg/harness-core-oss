package io.harness.audit;

import static io.harness.annotations.dev.HarnessTeam.PL;

import io.harness.annotations.dev.OwnedBy;

import lombok.experimental.UtilityClass;

@OwnedBy(PL)
@UtilityClass
public class AuditCommonConstants {
  public static final String MODULE = "module";
  public static final String ACTION = "action";
  public static final String ENVIRONMENT_IDENTIFIER = "environmentIdentifier";
  public static final String IDENTIFIER = "identifier";
  public static final String TYPE = "type";
}
