// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: io/harness/event/payloads/ecs_messages.proto

package io.harness.event.payloads;

@javax.annotation.Generated(value = "protoc", comments = "annotations:EcsSyncEventOrBuilder.java.pb.meta")
public interface EcsSyncEventOrBuilder extends
    // @@protoc_insertion_point(interface_extends:io.harness.event.payloads.EcsSyncEvent)
    com.google.protobuf.MessageOrBuilder {
  /**
   * <code>string cluster_arn = 1;</code>
   */
  java.lang.String getClusterArn();
  /**
   * <code>string cluster_arn = 1;</code>
   */
  com.google.protobuf.ByteString getClusterArnBytes();

  /**
   * <code>repeated string active_ec2_instance_arns = 2;</code>
   */
  java.util.List<java.lang.String> getActiveEc2InstanceArnsList();
  /**
   * <code>repeated string active_ec2_instance_arns = 2;</code>
   */
  int getActiveEc2InstanceArnsCount();
  /**
   * <code>repeated string active_ec2_instance_arns = 2;</code>
   */
  java.lang.String getActiveEc2InstanceArns(int index);
  /**
   * <code>repeated string active_ec2_instance_arns = 2;</code>
   */
  com.google.protobuf.ByteString getActiveEc2InstanceArnsBytes(int index);

  /**
   * <code>repeated string active_container_instance_arns = 3;</code>
   */
  java.util.List<java.lang.String> getActiveContainerInstanceArnsList();
  /**
   * <code>repeated string active_container_instance_arns = 3;</code>
   */
  int getActiveContainerInstanceArnsCount();
  /**
   * <code>repeated string active_container_instance_arns = 3;</code>
   */
  java.lang.String getActiveContainerInstanceArns(int index);
  /**
   * <code>repeated string active_container_instance_arns = 3;</code>
   */
  com.google.protobuf.ByteString getActiveContainerInstanceArnsBytes(int index);

  /**
   * <code>repeated string active_task_arns = 4;</code>
   */
  java.util.List<java.lang.String> getActiveTaskArnsList();
  /**
   * <code>repeated string active_task_arns = 4;</code>
   */
  int getActiveTaskArnsCount();
  /**
   * <code>repeated string active_task_arns = 4;</code>
   */
  java.lang.String getActiveTaskArns(int index);
  /**
   * <code>repeated string active_task_arns = 4;</code>
   */
  com.google.protobuf.ByteString getActiveTaskArnsBytes(int index);

  /**
   * <code>.google.protobuf.Timestamp last_processed_timestamp = 5;</code>
   */
  boolean hasLastProcessedTimestamp();
  /**
   * <code>.google.protobuf.Timestamp last_processed_timestamp = 5;</code>
   */
  com.google.protobuf.Timestamp getLastProcessedTimestamp();
  /**
   * <code>.google.protobuf.Timestamp last_processed_timestamp = 5;</code>
   */
  com.google.protobuf.TimestampOrBuilder getLastProcessedTimestampOrBuilder();

  /**
   * <code>string cluster_id = 6;</code>
   */
  java.lang.String getClusterId();
  /**
   * <code>string cluster_id = 6;</code>
   */
  com.google.protobuf.ByteString getClusterIdBytes();

  /**
   * <code>string setting_id = 7;</code>
   */
  java.lang.String getSettingId();
  /**
   * <code>string setting_id = 7;</code>
   */
  com.google.protobuf.ByteString getSettingIdBytes();
}
