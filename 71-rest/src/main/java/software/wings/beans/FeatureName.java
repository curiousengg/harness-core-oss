package software.wings.beans;

import lombok.Getter;
import software.wings.beans.FeatureFlag.Scope;

/**
 * Add your feature name here. When the feature is fully launched and no longer needs to be flagged,
 * delete the feature name.
 */
public enum FeatureName {
  CV_DEMO,
  LOGML_NEURAL_NET,
  GIT_BATCH_SYNC,
  GLOBAL_CV_DASH,
  CV_SUCCEED_FOR_ANOMALY,
  COPY_ARTIFACT,
  INLINE_SSH_COMMAND,
  CUSTOM_WORKFLOW,
  ECS_DELEGATE,
  USE_QUARTZ_JOBS,
  CV_DATA_COLLECTION_JOB,
  THREE_PHASE_SECRET_DECRYPTION,
  DELEGATE_CAPABILITY_FRAMEWORK,
  GRAPHQL,
  SHELL_SCRIPT_ENV,
  REMOVE_STENCILS,
  DISABLE_METRIC_NAME_CURLY_BRACE_CHECK,
  GLOBAL_DISABLE_HEALTH_CHECK(Scope.GLOBAL),
  GIT_HTTPS_KERBEROS,
  TRIGGER_FOR_ALL_ARTIFACTS,
  USE_PCF_CLI,
  AUDIT_TRAIL_UI,
  ARTIFACT_STREAM_REFACTOR,
  TRIGGER_REFACTOR,
  TRIGGER_YAML,
  CV_FEEDBACKS,
  CV_HOST_SAMPLING,
  CUSTOM_DASHBOARD,
  SEND_LOG_ANALYSIS_COMPRESSED,
  SSH_SHORT_VALIDATION_TIMEOUT,
  PERPETUAL_TASK_SERVICE(Scope.GLOBAL),
  CCM_EVENT_COLLECTION,
  INFRA_MAPPING_REFACTOR,
  GRAPHQL_DEV,
  SUPERVISED_TS_THRESHOLD,
  REJECT_TRIGGER_IF_ARTIFACTS_NOT_MATCH,
  NEW_INSTANCE_TIMESERIES,
  SPOTINST,
  ENTITY_AUDIT_RECORD,
  TIME_RANGE_FREEZE_GOVERNANCE,
  SCIM_INTEGRATION,
  NEW_RELIC_CV_TASK,
  ELK_CV_TASK,
  ELK_24_7_CV_TASK,
  SLACK_APPROVALS,
  NEWRELIC_24_7_CV_TASK,
  SEARCH(Scope.GLOBAL),
  SERVERLESS_DASHBOARD_AWS_LAMBDA,
  BATCH_SECRET_DECRYPTION,
  PIPELINE_GOVERNANCE,
  ADD_COMMAND,
  STACKDRIVER_SERVICEGUARD,
  SEARCH_REQUEST,
  ON_DEMAND_ROLLBACK,
  WORKFLOW_VERIFICATION_REMOVE_CRON,
  NODE_AGGREGATION,
  BIND_FETCH_FILES_TASK_TO_DELEGATE,
  DEFAULT_ARTIFACT,
  DEPLOY_TO_SPECIFIC_HOSTS,
  CUSTOM_LOGS_SERVICEGUARD,
  UI_ALLOW_K8S_V1,
  SEND_SLACK_NOTIFICATION_FROM_DELEGATE,
  TIME_SERIES_SERVICEGUARD_V2,
  TF_USE_VAR_CL,
  GOOGLE_KMS,
  LOGS_V2_247,
  WEEKLY_WINDOW,
  SSH_WINRM_SO,
  APM_CUSTOM_THRESHOLDS,
  SALESFORCE_INTEGRATION,
  REMOVE_WORKFLOW_VERIFICATION_CLUSTERING_CRON,
  SWITCH_GLOBAL_TO_GCP_KMS,
  FAIL_FAST_THRESHOLDS_WORKFLOW,
  FAIL_FAST_THRESHOLDS_SERVICEGUARD,
  SIDE_NAVIGATION,
  WORKFLOW_TS_RECORDS_NEW,
  TEMPLATE_YAML_SUPPORT,
  ADD_WORKFLOW_FORMIK,
  CV_INSTANA;
  FeatureName() {
    scope = Scope.PER_ACCOUNT;
  }

  FeatureName(Scope scope) {
    this.scope = scope;
  }

  @Getter private FeatureFlag.Scope scope;
}
