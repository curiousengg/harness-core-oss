package software.wings.app;

import static io.harness.AuthorizationServiceHeader.MANAGER;
import static io.harness.eventsframework.EventsFrameworkConstants.DEFAULT_TOPIC_SIZE;
import static io.harness.eventsframework.EventsFrameworkConstants.DUMMY_GROUP_NAME;
import static io.harness.eventsframework.EventsFrameworkConstants.DUMMY_TOPIC_NAME;
import static io.harness.eventsframework.EventsFrameworkConstants.ENTITY_ACTIVITY;
import static io.harness.eventsframework.EventsFrameworkConstants.ENTITY_ACTIVITY_MAX_TOPIC_SIZE;
import static io.harness.eventsframework.EventsFrameworkConstants.ENTITY_CRUD;
import static io.harness.eventsframework.EventsFrameworkConstants.ENTITY_CRUD_MAX_PROCESSING_TIME;
import static io.harness.eventsframework.EventsFrameworkConstants.ENTITY_CRUD_MAX_TOPIC_SIZE;
import static io.harness.eventsframework.EventsFrameworkConstants.ENTITY_CRUD_READ_BATCH_SIZE;
import static io.harness.eventsframework.EventsFrameworkConstants.FEATURE_FLAG_MAX_TOPIC_SIZE;
import static io.harness.eventsframework.EventsFrameworkConstants.FEATURE_FLAG_STREAM;
import static io.harness.eventsframework.EventsFrameworkConstants.SAML_AUTHORIZATION_ASSERTION;

import io.harness.configuration.DeployMode;
import io.harness.eventsframework.EventsFrameworkConfiguration;
import io.harness.eventsframework.api.Consumer;
import io.harness.eventsframework.api.Producer;
import io.harness.eventsframework.impl.noop.NoOpConsumer;
import io.harness.eventsframework.impl.noop.NoOpProducer;
import io.harness.eventsframework.impl.redis.RedisConsumer;
import io.harness.eventsframework.impl.redis.RedisProducer;
import io.harness.redis.RedisConfig;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class EventsFrameworkModule extends AbstractModule {
  private final EventsFrameworkConfiguration eventsFrameworkConfiguration;
  private final boolean isEventsFrameworkAvailableInOnPrem;

  @Override
  protected void configure() {
    final RedisConfig redisConfig = this.eventsFrameworkConfiguration.getRedisConfig();

    final String deployMode = System.getenv(DeployMode.DEPLOY_MODE);
    if ((DeployMode.isOnPrem(deployMode) && !isEventsFrameworkAvailableInOnPrem)
        || redisConfig.getRedisUrl().equals("dummyRedisUrl")) {
      bind(Producer.class).annotatedWith(Names.named(ENTITY_CRUD)).toInstance(NoOpProducer.of(DUMMY_TOPIC_NAME));
      bind(Consumer.class)
          .annotatedWith(Names.named(ENTITY_CRUD))
          .toInstance(NoOpConsumer.of(DUMMY_TOPIC_NAME, DUMMY_GROUP_NAME));
      bind(Producer.class)
          .annotatedWith(Names.named(FEATURE_FLAG_STREAM))
          .toInstance(NoOpProducer.of(DUMMY_TOPIC_NAME));
      bind(Producer.class).annotatedWith(Names.named(ENTITY_ACTIVITY)).toInstance(NoOpProducer.of(DUMMY_TOPIC_NAME));
      bind(Producer.class)
          .annotatedWith(Names.named(SAML_AUTHORIZATION_ASSERTION))
          .toInstance(NoOpProducer.of(DUMMY_TOPIC_NAME));
    } else {
      bind(Producer.class)
          .annotatedWith(Names.named(ENTITY_CRUD))
          .toInstance(RedisProducer.of(ENTITY_CRUD, redisConfig, ENTITY_CRUD_MAX_TOPIC_SIZE, MANAGER.getServiceId()));
      bind(Consumer.class)
          .annotatedWith(Names.named(ENTITY_CRUD))
          .toInstance(RedisConsumer.of(ENTITY_CRUD, MANAGER.getServiceId(), redisConfig,
              ENTITY_CRUD_MAX_PROCESSING_TIME, ENTITY_CRUD_READ_BATCH_SIZE));
      bind(Producer.class)
          .annotatedWith(Names.named(FEATURE_FLAG_STREAM))
          .toInstance(
              RedisProducer.of(FEATURE_FLAG_STREAM, redisConfig, FEATURE_FLAG_MAX_TOPIC_SIZE, MANAGER.getServiceId()));
      bind(Producer.class)
          .annotatedWith(Names.named(ENTITY_ACTIVITY))
          .toInstance(
              RedisProducer.of(ENTITY_ACTIVITY, redisConfig, ENTITY_ACTIVITY_MAX_TOPIC_SIZE, MANAGER.getServiceId()));
      bind(Producer.class)
          .annotatedWith(Names.named(SAML_AUTHORIZATION_ASSERTION))
          .toInstance(
              RedisProducer.of(SAML_AUTHORIZATION_ASSERTION, redisConfig, DEFAULT_TOPIC_SIZE, MANAGER.getServiceId()));
    }
  }
}
