/**
 *
 */

package software.wings.resources;

import static software.wings.security.PermissionAttribute.ResourceType.PIPELINE;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import software.wings.beans.EntityType;
import software.wings.beans.Pipeline;
import software.wings.beans.RestResponse;
import software.wings.beans.Variable;
import software.wings.dl.PageRequest;
import software.wings.dl.PageResponse;
import software.wings.security.annotations.AuthRule;
import software.wings.service.intfc.PipelineService;
import software.wings.service.intfc.WorkflowService;
import software.wings.sm.StateTypeScope;
import software.wings.stencils.Stencil;

import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

/**
 * The Class PipelineResource.
 *
 * @author Rishi
 */
@Api("pipelines")
@Path("/pipelines")
@Produces("application/json")
@AuthRule(value = PIPELINE)
public class PipelineResource {
  private WorkflowService workflowService;
  private PipelineService pipelineService;

  /**
   * Instantiates a new pipeline resource.
   *
   * @param workflowService the workflow service
   * @param pipelineService the pipeline service
   */
  @Inject
  public PipelineResource(WorkflowService workflowService, PipelineService pipelineService) {
    this.workflowService = workflowService;
    this.pipelineService = pipelineService;
  }

  /**
   * List.
   *
   * @param appIds       the app ids
   * @param pageRequest the page request
   * @return the rest response
   */
  @GET
  @Timed
  @ExceptionMetered
  @AuthRule(value = PIPELINE)
  public RestResponse<PageResponse<Pipeline>> list(
      @QueryParam("appId") List<String> appIds, @BeanParam PageRequest<Pipeline> pageRequest) {
    return new RestResponse<>(pipelineService.listPipelines(pageRequest, true));
  }

  /**
   * Read.
   *
   * @param appId      the app id
   * @param pipelineId the pipeline id
   * @return the rest response
   */
  @GET
  @Path("{pipelineId}")
  @Timed
  @ExceptionMetered
  @AuthRule(value = PIPELINE)
  public RestResponse<Pipeline> read(@QueryParam("appId") String appId, @PathParam("pipelineId") String pipelineId,
      @QueryParam("withServices") boolean withServices) {
    return new RestResponse<>(pipelineService.readPipeline(appId, pipelineId, withServices));
  }

  /**
   * Creates the.
   *
   * @param appId    the app id
   * @param pipeline the pipeline
   * @return the rest response
   */
  @POST
  @Timed
  @ExceptionMetered
  @AuthRule(value = PIPELINE)
  public RestResponse<Pipeline> create(@QueryParam("appId") String appId, Pipeline pipeline) {
    pipeline.setAppId(appId);
    return new RestResponse<>(pipelineService.createPipeline(pipeline));
  }

  /**
   * Update.
   *
   * @param appId      the app id
   * @param pipelineId the pipeline id
   * @param pipeline   the pipeline
   * @return the rest response
   */
  @PUT
  @Path("{pipelineId}")
  @Timed
  @ExceptionMetered
  @AuthRule(value = PIPELINE)
  public RestResponse<Pipeline> update(
      @QueryParam("appId") String appId, @PathParam("pipelineId") String pipelineId, Pipeline pipeline) {
    pipeline.setAppId(appId);
    pipeline.setUuid(pipelineId);
    return new RestResponse<>(pipelineService.updatePipeline(pipeline));
  }

  @POST
  @Path("{pipelineId}/clone")
  @Timed
  @ExceptionMetered
  @AuthRule(value = PIPELINE)
  public RestResponse<Pipeline> read(
      @QueryParam("appId") String appId, @PathParam("pipelineId") String pipelineId, Pipeline pipeline) {
    pipeline.setAppId(appId);
    return new RestResponse<>(pipelineService.clonePipeline(pipelineId, pipeline));
  }

  /**
   * Delete.
   *
   * @param appId      the app id
   * @param pipelineId the pipeline id
   * @param pipeline   the pipeline
   * @return the rest response
   */
  @DELETE
  @Path("{pipelineId}")
  @Timed
  @ExceptionMetered
  @AuthRule(value = PIPELINE)
  public RestResponse delete(
      @QueryParam("appId") String appId, @PathParam("pipelineId") String pipelineId, Pipeline pipeline) {
    pipelineService.deletePipeline(appId, pipelineId);
    return new RestResponse();
  }

  /**
   * Stencils rest response.
   *
   * @param appId the app id
   * @param envId the env id
   * @return the rest response
   */
  @GET
  @Path("stencils")
  @Timed
  @ExceptionMetered
  @AuthRule(value = PIPELINE)
  public RestResponse<List<Stencil>> stencils(@QueryParam("appId") String appId, @QueryParam("envId") String envId) {
    return new RestResponse<>(workflowService.stencils(appId, null, null, StateTypeScope.PIPELINE_STENCILS)
                                  .get(StateTypeScope.PIPELINE_STENCILS));
  }

  /**
   * Required args rest response.
   *
   * @param appId         the app id
   * @param pipelineId    the pipelineId
   * @return the rest response
   */
  @GET
  @Path("required-entities")
  @Timed
  @ExceptionMetered
  @AuthRule(value = PIPELINE)
  public RestResponse<List<EntityType>> requiredEntities(
      @QueryParam("appId") String appId, @QueryParam("pipelineId") String pipelineId) {
    return new RestResponse<>(pipelineService.getRequiredEntities(appId, pipelineId));
  }

  /**
   * Update.
   *
   * @param appId         the app id
   * @param pipelineId    the orchestration id
   * @param variables     the pipeline variables
   * @return the rest response
   */
  @PUT
  @Path("{pipelineId}/variables")
  @Timed
  @ExceptionMetered
  public RestResponse<List<Variable>> updateUserVariables(
      @QueryParam("appId") String appId, @PathParam("pipelineId") String pipelineId, List<Variable> variables) {
    return new RestResponse<>(pipelineService.updateVariables(appId, pipelineId, variables));
  }
}
