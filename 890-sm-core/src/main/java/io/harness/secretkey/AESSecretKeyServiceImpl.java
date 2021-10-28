package io.harness.secretkey;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;

@OwnedBy(HarnessTeam.PL)
public class AESSecretKeyServiceImpl extends AbstractSecretKeyServiceImpl {
  @Override
  protected String getAlgorithm() {
    return "AES";
  }
}
