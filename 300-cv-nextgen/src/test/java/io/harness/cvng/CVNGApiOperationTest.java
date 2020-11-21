package io.harness.cvng;

import static io.harness.rule.OwnerRule.RAGHU;

import static org.assertj.core.api.Assertions.assertThat;

import io.harness.CategoryTest;
import io.harness.category.element.UnitTests;
import io.harness.rule.Owner;

import com.google.common.collect.Sets;
import io.swagger.annotations.ApiOperation;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.reflections.Reflections;

public class CVNGApiOperationTest extends CategoryTest {
  private static final String BASE_PACKAGE = "io.harness.cvng";

  @Test
  @Owner(developers = RAGHU)
  @Category(UnitTests.class)
  public void testNickNameUniqueness() {
    // Not adding PATCH at present.
    Set<Class<? extends Annotation>> supportedAnnotation =
        Sets.newHashSet(GET.class, POST.class, PUT.class, DELETE.class);

    final Set<String> uniqueOperationName = Sets.newHashSet();
    Reflections ref = new Reflections(BASE_PACKAGE);

    final Class<? extends Annotation> getMethodClass = GET.class;
    final Class<? extends Annotation> apiOperationClass = ApiOperation.class;

    for (Class<?> klass : ref.getTypesAnnotatedWith(Path.class)) {
      for (final Method method : klass.getDeclaredMethods()) {
        supportedAnnotation.stream()
            .filter(annotation -> method.isAnnotationPresent(annotation))
            .forEach(annotation -> {
              assertThat(method.isAnnotationPresent(apiOperationClass))
                  .withFailMessage("ApiOperation annotation not present for " + klass)
                  .isTrue();
              ApiOperation apiOperationAnnotation = (ApiOperation) method.getAnnotation(apiOperationClass);
              assertThat(apiOperationAnnotation.nickname()).isNotBlank();
              assertThat(uniqueOperationName.contains(apiOperationAnnotation.nickname()))
                  .withFailMessage("nick name " + apiOperationAnnotation.nickname() + " already exists")
                  .isFalse();
              uniqueOperationName.add(apiOperationAnnotation.nickname());
            });
      }
    }
  }
}
