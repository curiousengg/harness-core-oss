package io.harness.pms.helpers;

import static io.harness.security.dto.PrincipalType.USER;

import io.harness.PipelineServiceConfiguration;
import io.harness.beans.EmbeddedUser;
import io.harness.data.structure.EmptyPredicate;
import io.harness.exception.InvalidRequestException;
import io.harness.ng.core.user.User;
import io.harness.ng.core.user.remote.UserClient;
import io.harness.remote.client.RestClientUtils;
import io.harness.security.SecurityContextBuilder;
import io.harness.security.dto.UserPrincipal;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import java.util.List;

@Singleton
public class CurrentUserHelper {
  private static final EmbeddedUser DEFAULT_EMBEDDED_USER =
      EmbeddedUser.builder().uuid("lv0euRhKRCyiXWzS7pOg6g").name("Admin").email("admin@harness.io").build();

  @Inject private PipelineServiceConfiguration configuration;
  @Inject private UserClient userClient;

  public EmbeddedUser getFromSecurityContext() {
    if (!configuration.isEnableAuth()) {
      return DEFAULT_EMBEDDED_USER;
    }
    if (SecurityContextBuilder.getPrincipal() == null
        || !USER.equals(SecurityContextBuilder.getPrincipal().getType())) {
      throw new InvalidRequestException("Unable to fetch current user");
    }

    UserPrincipal userPrincipal = (UserPrincipal) SecurityContextBuilder.getPrincipal();
    String userId = userPrincipal.getName();
    List<User> users = RestClientUtils.getResponse(userClient.getUsersByIds(Collections.singletonList(userId)));
    if (EmptyPredicate.isEmpty(users)) {
      throw new InvalidRequestException(String.format("Invalid user: %s", userId));
    }

    User user = users.get(0);
    return EmbeddedUser.builder().uuid(user.getUuid()).name(user.getName()).email(user.getEmail()).build();
  }
}
