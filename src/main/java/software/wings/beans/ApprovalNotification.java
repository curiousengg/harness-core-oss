package software.wings.beans;

import static java.util.Arrays.asList;
import static software.wings.beans.ApprovalNotification.ApprovalStage.APPROVED;
import static software.wings.beans.ApprovalNotification.ApprovalStage.PENDING;
import static software.wings.beans.ApprovalNotification.ApprovalStage.REJECTED;
import static software.wings.beans.Notification.NotificationType.APPROVAL;
import static software.wings.beans.NotificationAction.Builder.aNotificationAction;
import static software.wings.beans.NotificationAction.NotificationActionType.APPROVE;
import static software.wings.beans.NotificationAction.NotificationActionType.REJECT;

import com.google.common.collect.ImmutableMap;

import org.hibernate.validator.constraints.NotEmpty;
import org.mongodb.morphia.annotations.Transient;
import software.wings.beans.Artifact.Status;
import software.wings.beans.NotificationAction.NotificationActionType;
import software.wings.dl.WingsPersistence;
import software.wings.service.intfc.ArtifactService;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

/**
 * Created by anubhaw on 7/25/16.
 */
public class ApprovalNotification extends ActionableNotification {
  @NotEmpty private String entityName;
  @NotNull private ApprovalStage stage = PENDING;
  private String releaseId;
  @Inject @Transient private transient WingsPersistence wingsPersistence;
  @Inject @Transient private transient ArtifactService artifactService;

  @Override
  public boolean performAction(NotificationActionType actionType) {
    if (EntityType.ARTIFACT.equals(getEntityType())) {
      artifactService.updateStatus(
          getEntityId(), getAppId(), actionType.equals(APPROVE) ? Status.APPROVED : Status.REJECTED);
    }
    wingsPersistence.updateFields(ApprovalNotification.class, getUuid(),
        ImmutableMap.of("stage", actionType.equals(APPROVE) ? APPROVED : REJECTED));
    return true;
  }

  /**
   * The enum Approval stage.
   */
  public enum ApprovalStage {
    /**
     * Pending approval stage.
     */
    PENDING, /**
              * Accepted approval stage.
              */
    APPROVED, /**
               * Rejected approval stage.
               */
    REJECTED
  }

  /**
   * Instantiates a new Approval notification.
   */
  public ApprovalNotification() {
    super(APPROVAL,
        asList(aNotificationAction().withName("Approve").withType(APPROVE).withPrimary(true).build(),
            aNotificationAction().withName("Reject").withType(REJECT).withPrimary(false).build()));
  }

  /**
   * Gets entity name.
   *
   * @return the entity name
   */
  public String getEntityName() {
    return entityName;
  }

  /**
   * Sets entity name.
   *
   * @param entityName the entity name
   */
  public void setEntityName(String entityName) {
    this.entityName = entityName;
  }

  /**
   * Gets stage.
   *
   * @return the stage
   */
  public ApprovalStage getStage() {
    return stage;
  }

  /**
   * Sets stage.
   *
   * @param stage the stage
   */
  public void setStage(ApprovalStage stage) {
    this.stage = stage;
  }

  /**
   * Gets release id.
   *
   * @return the release id
   */
  public String getReleaseId() {
    return releaseId;
  }

  /**
   * Sets release id.
   *
   * @param releaseId the release id
   */
  public void setReleaseId(String releaseId) {
    this.releaseId = releaseId;
  }

  /**
   * The type Builder.
   */
  public static final class Builder {
    private String entityName;
    private ApprovalStage stage = PENDING;
    private String releaseId;
    private String environmentId;
    private String entityId;
    private EntityType entityType;
    private boolean complete = true;
    private String uuid;
    private String appId;
    private User createdBy;
    private long createdAt;
    private User lastUpdatedBy;
    private long lastUpdatedAt;
    private boolean active = true;

    private Builder() {}

    /**
     * An approval notification builder.
     *
     * @return the builder
     */
    public static Builder anApprovalNotification() {
      return new Builder();
    }

    /**
     * With entity name builder.
     *
     * @param entityName the entity name
     * @return the builder
     */
    public Builder withEntityName(String entityName) {
      this.entityName = entityName;
      return this;
    }

