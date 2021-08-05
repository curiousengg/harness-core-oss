package io.harness.delegate.beans.connector.awsconnector;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModel;
import java.util.Set;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName(AwsConstants.IRSA)
@ApiModel("AwsIRSA")
public class AwsIRSASpecDTO implements AwsCredentialSpecDTO {
  @NotNull @Size(min = 1) Set<String> delegateSelectors;
}
