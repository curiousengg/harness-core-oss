package io.harness.beans;

import static io.harness.annotations.dev.HarnessTeam.DX;

import io.harness.annotations.dev.OwnedBy;
import io.harness.product.ci.scm.proto.ParseWebhookResponse;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
@OwnedBy(DX)
public class WebhookPayload {
  private WebhookGitUser webhookGitUser;
  private Repository repository;
  private WebhookEvent webhookEvent;
  private ParseWebhookResponse parseWebhookResponse;
}
