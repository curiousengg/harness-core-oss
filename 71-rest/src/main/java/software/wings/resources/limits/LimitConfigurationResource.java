package software.wings.resources.limits;

import static io.harness.logging.AutoLogContext.OverrideBehavior.OVERRIDE_ERROR;

import com.google.inject.Inject;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import io.harness.exception.InvalidRequestException;
import io.harness.exception.UnauthorizedException;
import io.harness.exception.WingsException;
import io.harness.limits.ActionType;
import io.harness.limits.configuration.LimitConfigurationService;
import io.harness.limits.impl.model.RateLimit;
import io.harness.limits.impl.model.StaticLimit;
import io.harness.limits.lib.Limit;
import io.harness.logging.AccountLogContext;
import io.harness.logging.AutoLogContext;
import io.harness.rest.RestResponse;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.http.Body;
import software.wings.beans.User;
import software.wings.security.PermissionAttribute.ResourceType;
import software.wings.security.UserThreadLocal;
import software.wings.security.annotations.Scope;
import software.wings.service.intfc.HarnessUserGroupService;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

@Api("limits")
@Path("/limits")
@Produces("application/json")
@Scope(ResourceType.LIMIT)
public class LimitConfigurationResource {
  private static final Logger log = LoggerFactory.getLogger(LimitConfigurationResource.class);

  @Inject private LimitConfigurationService limitsService;
  @Inject private HarnessUserGroupService harnessUserGroupService;

  @POST
  @Path("configure/static-limit")
  @Timed
  @ExceptionMetered
  public RestResponse<Boolean> configureStaticLimit(
      @QueryParam("accountId") String accountId, @QueryParam("action") String action, @Body StaticLimit limit) {
    try (AutoLogContext ignore1 = new AccountLogContext(accountId, OVERRIDE_ERROR)) {
      checkPermissions(currentUser());
      Boolean configured = configure(accountId, action, limit);
      return new RestResponse<>(configured);
    }
  }

  @POST
  @Path("configure/rate-limit")
  @Timed
  @ExceptionMetered
  public RestResponse<Boolean> configureRateLimit(
      @QueryParam("accountId") String accountId, @QueryParam("action") String action, @Body RateLimit limit) {
    try (AutoLogContext ignore1 = new AccountLogContext(accountId, OVERRIDE_ERROR)) {
      checkPermissions(currentUser());
      Boolean configured = configure(accountId, action, limit);
      return new RestResponse<>(configured);
    }
  }

  private User currentUser() {
    User user = UserThreadLocal.get();
    if (null == user) {
      throw new UnauthorizedException("User does not exist", WingsException.USER);
    }

    return user;
  }

  private void checkPermissions(User user) {
    boolean isHarnessSupportUser = harnessUserGroupService.isHarnessSupportUser(user.getUuid());
    if (!isHarnessSupportUser) {
      throw new UnauthorizedException("You don't have the permissions to perform this action.", WingsException.USER);
    }
  }

  private Boolean configure(String accountId, String action, Limit limit) {
    ActionType actionType;

    try {
      actionType = ActionType.valueOf(action);
    } catch (IllegalArgumentException e) {
      throw new InvalidRequestException("Invalid action specified: " + action);
    }

    return limitsService.configure(accountId, actionType, limit);
  }
}
