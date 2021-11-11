package io.harness.cvng.beans.datadog;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.harness.annotations.dev.OwnedBy;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

import java.util.Map;

import static io.harness.annotations.dev.HarnessTeam.CV;

@JsonTypeName("DATADOG_LOG_INDEXES")
@Data
@SuperBuilder
@NoArgsConstructor
@OwnedBy(CV)
@FieldNameConstants(innerTypeName = "DatadogLogIndexesRequestKeys")
@EqualsAndHashCode(callSuper = true)
public class DatadogLogIndexesRequest extends DatadogRequest {

    public static final String DSL = DatadogLogSampleDataRequest.readDSL(
            "datadog-log-indexes.datacollection", DatadogLogIndexesRequest.class);
    @Override
    public String getDSL() {
        return DSL;
    }

    @Override
    public Map<String, Object> fetchDslEnvVariables() {
        Map<String, Object> commonEnvVariables = super.fetchDslEnvVariables();
        return commonEnvVariables;
    }
}
