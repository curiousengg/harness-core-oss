package io.harness.delegate.beans.connector.ceazure;

import io.harness.beans.DecryptableEntity;
import io.harness.delegate.beans.connector.CEFeatures;
import io.harness.delegate.beans.connector.ConnectorConfigDTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.NotEmpty;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel("CEAzureConnector")
@Schema(name = "CEAzureConnector", description = "This contains the cost explorer of Azure connector")
public class CEAzureConnectorDTO extends ConnectorConfigDTO {
  @NotEmpty(message = "FeaturesEnabled can't be empty") List<CEFeatures> featuresEnabled;

  @NotNull String tenantId;
  @NotNull String subscriptionId;
  @Valid BillingExportSpecDTO billingExportSpec;

  @Override
  public List<DecryptableEntity> getDecryptableEntities() {
    return null;
  }
}
