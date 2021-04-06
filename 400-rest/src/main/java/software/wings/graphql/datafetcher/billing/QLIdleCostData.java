package software.wings.graphql.datafetcher.billing;

import static io.harness.annotations.dev.HarnessTeam.CE;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.OwnedBy;
import io.harness.annotations.dev.TargetModule;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@TargetModule(HarnessModule._375_CE_GRAPHQL)
@OwnedBy(CE)
public class QLIdleCostData {
  private BigDecimal totalCost;
  private BigDecimal totalCpuCost;
  private BigDecimal totalMemoryCost;
  private BigDecimal totalStorageCost;
  private BigDecimal idleCost;
  private BigDecimal cpuIdleCost;
  private BigDecimal memoryIdleCost;
  private BigDecimal storageIdleCost;
  private BigDecimal avgCpuUtilization;
  private BigDecimal avgMemoryUtilization;
  private long minStartTime;
  private long maxStartTime;
}
