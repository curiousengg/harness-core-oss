package software.wings.service.impl.aws.model;

import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AwsSecurityGroup implements Serializable {
  private String id;
  private String name;
}
