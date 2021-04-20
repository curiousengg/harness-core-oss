package io.harness.changehandlers;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.changestreamsframework.ChangeEvent;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@OwnedBy(HarnessTeam.CI)
@Slf4j
public class PlanExecutionSummaryChangeDataHandler extends AbstractChangeDataHandler {
  @Override
  public Map<String, String> getColumnValueMapping(ChangeEvent<?> changeEvent, String[] fields) {
    if (changeEvent == null) {
      return null;
    }
    Map<String, String> columnValueMapping = new HashMap<>();
    DBObject dbObject = changeEvent.getFullDocument();

    columnValueMapping.put("id", changeEvent.getUuid());

    if (dbObject == null) {
      return columnValueMapping;
    }

    if (dbObject.get("accountId") != null) {
      columnValueMapping.put("accountId", dbObject.get("accountId").toString());
    }
    if (dbObject.get("orgIdentifier") != null) {
      columnValueMapping.put("orgIdentifier", dbObject.get("orgIdentifier").toString());
    }
    if (dbObject.get("projectIdentifier") != null) {
      columnValueMapping.put("projectIdentifier", dbObject.get("projectIdentifier").toString());
    }
    if (dbObject.get("pipelineIdentifier") != null) {
      columnValueMapping.put("pipelineIdentifier", dbObject.get("pipelineIdentifier").toString());
    }
    if (dbObject.get("name") != null) {
      columnValueMapping.put("name", dbObject.get("name").toString());
    }
    if (dbObject.get("status") != null) {
      columnValueMapping.put("status", dbObject.get("status").toString());
    }

    // if moduleInfo is not null
    if (dbObject.get("moduleInfo") != null) {
      if (((BasicDBObject) dbObject.get("moduleInfo")).get("ci") != null) {
        columnValueMapping.put("moduleInfo_type", "CI");
        DBObject ciExecutionInfo = (DBObject) ((BasicDBObject) ((BasicDBObject) dbObject.get("moduleInfo")).get("ci"))
                                       .get("ciExecutionInfoDTO");
        if (ciExecutionInfo != null) {
          DBObject branch = (DBObject) (ciExecutionInfo.get("branch"));

          HashMap firstCommit = null;
          if (branch != null) {
            firstCommit = (HashMap) ((List) branch.get("commits")).get(0);
            if (firstCommit != null) {
              columnValueMapping.put("moduleInfo_branch_commit_id", firstCommit.get("id").toString());
              columnValueMapping.put("moduleInfo_branch_commit_message", firstCommit.get("message").toString());
            }
            columnValueMapping.put("moduleInfo_branch_name", branch.get("name").toString());
          }
          DBObject author = (DBObject) (ciExecutionInfo.get("author"));
          if (ciExecutionInfo.get("event") != null) {
            columnValueMapping.put("moduleInfo_event", ciExecutionInfo.get("event").toString());
          }
          if (author != null) {
            columnValueMapping.put("moduleInfo_author_id", author.get("id").toString());
          }
        }
      }
    }
    columnValueMapping.put(
        "startTs", String.valueOf(new Timestamp(Long.parseLong(dbObject.get("startTs").toString()))));
    if (dbObject.get("endTs") != null) {
      columnValueMapping.put("endTs", String.valueOf(new Timestamp(Long.parseLong(dbObject.get("endTs").toString()))));
    }
    return columnValueMapping;
  }
}
