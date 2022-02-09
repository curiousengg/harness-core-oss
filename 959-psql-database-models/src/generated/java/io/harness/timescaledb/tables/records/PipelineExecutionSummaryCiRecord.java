/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

/*
 * This file is generated by jOOQ.
 */
package io.harness.timescaledb.tables.records;

import io.harness.timescaledb.tables.PipelineExecutionSummaryCi;

import org.jooq.Record2;
import org.jooq.impl.UpdatableRecordImpl;

/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class PipelineExecutionSummaryCiRecord extends UpdatableRecordImpl<PipelineExecutionSummaryCiRecord> {
  private static final long serialVersionUID = 1L;

  /**
   * Setter for <code>public.pipeline_execution_summary_ci.id</code>.
   */
  public PipelineExecutionSummaryCiRecord setId(String value) {
    set(0, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_ci.id</code>.
   */
  public String getId() {
    return (String) get(0);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_ci.accountid</code>.
   */
  public PipelineExecutionSummaryCiRecord setAccountid(String value) {
    set(1, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_ci.accountid</code>.
   */
  public String getAccountid() {
    return (String) get(1);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_ci.orgidentifier</code>.
   */
  public PipelineExecutionSummaryCiRecord setOrgidentifier(String value) {
    set(2, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_ci.orgidentifier</code>.
   */
  public String getOrgidentifier() {
    return (String) get(2);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_ci.projectidentifier</code>.
   */
  public PipelineExecutionSummaryCiRecord setProjectidentifier(String value) {
    set(3, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_ci.projectidentifier</code>.
   */
  public String getProjectidentifier() {
    return (String) get(3);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_ci.pipelineidentifier</code>.
   */
  public PipelineExecutionSummaryCiRecord setPipelineidentifier(String value) {
    set(4, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_ci.pipelineidentifier</code>.
   */
  public String getPipelineidentifier() {
    return (String) get(4);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_ci.name</code>.
   */
  public PipelineExecutionSummaryCiRecord setName(String value) {
    set(5, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_ci.name</code>.
   */
  public String getName() {
    return (String) get(5);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_ci.status</code>.
   */
  public PipelineExecutionSummaryCiRecord setStatus(String value) {
    set(6, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_ci.status</code>.
   */
  public String getStatus() {
    return (String) get(6);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_ci.moduleinfo_type</code>.
   */
  public PipelineExecutionSummaryCiRecord setModuleinfoType(String value) {
    set(7, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_ci.moduleinfo_type</code>.
   */
  public String getModuleinfoType() {
    return (String) get(7);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_ci.moduleinfo_event</code>.
   */
  public PipelineExecutionSummaryCiRecord setModuleinfoEvent(String value) {
    set(8, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_ci.moduleinfo_event</code>.
   */
  public String getModuleinfoEvent() {
    return (String) get(8);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_ci.moduleinfo_author_id</code>.
   */
  public PipelineExecutionSummaryCiRecord setModuleinfoAuthorId(String value) {
    set(9, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_ci.moduleinfo_author_id</code>.
   */
  public String getModuleinfoAuthorId() {
    return (String) get(9);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_ci.moduleinfo_repository</code>.
   */
  public PipelineExecutionSummaryCiRecord setModuleinfoRepository(String value) {
    set(10, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_ci.moduleinfo_repository</code>.
   */
  public String getModuleinfoRepository() {
    return (String) get(10);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_ci.moduleinfo_branch_name</code>.
   */
  public PipelineExecutionSummaryCiRecord setModuleinfoBranchName(String value) {
    set(11, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_ci.moduleinfo_branch_name</code>.
   */
  public String getModuleinfoBranchName() {
    return (String) get(11);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_ci.moduleinfo_branch_commit_id</code>.
   */
  public PipelineExecutionSummaryCiRecord setModuleinfoBranchCommitId(String value) {
    set(12, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_ci.moduleinfo_branch_commit_id</code>.
   */
  public String getModuleinfoBranchCommitId() {
    return (String) get(12);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_ci.moduleinfo_branch_commit_message</code>.
   */
  public PipelineExecutionSummaryCiRecord setModuleinfoBranchCommitMessage(String value) {
    set(13, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_ci.moduleinfo_branch_commit_message</code>.
   */
  public String getModuleinfoBranchCommitMessage() {
    return (String) get(13);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_ci.author_name</code>.
   */
  public PipelineExecutionSummaryCiRecord setAuthorName(String value) {
    set(14, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_ci.author_name</code>.
   */
  public String getAuthorName() {
    return (String) get(14);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_ci.author_avatar</code>.
   */
  public PipelineExecutionSummaryCiRecord setAuthorAvatar(String value) {
    set(15, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_ci.author_avatar</code>.
   */
  public String getAuthorAvatar() {
    return (String) get(15);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_ci.startts</code>.
   */
  public PipelineExecutionSummaryCiRecord setStartts(Long value) {
    set(16, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_ci.startts</code>.
   */
  public Long getStartts() {
    return (Long) get(16);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_ci.endts</code>.
   */
  public PipelineExecutionSummaryCiRecord setEndts(Long value) {
    set(17, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_ci.endts</code>.
   */
  public Long getEndts() {
    return (Long) get(17);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_ci.planexecutionid</code>.
   */
  public PipelineExecutionSummaryCiRecord setPlanexecutionid(String value) {
    set(18, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_ci.planexecutionid</code>.
   */
  public String getPlanexecutionid() {
    return (String) get(18);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_ci.errormessage</code>.
   */
  public PipelineExecutionSummaryCiRecord setErrormessage(String value) {
    set(19, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_ci.errormessage</code>.
   */
  public String getErrormessage() {
    return (String) get(19);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_ci.trigger_type</code>.
   */
  public PipelineExecutionSummaryCiRecord setTriggerType(String value) {
    set(20, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_ci.trigger_type</code>.
   */
  public String getTriggerType() {
    return (String) get(20);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_ci.source_branch</code>.
   */
  public PipelineExecutionSummaryCiRecord setSourceBranch(String value) {
    set(21, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_ci.source_branch</code>.
   */
  public String getSourceBranch() {
    return (String) get(21);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_ci.pr</code>. The Pull Request number
   */
  public PipelineExecutionSummaryCiRecord setPr(Integer value) {
    set(22, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_ci.pr</code>. The Pull Request number
   */
  public Integer getPr() {
    return (Integer) get(22);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_ci.moduleinfo_is_private</code>. Is the cloned repo private
   */
  public PipelineExecutionSummaryCiRecord setModuleinfoIsPrivate(Boolean value) {
    set(23, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_ci.moduleinfo_is_private</code>. Is the cloned repo private
   */
  public Boolean getModuleinfoIsPrivate() {
    return (Boolean) get(23);
  }

  // -------------------------------------------------------------------------
  // Primary key information
  // -------------------------------------------------------------------------

  @Override
  public Record2<String, Long> key() {
    return (Record2) super.key();
  }

  // -------------------------------------------------------------------------
  // Constructors
  // -------------------------------------------------------------------------

  /**
   * Create a detached PipelineExecutionSummaryCiRecord
   */
  public PipelineExecutionSummaryCiRecord() {
    super(PipelineExecutionSummaryCi.PIPELINE_EXECUTION_SUMMARY_CI);
  }

  /**
   * Create a detached, initialised PipelineExecutionSummaryCiRecord
   */
  public PipelineExecutionSummaryCiRecord(String id, String accountid, String orgidentifier, String projectidentifier,
      String pipelineidentifier, String name, String status, String moduleinfoType, String moduleinfoEvent,
      String moduleinfoAuthorId, String moduleinfoRepository, String moduleinfoBranchName,
      String moduleinfoBranchCommitId, String moduleinfoBranchCommitMessage, String authorName, String authorAvatar,
      Long startts, Long endts, String planexecutionid, String errormessage, String triggerType, String sourceBranch,
      Integer pr, Boolean moduleinfoIsPrivate) {
    super(PipelineExecutionSummaryCi.PIPELINE_EXECUTION_SUMMARY_CI);

    setId(id);
    setAccountid(accountid);
    setOrgidentifier(orgidentifier);
    setProjectidentifier(projectidentifier);
    setPipelineidentifier(pipelineidentifier);
    setName(name);
    setStatus(status);
    setModuleinfoType(moduleinfoType);
    setModuleinfoEvent(moduleinfoEvent);
    setModuleinfoAuthorId(moduleinfoAuthorId);
    setModuleinfoRepository(moduleinfoRepository);
    setModuleinfoBranchName(moduleinfoBranchName);
    setModuleinfoBranchCommitId(moduleinfoBranchCommitId);
    setModuleinfoBranchCommitMessage(moduleinfoBranchCommitMessage);
    setAuthorName(authorName);
    setAuthorAvatar(authorAvatar);
    setStartts(startts);
    setEndts(endts);
    setPlanexecutionid(planexecutionid);
    setErrormessage(errormessage);
    setTriggerType(triggerType);
    setSourceBranch(sourceBranch);
    setPr(pr);
    setModuleinfoIsPrivate(moduleinfoIsPrivate);
  }
}
