package io.harness.testframework.restutils;

import io.harness.exception.InvalidRequestException;
import io.harness.testframework.framework.Setup;

import io.restassured.http.ContentType;
import io.restassured.mapper.ObjectMapperType;
import io.restassured.path.json.JsonPath;
import java.util.HashMap;
import java.util.Map;

public class GraphQLRestUtils {
  private GraphQLRestUtils() {
    throw new IllegalStateException("Utility class");
  }

  public static Map<Object, Object> executeGraphQLQuery(String bearerToken, String accountId, String query) {
    Map<String, Object> queryObj = new HashMap<>();
    queryObj.put("query", query);
    queryObj.put("variables", null);
    JsonPath jsonPath = Setup.portal()
                            .auth()
                            .oauth2(bearerToken)
                            .queryParam("accountId", accountId)
                            .body(queryObj, ObjectMapperType.GSON)
                            .contentType(ContentType.JSON)
                            .post("/graphql")
                            .jsonPath();

    Map<Object, Object> resource = jsonPath.getMap("data");
    if (resource == null) {
      throw new InvalidRequestException("");
    }

    return resource;
  }
}
