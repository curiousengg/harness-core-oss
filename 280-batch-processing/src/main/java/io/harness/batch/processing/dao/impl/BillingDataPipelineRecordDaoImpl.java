package io.harness.batch.processing.dao.impl;

import static io.harness.persistence.HPersistence.returnNewOptions;

import static java.util.Objects.isNull;

import io.harness.batch.processing.dao.intfc.BillingDataPipelineRecordDao;
import io.harness.ccm.billing.entities.BillingDataPipelineRecord;
import io.harness.ccm.billing.entities.BillingDataPipelineRecord.BillingDataPipelineRecordKeys;
import io.harness.persistence.HPersistence;

import com.google.inject.Inject;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class BillingDataPipelineRecordDaoImpl implements BillingDataPipelineRecordDao {
  @Autowired @Inject private HPersistence hPersistence;

  @Override
  public String create(BillingDataPipelineRecord billingDataPipelineRecord) {
    return hPersistence.save(billingDataPipelineRecord);
  }

  @Override
  public List<BillingDataPipelineRecord> listAllBillingDataPipelineRecords() {
    return hPersistence.createQuery(BillingDataPipelineRecord.class).asList();
  }

  @Override
  public BillingDataPipelineRecord getByMasterAccountId(String accountId, String awsMasterAccountId) {
    return hPersistence.createQuery(BillingDataPipelineRecord.class)
        .field(BillingDataPipelineRecordKeys.accountId)
        .equal(accountId)
        .filter(BillingDataPipelineRecordKeys.awsMasterAccountId, awsMasterAccountId)
        .get();
  }

  @Override
  public BillingDataPipelineRecord getByAccountId(String accountId) {
    return hPersistence.createQuery(BillingDataPipelineRecord.class)
        .filter(BillingDataPipelineRecordKeys.accountId, accountId)
        .get();
  }

  @Override
  public List<BillingDataPipelineRecord> getAllRecordsByAccountId(String accountId) {
    return hPersistence.createQuery(BillingDataPipelineRecord.class)
        .filter(BillingDataPipelineRecordKeys.accountId, accountId)
        .asList();
  }

  @Override
  public BillingDataPipelineRecord getBySettingId(String accountId, String settingId) {
    return hPersistence.createQuery(BillingDataPipelineRecord.class)
        .filter(BillingDataPipelineRecordKeys.accountId, accountId)
        .filter(BillingDataPipelineRecordKeys.settingId, settingId)
        .get();
  }

  @Override
  public List<BillingDataPipelineRecord> listByGcpBillingAccountDataset(
      String accountId, String gcpBqProjectId, String gcpBqDatasetId) {
    return hPersistence.createQuery(BillingDataPipelineRecord.class)
        .field(BillingDataPipelineRecordKeys.accountId)
        .equal(accountId)
        .field(BillingDataPipelineRecordKeys.gcpBqProjectId)
        .equal(gcpBqProjectId)
        .field(BillingDataPipelineRecordKeys.gcpBqDatasetId)
        .equal(gcpBqDatasetId)
        .asList();
  }

  @Override
  public BillingDataPipelineRecord upsert(BillingDataPipelineRecord billingDataPipelineRecord) {
    Query<BillingDataPipelineRecord> query =
        hPersistence.createQuery(BillingDataPipelineRecord.class)
            .filter(BillingDataPipelineRecordKeys.accountId, billingDataPipelineRecord.getAccountId())
            .filter(BillingDataPipelineRecordKeys.settingId, billingDataPipelineRecord.getSettingId());

    UpdateOperations<BillingDataPipelineRecord> updateOperations =
        hPersistence.createUpdateOperations(BillingDataPipelineRecord.class);

    if (!isNull(billingDataPipelineRecord.getDataTransferJobStatus())) {
      updateOperations.set(
          BillingDataPipelineRecordKeys.dataTransferJobStatus, billingDataPipelineRecord.getDataTransferJobStatus());
    }

    if (!isNull(billingDataPipelineRecord.getPreAggregatedScheduledQueryStatus())) {
      updateOperations.set(BillingDataPipelineRecordKeys.preAggregatedScheduledQueryStatus,
          billingDataPipelineRecord.getPreAggregatedScheduledQueryStatus());
    }

    if (!isNull(billingDataPipelineRecord.getAwsFallbackTableScheduledQueryStatus())) {
      updateOperations.set(BillingDataPipelineRecordKeys.awsFallbackTableScheduledQueryStatus,
          billingDataPipelineRecord.getAwsFallbackTableScheduledQueryStatus());
    }

    Instant lastSuccessfulS3Sync = billingDataPipelineRecord.getLastSuccessfulS3Sync();
    if (!isNull(lastSuccessfulS3Sync) && lastSuccessfulS3Sync.isAfter(Instant.EPOCH)) {
      updateOperations.set(
          BillingDataPipelineRecordKeys.lastSuccessfulS3Sync, billingDataPipelineRecord.getLastSuccessfulS3Sync());
    }

    Instant lastSuccessfulStorageSync = billingDataPipelineRecord.getLastSuccessfulStorageSync();
    if (!isNull(lastSuccessfulStorageSync) && lastSuccessfulStorageSync.isAfter(Instant.EPOCH)) {
      updateOperations.set(BillingDataPipelineRecordKeys.lastSuccessfulStorageSync,
          billingDataPipelineRecord.getLastSuccessfulStorageSync());
    }
    log.info("query.toString(): {} updateOperations.toString(): {}", query.toString(), updateOperations.toString());
    return hPersistence.findAndModify(query, updateOperations, returnNewOptions);
  }

  @Override
  public boolean removeBillingDataPipelineRecord(String accountId, String settingId) {
    Query<BillingDataPipelineRecord> query = hPersistence.createQuery(BillingDataPipelineRecord.class)
                                                 .filter(BillingDataPipelineRecordKeys.accountId, accountId)
                                                 .field(BillingDataPipelineRecordKeys.uuid)
                                                 .equal(settingId);

    return hPersistence.delete(query);
  }
}
