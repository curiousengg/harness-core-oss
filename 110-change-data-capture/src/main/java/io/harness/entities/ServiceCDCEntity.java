package io.harness.entities;

import static io.harness.annotations.dev.HarnessTeam.PIPELINE;

import io.harness.ChangeHandler;
import io.harness.annotations.dev.OwnedBy;
import io.harness.changehandlers.ServicesChangeDataHandler;
import io.harness.ng.core.service.entity.ServiceEntity;
import io.harness.persistence.PersistentEntity;

import com.google.inject.Inject;

@OwnedBy(PIPELINE)
public class ServiceCDCEntity implements CDCEntity<ServiceEntity> {
  @Inject private ServicesChangeDataHandler servicesChangeDataHandler;

  @Override
  public ChangeHandler getChangeHandler(String handlerClass) {
    return servicesChangeDataHandler;
  }

  @Override
  public Class<? extends PersistentEntity> getSubscriptionEntity() {
    return ServiceEntity.class;
  }
}
