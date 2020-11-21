package io.harness.yaml.utils;

import io.harness.serializer.AnnotationAwareJsonSubtypeResolver;
import io.harness.serializer.JsonSubtypeResolver;
import io.harness.serializer.jackson.NGHarnessJacksonModule;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.net.URL;
import lombok.experimental.UtilityClass;

/**
 * YamlPipelineUtils is used to convert arbitrary class from yaml file.
 * ObjectMapper is preconfigured for yaml parsing. It uses custom
 * {@link JsonSubtypeResolver} which is responsible for scanning entire code base
 * and registering classes that are extending base interfaces or abstract classes defined in this framworkd
 * End user will typically extend interface and define {@link com.fasterxml.jackson.annotation.JsonTypeName} annotation
 * with proper type name. This way framework is decoupled from concrete implementations.
 */
@UtilityClass
public class YamlPipelineUtils {
  private final ObjectMapper mapper;

  static {
    mapper = new ObjectMapper(new YAMLFactory());
    mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    mapper.setSubtypeResolver(AnnotationAwareJsonSubtypeResolver.newInstance(mapper.getSubtypeResolver()));
    mapper.registerModule(new Jdk8Module());
    mapper.registerModule(new GuavaModule());
    mapper.registerModule(new JavaTimeModule());
    mapper.registerModule(new NGHarnessJacksonModule());
  }

  /**
   * Read.
   *
   * @param <T>  the generic type
   * @param yaml the yaml
   * @param cls  the cls
   * @return the t
   * @throws JsonParseException   the json parse exception
   * @throws JsonMappingException the json mapping exception
   * @throws IOException          Signals that an I/O exception has occurred.
   */
  public <T> T read(String yaml, Class<T> cls) throws IOException {
    return mapper.readValue(yaml, cls);
  }

  /**
   * Read.
   *
   * @param <T>  the generic type
   * @param yaml the yaml
   * @param cls  the cls
   * @return the t
   * @throws JsonParseException   the json parse exception
   * @throws JsonMappingException the json mapping exception
   * @throws IOException          Signals that an I/O exception has occurred.
   */
  public <T> T read(URL yaml, Class<T> cls) throws IOException {
    return mapper.readValue(yaml, cls);
  }

  public String writeString(Object value) throws JsonProcessingException {
    return mapper.writeValueAsString(value);
  }

  public ObjectMapper getMapper() {
    return mapper;
  }
}
