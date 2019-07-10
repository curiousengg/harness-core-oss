package software.wings.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.harness.CategoryTest;
import io.harness.category.element.UnitTests;
import io.harness.configuration.ConfigurationType;
import io.harness.mongo.MongoConfig;
import io.harness.rest.RestResponse;
import io.harness.security.AsymmetricEncryptor;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import software.wings.app.MainConfiguration;
import software.wings.exception.WingsExceptionMapper;
import software.wings.utils.ResourceTestRule;

import javax.ws.rs.core.GenericType;

public class HealthResourceTest extends CategoryTest {
  public static final MainConfiguration configuration = mock(MainConfiguration.class);
  public static final AsymmetricEncryptor asymmetricEncryptor = mock(AsymmetricEncryptor.class);

  @ClassRule
  public static final ResourceTestRule RESOURCES =
      ResourceTestRule.builder()
          .addResource(new HealthResource(configuration, asymmetricEncryptor, null))
          .addProvider(WingsExceptionMapper.class)
          .build();

  @Test
  @Category(UnitTests.class)
  public void shouldGetMongoUri() throws Exception {
    when(configuration.getMongoConnectionFactory())
        .thenReturn(MongoConfig.builder()
                        .uri("mongodb://localhost:27017/wings")
                        .locksUri("mongodb://localhost:27017/wings")
                        .build());

    when(asymmetricEncryptor.encryptText("mongodb://localhost:27017/wings"))
        .thenReturn("mongodb://localhost:27017/wings".getBytes());
    RestResponse<MongoConfig> restResponse =
        RESOURCES.client()
            .target("/health/configuration?configurationType=" + ConfigurationType.MONGO)
            .request()
            .get(new GenericType<RestResponse<MongoConfig>>() {});

    assertThat(restResponse.getResource()).isNotNull();
    assertThat(restResponse.getResource().getEncryptedUri()).isNotEmpty();
    assertThat(restResponse.getResource().getEncryptedLocksUri()).isNotEmpty();
  }
}