/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package io.harness.logstreaming;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.network.SafeHttpCall;

import java.util.Collections;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@OwnedBy(HarnessTeam.PIPELINE)
@Data
@Builder
@Slf4j
public class LogStreamingStepClientImpl implements ILogStreamingStepClient {
  public static final String COMMAND_UNIT_PLACEHOLDER = "-commandUnit:%s";

  private final LogStreamingClient logStreamingClient;
  private final LogStreamingSanitizer logStreamingSanitizer;
  private final String token;
  private final String accountId;
  private final String baseLogKey;

  @Override
  public void openStream(String logKeySuffix) {
    try {
      String logKey = generateLogKey(baseLogKey, logKeySuffix);
      SafeHttpCall.executeWithExceptions(logStreamingClient.openLogStream(token, accountId, logKey));
    } catch (Exception ex) {
      log.error("Unable to open log stream for account {} and logKeySuffix {} ", accountId, logKeySuffix, ex);
    }
  }

  @Override
  public void closeStream(String logKeySuffix) {
    // we don't want steps to hang because of any log reasons.
    try {
      String logKey = generateLogKey(baseLogKey, logKeySuffix);
      SafeHttpCall.executeWithExceptions(logStreamingClient.closeLogStream(token, accountId, logKey, true));
    } catch (Exception ex) {
      log.error("Unable to close log stream for account {} and logKeySuffix {} ", accountId, logKeySuffix, ex);
    }
  }

  @Override
  public void writeLogLine(LogLine logLine, String logKeySuffix) {
    // we don't want steps to hang because of any log reasons.
    try {
      String logKey = generateLogKey(baseLogKey, logKeySuffix);
      logStreamingSanitizer.sanitizeLogMessage(logLine);
      LogStreamingHelper.colorLog(logLine);

      SafeHttpCall.executeWithExceptions(
          logStreamingClient.pushMessage(token, accountId, logKey, Collections.singletonList(logLine)));
    } catch (Exception ex) {
      log.error("Unable to push message to log stream for account {} and logKeySuffix {}", accountId, logKeySuffix, ex);
    }
  }

  @Override
  public void closeAllOpenStreamsWithPrefix(String prefix) {
    // we don't want steps to hang because of any log reasons.
    try {
      SafeHttpCall.executeWithExceptions(
          logStreamingClient.closeLogStreamWithPrefix(token, accountId, prefix, true, true));
    } catch (Exception ex) {
      log.error("Unable to close log stream for account {} and logKeySuffix {} ", accountId, prefix, ex);
    }
  }

  private String generateLogKey(String baseLogKey, String logKeySuffix) {
    return logKeySuffix == null ? baseLogKey
                                : LogStreamingHelper.generateLogKeyGivenCommandUnit(baseLogKey, logKeySuffix);
  }
}
