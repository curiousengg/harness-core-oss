package software.wings.search.entities.related.audit;

import static io.harness.annotations.dev.HarnessTeam.PL;

import io.harness.annotations.dev.OwnedBy;

import software.wings.audit.AuditHeader;
import software.wings.audit.EntityAuditRecord;
import software.wings.beans.EntityType;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Singleton;
import java.util.Map;

@OwnedBy(PL)
@Singleton
public class RelatedAuditViewBuilder {
  public RelatedAuditView getAuditRelatedEntityView(AuditHeader auditHeader) {
    String createdBy = null;
    if (auditHeader.getCreatedBy() != null) {
      createdBy = auditHeader.getCreatedBy().getName();
    }
    return new RelatedAuditView(auditHeader.getUuid(), createdBy, auditHeader.getCreatedAt(), auditHeader.getAppId(),
        auditHeader.getAppId(), null, EntityType.APPLICATION.name());
  }

  public RelatedAuditView getAuditRelatedEntityView(AuditHeader auditHeader, EntityAuditRecord entityAuditRecord) {
    String createdBy = null;
    if (auditHeader.getCreatedBy() != null) {
      createdBy = auditHeader.getCreatedBy().getName();
    }
    return new RelatedAuditView(auditHeader.getUuid(), createdBy, auditHeader.getCreatedAt(),
        entityAuditRecord.getAppId(), entityAuditRecord.getAffectedResourceId(), entityAuditRecord.getEntityName(),
        entityAuditRecord.getEntityType());
  }

  public Map<String, Object> getAuditRelatedEntityViewMap(
      AuditHeader auditHeader, EntityAuditRecord entityAuditRecord) {
    ObjectMapper mapper = new ObjectMapper();
    RelatedAuditView relatedAuditView = getAuditRelatedEntityView(auditHeader, entityAuditRecord);
    return mapper.convertValue(relatedAuditView, new TypeReference<Object>() {});
  }
}
