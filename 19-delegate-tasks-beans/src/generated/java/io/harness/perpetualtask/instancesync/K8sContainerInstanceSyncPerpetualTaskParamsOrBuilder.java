// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: io/harness/perpetualtask/instancesync/container_instance_sync_perpetual_task_params.proto

package io.harness.perpetualtask.instancesync;

@javax.annotation.
Generated(value = "protoc", comments = "annotations:K8sContainerInstanceSyncPerpetualTaskParamsOrBuilder.java.pb.meta")
public interface K8sContainerInstanceSyncPerpetualTaskParamsOrBuilder extends
    // @@protoc_insertion_point(interface_extends:io.harness.perpetualtask.instancesync.K8sContainerInstanceSyncPerpetualTaskParams)
    com.google.protobuf.MessageOrBuilder {
  /**
   * <code>string account_id = 1;</code>
   */
  java.lang.String getAccountId();
  /**
   * <code>string account_id = 1;</code>
   */
  com.google.protobuf.ByteString getAccountIdBytes();

  /**
   * <code>string app_id = 2;</code>
   */
  java.lang.String getAppId();
  /**
   * <code>string app_id = 2;</code>
   */
  com.google.protobuf.ByteString getAppIdBytes();

  /**
   * <code>bytes k8s_cluster_config = 3;</code>
   */
  com.google.protobuf.ByteString getK8SClusterConfig();

  /**
   * <code>string namespace = 4;</code>
   */
  java.lang.String getNamespace();
  /**
   * <code>string namespace = 4;</code>
   */
  com.google.protobuf.ByteString getNamespaceBytes();

  /**
   * <code>string release_name = 5;</code>
   */
  java.lang.String getReleaseName();
  /**
   * <code>string release_name = 5;</code>
   */
  com.google.protobuf.ByteString getReleaseNameBytes();
}
