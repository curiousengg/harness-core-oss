package software.wings.resources;

import static software.wings.security.PermissionAttribute.ResourceType.USER;

import com.google.inject.Inject;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import io.harness.ccm.views.entities.CEReportSchedule;
import io.harness.ccm.views.service.CEReportScheduleService;
import io.harness.eraro.ErrorCode;
import io.harness.eraro.Level;
import io.harness.eraro.ResponseMessage;
import io.harness.rest.RestResponse;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.web.bind.annotation.RequestBody;
import software.wings.security.annotations.Scope;

import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api("ceReportSchedule")
@Path("/ceReportSchedule")
@Produces("application/json")
@Scope(USER)
@Slf4j
public class CEReportScheduleResource {
  private CEReportScheduleService ceReportScheduleService;

  @Inject
  public CEReportScheduleResource(CEReportScheduleService ceReportScheduleService) {
    this.ceReportScheduleService = ceReportScheduleService;
  }

  @GET
  @Timed
  @Path("{accountId}")
  @ExceptionMetered
  public Response getReportSetting(@QueryParam("viewId") String viewId, @QueryParam("reportId") String reportId,
      @PathParam("accountId") String accountId) {
    if (viewId != null) {
      RestResponse rr =
          new RestResponse<List<CEReportSchedule>>(ceReportScheduleService.getReportSettingByView(viewId, accountId));
      return prepareResponse(rr, Response.Status.OK);
    } else if (reportId != null) {
      List<CEReportSchedule> ceList = new ArrayList<>();
      CEReportSchedule rep = ceReportScheduleService.get(reportId, accountId);
      if (rep != null) {
        ceList.add(rep);
      }
      RestResponse rr = new RestResponse<List<CEReportSchedule>>(ceList);
      return prepareResponse(rr, Response.Status.OK);
    }
    // INVALID_REQUEST
    RestResponse rr = new RestResponse<>();
    addResponseMessage(
        rr, ErrorCode.INVALID_REQUEST, Level.ERROR, "ERROR: Invalid request. Either 'viewId' or 'reportId' is needed");
    return prepareResponse(rr, Response.Status.BAD_REQUEST);
  }

  @DELETE
  @Timed
  @Path("{accountId}")
  @ExceptionMetered
  public Response deleteReportSetting(@QueryParam("reportId") String reportId, @QueryParam("viewId") String viewId,
      @PathParam("accountId") String accountId) {
    if (viewId != null) {
      ceReportScheduleService.deleteAllByView(viewId, accountId);
      RestResponse rr = new RestResponse("Successfully deleted the record");
      return prepareResponse(rr, Response.Status.OK);
    } else if (reportId != null) {
      ceReportScheduleService.delete(reportId, accountId);
      RestResponse rr = new RestResponse("Successfully deleted the record");
      return prepareResponse(rr, Response.Status.OK);
    }
    // INVALID_REQUEST
    RestResponse rr = new RestResponse();
    addResponseMessage(
        rr, ErrorCode.INVALID_REQUEST, Level.ERROR, "ERROR: Invalid request. Either 'viewId' or 'reportId' is needed");
    return prepareResponse(rr, Response.Status.BAD_REQUEST);
  }

  @POST
  @Path("{accountId}")
  @Timed
  @ExceptionMetered
  public Response createReportSetting(
      @PathParam("accountId") String accountId, @Valid @RequestBody CEReportSchedule schedule) {
    List<CEReportSchedule> ceList = new ArrayList<>();
    try {
      CronSequenceGenerator cronSequenceGenerator = new CronSequenceGenerator(schedule.getUserCron());
      ceList.add(ceReportScheduleService.createReportSetting(cronSequenceGenerator, accountId, schedule));
      RestResponse rr = new RestResponse<List<CEReportSchedule>>(ceList);
      return prepareResponse(rr, Response.Status.OK);
    } catch (IllegalArgumentException e) {
      logger.warn(String.valueOf(e));
      RestResponse rr = new RestResponse();
      addResponseMessage(
          rr, ErrorCode.INVALID_REQUEST, Level.ERROR, "ERROR: Invalid request. Schedule provided is invalid");
      return prepareResponse(rr, Response.Status.BAD_REQUEST);
    }
  }

  @PUT
  @Path("{accountId}")
  @Timed
  @ExceptionMetered
  public Response updateReportSetting(
      @PathParam("accountId") String accountId, @Valid @RequestBody CEReportSchedule schedule) {
    try {
      CronSequenceGenerator cronSequenceGenerator = new CronSequenceGenerator(schedule.getUserCron());
      RestResponse rr = new RestResponse<List<CEReportSchedule>>(
          ceReportScheduleService.update(cronSequenceGenerator, accountId, schedule));
      return prepareResponse(rr, Response.Status.OK);
    } catch (IllegalArgumentException e) {
      logger.warn(String.valueOf(e));
      RestResponse rr = new RestResponse();
      addResponseMessage(
          rr, ErrorCode.INVALID_REQUEST, Level.ERROR, "ERROR: Invalid request. Schedule provided is invalid");
      return prepareResponse(rr, Response.Status.BAD_REQUEST);
    }
  }

  private static void addResponseMessage(RestResponse rr, ErrorCode errorCode, Level level, String message) {
    ResponseMessage rm = ResponseMessage.builder().code(errorCode).level(level).message(message).build();

    List<ResponseMessage> responseMessages = rr.getResponseMessages();
    responseMessages.add(rm);
    rr.setResponseMessages(responseMessages);
  }

  private Response prepareResponse(RestResponse restResponse, Response.Status status) {
    return Response.status(status).entity(restResponse).type(MediaType.APPLICATION_JSON).build();
  }
}
