package software.wings.graphql.scalar;

import graphql.language.IntValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.function.Function;

/**
 * Created this GraphQLScalar for DateTime scalar.
 * At present adding support for String/Long/Int conversion to date.
 */
public final class GraphQLDateTimeScalar {
  public static String INVALID_INPUT_INSTANCE_TYPE = "DateTime scalars needs input to be instanceof Long but it was: ";
  public static String INVALID_AST_TYPE = "Expected AST type 'StringValue' but was: ";

  public static final GraphQLScalarType type =
      GraphQLScalarType.newScalar()
          .name("DateTime")
          .description("DateTime Scalar")
          .coercing(new Coercing<Long, Long>() {
            @Override
            public Long serialize(Object input) throws CoercingSerializeException {
              if (input instanceof Long) {
                return (Long) input;
              }
              throw new CoercingSerializeException(INVALID_INPUT_INSTANCE_TYPE + typeName(input));
            }

            @Override
            public Long parseValue(Object input) throws CoercingParseValueException {
              try {
                Long date = parseInput(input);
                if (date == null) {
                  throw new CoercingParseValueException(INVALID_INPUT_INSTANCE_TYPE + typeName(input));
                }
                return date;
              } catch (CoercingParseValueException e) {
                throw e;
              } catch (Exception e) {
                throw new CoercingParseValueException(e);
              }
            }

            @Override
            public Long parseLiteral(Object input) throws CoercingParseLiteralException {
              try {
                Long date = parseInput(input);
                if (date == null) {
                  throw new CoercingParseLiteralException(INVALID_INPUT_INSTANCE_TYPE + typeName(input));
                }
                return date;
              } catch (CoercingParseLiteralException e) {
                throw e;
              } catch (Exception e) {
                throw new CoercingParseLiteralException(e);
              }
            }

            private Long parseInput(Object input) {
              if (input instanceof String) {
                return Long.parseLong((String) input);
              } else if (input instanceof Long) {
                return (Long) input;
              } else if (input instanceof IntValue) {
                return ((IntValue) input).getValue().longValue();
              } else if (input instanceof graphql.language.StringValue) {
                return Long.parseLong(((graphql.language.StringValue) input).getValue());
              }
              return null;
            }
          })
          .build();

  public static String typeName(Object input) {
    if (input == null) {
      return "null";
    }
    return input.getClass().getSimpleName();
  }

  public static OffsetDateTime parseOffsetDateTime(String s, Function<String, RuntimeException> exceptionMaker) {
    try {
      return OffsetDateTime.parse(s, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    } catch (DateTimeParseException e) {
      throw exceptionMaker.apply("Invalid RFC3339 value : '" + s + "'. because of : '" + e.getMessage() + "'");
    }
  }
}
