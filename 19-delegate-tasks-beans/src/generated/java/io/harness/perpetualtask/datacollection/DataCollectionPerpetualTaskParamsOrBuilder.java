// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: io/harness/perpetualtask/datacollection/data_collection_task.proto

package io.harness.perpetualtask.datacollection;

@javax.annotation.
Generated(value = "protoc", comments = "annotations:DataCollectionPerpetualTaskParamsOrBuilder.java.pb.meta")
public interface DataCollectionPerpetualTaskParamsOrBuilder extends
    // @@protoc_insertion_point(interface_extends:io.harness.perpetualtask.datacollection.DataCollectionPerpetualTaskParams)
    com.google.protobuf.MessageOrBuilder {
  /**
   * <code>string account_id = 1[json_name = "accountId"];</code>
   * @return The accountId.
   */
  java.lang.String getAccountId();
  /**
   * <code>string account_id = 1[json_name = "accountId"];</code>
   * @return The bytes for accountId.
   */
  com.google.protobuf.ByteString getAccountIdBytes();

  /**
   * <code>string cv_config_id = 2[json_name = "cvConfigId"];</code>
   * @return The cvConfigId.
   */
  java.lang.String getCvConfigId();
  /**
   * <code>string cv_config_id = 2[json_name = "cvConfigId"];</code>
   * @return The bytes for cvConfigId.
   */
  com.google.protobuf.ByteString getCvConfigIdBytes();

  /**
   * <code>bytes data_collection_info = 3[json_name = "dataCollectionInfo"];</code>
   * @return The dataCollectionInfo.
   */
  com.google.protobuf.ByteString getDataCollectionInfo();
}
