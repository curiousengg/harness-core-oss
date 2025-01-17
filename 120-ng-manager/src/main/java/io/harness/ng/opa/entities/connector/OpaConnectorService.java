/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.ng.opa.entities.connector;

import static io.harness.annotations.dev.HarnessTeam.PL;

import io.harness.annotations.dev.OwnedBy;
import io.harness.connector.ConnectorDTO;
import io.harness.pms.contracts.governance.GovernanceMetadata;

@OwnedBy(PL)
public interface OpaConnectorService {
  void evaluatePoliciesWithJson(String accountId, String expandedJson, String orgIdentifier, String projectIdentifier,
      String action, String identifier);
  GovernanceMetadata evaluatePoliciesWithEntity(String accountId, ConnectorDTO connectorDTO, String orgIdentifier,
      String projectIdentifier, String action, String identifier);
}
