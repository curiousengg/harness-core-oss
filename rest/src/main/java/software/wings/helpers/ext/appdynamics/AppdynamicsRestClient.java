package software.wings.helpers.ext.appdynamics;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;
import software.wings.service.impl.appdynamics.AppdynamicsMetric;
import software.wings.service.impl.appdynamics.AppdynamicsMetricData;
import software.wings.service.impl.appdynamics.AppdynamicsTier;
import software.wings.service.impl.newrelic.NewRelicApplication;

import java.util.List;
import java.util.Set;

/**
 * Created by rsingh on 4/17/17.
 */
public interface AppdynamicsRestClient {
  /**
   * Lists all the applications of appdynamics
   *
   * @param authorization the authorization
   * @return the call
   */
  @GET("rest/applications?output=json")
  Call<List<NewRelicApplication>> listAllApplications(@Header("Authorization") String authorization);

  /**
   * Lists all the tiers of an application in appdynamics
   *
   * @param authorization
   * @param appdynamicsAppId
   * @return
   */
  @GET("rest/applications/{appdynamicsAppId}/tiers?output=json")
  Call<Set<AppdynamicsTier>> listTiers(
      @Header("Authorization") String authorization, @Path("appdynamicsAppId") long appdynamicsAppId);

  /**
   * Get all the details of a tier
   *
   * @param authorization
   * @param appdynamicsAppId
   * @return
   */
  @GET("rest/applications/{appdynamicsAppId}/tiers/{tierId}?output=json")
  Call<List<AppdynamicsTier>> getTierDetails(@Header("Authorization") String authorization,
      @Path("appdynamicsAppId") long appdynamicsAppId, @Path("tierId") long tierId);

  /**
   * List the metrices of appdynamics application for a give path
   *
   * @param authorization the authorization
   * @param metricPath    the path to look for metrices
   * @return the call
   */
  @GET("rest/applications/{applicationId}/metrics?output=JSON")
  Call<List<AppdynamicsMetric>> listMetrices(@Header("Authorization") String authorization,
      @Path("applicationId") long applicationId, @Query("metric-path") String metricPath);

  /**
   * Get the metric data points of appdynamics application for a give path
   *
   * @param authorization the authorization
   * @param metricPath    the path to look for metrices
   * @return the call
   */
  @GET("rest/applications/{applicationId}/metric-data?output=JSON&time-range-type=BEFORE_NOW&rollup=false")
  Call<List<AppdynamicsMetricData>> getMetricData(@Header("Authorization") String authorization,
      @Path("applicationId") long applicationId, @Query("metric-path") String metricPath,
      @Query("duration-in-mins") int durantionInMinutes);
}
