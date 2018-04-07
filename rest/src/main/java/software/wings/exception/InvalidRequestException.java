package software.wings.exception;

import static software.wings.beans.ErrorCode.INVALID_REQUEST;

public class InvalidRequestException extends WingsException {
  public InvalidRequestException(String message, ReportTarget reportTarget) {
    super(INVALID_REQUEST, reportTarget);
    super.addParam("message", message);
  }
}
