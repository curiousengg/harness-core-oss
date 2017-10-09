package software.wings.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mongodb.morphia.annotations.Entity;
import software.wings.core.queue.Queuable;

/**
 * Created by rsingh on 10/6/17.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(value = "kmsTransitionEvent", noClassnameStored = true)
public class KmsTransitionEvent extends Queuable {
  private String entityId;
  private String fromKmsId;
  private String toKmsId;
  private String accountId;
}
