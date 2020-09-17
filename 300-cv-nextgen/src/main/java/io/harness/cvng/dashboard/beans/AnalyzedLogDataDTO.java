package io.harness.cvng.dashboard.beans;

import io.harness.cvng.analysis.entities.LogAnalysisResult.LogAnalysisTag;
import lombok.Builder;
import lombok.Data;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Value
@Builder
public class AnalyzedLogDataDTO implements Comparable<AnalyzedLogDataDTO> {
  String projectIdentifier;
  String orgIdentifier;
  String environmentIdentifier;
  String serviceIdentifier;

  LogData logData;

  @Data
  @Builder
  public static class LogData {
    String text;
    Long label;
    int count;
    List<Frequency> trend;
    LogAnalysisTag tag;
  }

  @Data
  @Builder
  public static class Frequency {
    private long timestamp;
    private int count;
  }

  @Override
  public int compareTo(@NotNull AnalyzedLogDataDTO o) {
    return logData.getLabel().compareTo(o.getLogData().getLabel());
  }
}
