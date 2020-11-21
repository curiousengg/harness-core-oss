package io.harness.cvng.core.resources;

import io.harness.cvng.beans.AppdynamicsValidationResponse;
import io.harness.cvng.beans.appd.AppDynamicsApplication;
import io.harness.cvng.beans.appd.AppDynamicsTier;
import io.harness.cvng.core.entities.MetricPack;
import io.harness.cvng.core.services.api.AppDynamicsService;
import io.harness.rest.RestResponse;
import io.harness.security.annotations.NextGenManagerAuth;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import com.google.inject.Inject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import retrofit2.http.Body;

@Api("appdynamics")
@Path("/appdynamics")
@Produces("application/json")
@NextGenManagerAuth
public class AppDynamicsResource {
  @Inject private AppDynamicsService appDynamicsService;
  @POST
  @Path("/metric-data")
  @Timed
  @ExceptionMetered
  @ApiOperation(value = "get metric data for given metric packs", nickname = "getAppdynamicsMetricData")
  public RestResponse<Set<AppdynamicsValidationResponse>> getMetricData(
      @QueryParam("accountId") @NotNull String accountId, @QueryParam("orgIdentifier") @NotNull String orgIdentifier,
      @QueryParam("projectIdentifier") @NotNull String projectIdentifier,
      @QueryParam("connectorIdentifier") @NotNull String connectorIdentifier,
      @QueryParam("appdAppId") @NotNull long appdAppId, @QueryParam("appdTierId") @NotNull long appdTierId,
      @QueryParam("requestGuid") @NotNull String requestGuid, @NotNull @Valid @Body List<MetricPack> metricPacks) {
    return new RestResponse<>(appDynamicsService.getMetricPackData(accountId, connectorIdentifier, orgIdentifier,
        projectIdentifier, appdAppId, appdTierId, requestGuid, metricPacks));
  }

  @GET
  @Path("/applications")
  @Timed
  @ExceptionMetered
  @ApiOperation(value = "get all appdynamics applications", nickname = "getAppdynamicsApplications")
  public RestResponse<List<AppDynamicsApplication>> getAllApplications(
      @NotNull @QueryParam("accountId") String accountId,
      @NotNull @QueryParam("connectorIdentifier") final String connectorIdentifier,
      @QueryParam("orgIdentifier") @NotNull String orgIdentifier,
      @QueryParam("projectIdentifier") @NotNull String projectIdentifier) {
    return new RestResponse<>(
        appDynamicsService.getApplications(accountId, connectorIdentifier, orgIdentifier, projectIdentifier));
  }

  @GET
  @Path("/tiers")
  @Timed
  @ExceptionMetered
  @ApiOperation(value = "get all appdynamics tiers for an application", nickname = "getAppdynamicsTiers")
  public RestResponse<Set<AppDynamicsTier>> getAllTiers(@NotNull @QueryParam("accountId") String accountId,
      @NotNull @QueryParam("connectorIdentifier") final String connectorIdentifier,
      @QueryParam("orgIdentifier") @NotNull String orgIdentifier,
      @QueryParam("projectIdentifier") @NotNull String projectIdentifier,
      @NotNull @QueryParam("appDynamicsAppId") long appdynamicsAppId) {
    return new RestResponse<>(appDynamicsService.getTiers(
        accountId, connectorIdentifier, orgIdentifier, projectIdentifier, appdynamicsAppId));
  }
}
