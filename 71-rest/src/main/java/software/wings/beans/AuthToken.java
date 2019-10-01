package software.wings.beans;

import static software.wings.beans.Application.GLOBAL_APP_ID;
import static software.wings.utils.CryptoUtils.secureRandAlphaNumString;

import io.harness.annotation.HarnessEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexed;
import org.simpleframework.xml.Transient;

import java.time.OffsetDateTime;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldNameConstants(innerTypeName = "AuthTokenKeys")
@Entity(value = "authTokens", noClassnameStored = true)
@HarnessEntity(exportable = true)
public class AuthToken extends Base {
  @Transient private User user;
  private String accountId;
  private String userId;
  private long expireAt;
  private String jwtToken;
  private boolean refreshed;

  @Indexed(options = @IndexOptions(expireAfterSeconds = 0))
  private Date validUntil = Date.from(OffsetDateTime.now().plusDays(7).toInstant());

  /**
   * Instantiates a new auth token.
   *
   * @param accountId           the account id
   * @param userId              the user id
   * @param tokenExpiryInMillis the token expiry in millis
   */
  public AuthToken(String accountId, String userId, Long tokenExpiryInMillis) {
    this.accountId = accountId;
    this.userId = userId;
    setUuid(secureRandAlphaNumString(32));
    setAppId(GLOBAL_APP_ID);
    expireAt = System.currentTimeMillis() + tokenExpiryInMillis;
  }
}