    /**
     * With stage builder.
     *
     * @param stage the stage
     * @return the builder
     */
    public Builder withStage(ApprovalStage stage) {
      this.stage = stage;
      return this;
    }

    /**
     * With release id builder.
     *
     * @param releaseId the release id
     * @return the builder
     */
    public Builder withReleaseId(String releaseId) {
      this.releaseId = releaseId;
      return this;
    }

    /**
     * With environment id builder.
     *
     * @param environmentId the environment id
     * @return the builder
     */
    public Builder withEnvironmentId(String environmentId) {
      this.environmentId = environmentId;
      return this;
    }

    /**
     * With entity id builder.
     *
     * @param entityId the entity id
     * @return the builder
     */
    public Builder withEntityId(String entityId) {
      this.entityId = entityId;
      return this;
    }

    /**
     * With entity type builder.
     *
     * @param entityType the entity type
     * @return the builder
     */
    public Builder withEntityType(EntityType entityType) {
      this.entityType = entityType;
      return this;
    }

    /**
     * With complete builder.
     *
     * @param complete the complete
     * @return the builder
     */
    public Builder withComplete(boolean complete) {
      this.complete = complete;
      return this;
    }

    /**
     * With uuid builder.
     *
     * @param uuid the uuid
     * @return the builder
     */
    public Builder withUuid(String uuid) {
      this.uuid = uuid;
      return this;
    }

    /**
     * With app id builder.
     *
     * @param appId the app id
     * @return the builder
     */
    public Builder withAppId(String appId) {
      this.appId = appId;
      return this;
    }

    /**
     * With created by builder.
     *
     * @param createdBy the created by
     * @return the builder
     */
    public Builder withCreatedBy(User createdBy) {
      this.createdBy = createdBy;
      return this;
    }

    /**
     * With created at builder.
     *
     * @param createdAt the created at
     * @return the builder
     */
    public Builder withCreatedAt(long createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    /**
     * With last updated by builder.
     *
     * @param lastUpdatedBy the last updated by
     * @return the builder
     */
    public Builder withLastUpdatedBy(User lastUpdatedBy) {
      this.lastUpdatedBy = lastUpdatedBy;
      return this;
    }

    /**
     * With last updated at builder.
     *
     * @param lastUpdatedAt the last updated at
     * @return the builder
     */
    public Builder withLastUpdatedAt(long lastUpdatedAt) {
      this.lastUpdatedAt = lastUpdatedAt;
      return this;
    }

    /**
     * With active builder.
     *
     * @param active the active
     * @return the builder
     */
    public Builder withActive(boolean active) {
      this.active = active;
      return this;
    }

    /**
     * But builder.
     *
     * @return the builder
     */
    public Builder but() {
      return anApprovalNotification()
          .withEntityName(entityName)
          .withStage(stage)
          .withReleaseId(releaseId)
          .withEnvironmentId(environmentId)
          .withEntityId(entityId)
          .withEntityType(entityType)
          .withComplete(complete)
          .withUuid(uuid)
          .withAppId(appId)
          .withCreatedBy(createdBy)
          .withCreatedAt(createdAt)
          .withLastUpdatedBy(lastUpdatedBy)
          .withLastUpdatedAt(lastUpdatedAt)
          .withActive(active);
    }

    /**
     * Build approval notification.
     *
     * @return the approval notification
     */
    public ApprovalNotification build() {
      ApprovalNotification approvalNotification = new ApprovalNotification();
      approvalNotification.setEntityName(entityName);
      approvalNotification.setStage(stage);
      approvalNotification.setReleaseId(releaseId);
      approvalNotification.setEnvironmentId(environmentId);
      approvalNotification.setEntityId(entityId);
      approvalNotification.setEntityType(entityType);
      approvalNotification.setComplete(complete);
      approvalNotification.setUuid(uuid);
      approvalNotification.setAppId(appId);
      approvalNotification.setCreatedBy(createdBy);
      approvalNotification.setCreatedAt(createdAt);
      approvalNotification.setLastUpdatedBy(lastUpdatedBy);
      approvalNotification.setLastUpdatedAt(lastUpdatedAt);
      approvalNotification.setActive(active);
      return approvalNotification;
    }
  }
}
