/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.gitsync.common.scmerrorhandling.handlers.github;

import static io.harness.annotations.dev.HarnessTeam.PL;
import static io.harness.eraro.ErrorCode.UNEXPECTED;

import io.harness.annotations.dev.OwnedBy;
import io.harness.exception.NestedExceptionUtils;
import io.harness.exception.ScmException;
import io.harness.exception.ScmResourceNotFoundException;
import io.harness.exception.ScmUnauthorizedException;
import io.harness.exception.WingsException;
import io.harness.gitsync.common.scmerrorhandling.handlers.ScmApiErrorHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@OwnedBy(PL)
public class GithubListBranchesScmApiErrorHandler implements ScmApiErrorHandler {
  public static final String LIST_BRANCH_WITH_INVALID_CREDS =
      "Couldn't list branches from Github as the credentials provided in the connector are invalid or have expired.";
  public static final String LIST_BRANCH_WHEN_REPO_NOT_EXIST =
      "Couldn't list branches as given Github repo does not exist or has been deleted.";

  @Override
  public void handleError(int statusCode, String errorMessage) throws WingsException {
    switch (statusCode) {
      case 401:
      case 403:
        throw NestedExceptionUtils.hintWithExplanationException(ScmErrorHints.INVALID_CREDENTIALS,
            LIST_BRANCH_WITH_INVALID_CREDS, new ScmUnauthorizedException(errorMessage));
      case 404:
        throw NestedExceptionUtils.hintWithExplanationException(ScmErrorHints.REPO_NOT_FOUND,
            LIST_BRANCH_WHEN_REPO_NOT_EXIST, new ScmResourceNotFoundException(errorMessage));
      default:
        log.error(String.format("Error while listing github branches: [%s: %s]", statusCode, errorMessage));
        throw new ScmException(UNEXPECTED);
    }
  }
}
