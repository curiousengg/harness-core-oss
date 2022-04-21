/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.ngmigration.service.ngManifestFactory;

import software.wings.beans.appmanifest.AppManifestKind;
import software.wings.beans.appmanifest.ApplicationManifest;
import software.wings.beans.appmanifest.StoreType;

public class NgManifestFactory {
  public NgManifestService getNgManifestService(ApplicationManifest applicationManifest) {
    if (applicationManifest.getKind() == null) {
      throw new UnsupportedOperationException("Empty appManifest kind is not supported for migration");
    }
    AppManifestKind appManifestKind = applicationManifest.getKind();
    StoreType storeType = applicationManifest.getStoreType();

    switch (appManifestKind) {
      case K8S_MANIFEST:
        switch (storeType) {
          case Remote:
            return new K8sManifestRemoteStoreService();
          default:
            throw new UnsupportedOperationException(String.format(
                "%s storetype is currently not supported for %s appManifestKind", storeType, appManifestKind));
        }
      case VALUES:
        switch (storeType) {
          case Remote:
            return new ValuesManifestRemoteStoreService();
          default:
            throw new UnsupportedOperationException(String.format(
                "%s storetype is currently not supported for %s appManifestKind", storeType, appManifestKind));
        }
      default:
        throw new UnsupportedOperationException(
            String.format("%s appManifestKind is currently not supported", appManifestKind));
    }
  }
}
