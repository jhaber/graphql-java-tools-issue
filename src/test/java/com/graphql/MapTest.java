package com.graphql;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.coxautodev.graphql.tools.SchemaParser;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;

public class MapTest {
  private static final Map<String, Map<String, String>> EXPECTED_DATA = ImmutableMap.of(
      "greetOne", ImmutableMap.of("value", "Hello one"),
      "greetTwo", ImmutableMap.of("value", "Hello two")
  );

  @Test
  public void itHandlesReusedMapTypeWhenAddedToDictionary() throws Exception {
    String schemaString = readResourceToString("schema.graphqls");
    String queryString = readResourceToString("query.graphql");

    GraphQLSchema schema = SchemaParser
        .newParser()
        .resolvers(new QueryResolver())
        .dictionary("Greeting", Greeting.class)
        .schemaString(schemaString)
        .build()
        .makeExecutableSchema();

    GraphQL graphQL = GraphQL.newGraphQL(schema).build();

    ExecutionResult result = graphQL.execute(queryString);

    assertThat(result.<Map<String, Map<String, String>>>getData()).isEqualTo(EXPECTED_DATA);
  }

  @Test
  public void itHandlesReusedMapTypeWhenNotAddedToDictionary() throws Exception {
    String schemaString = readResourceToString("schema.graphqls");
    String queryString = readResourceToString("query.graphql");

    GraphQLSchema schema = SchemaParser
        .newParser()
        .resolvers(new QueryResolver())
        .schemaString(schemaString)
        .build()
        .makeExecutableSchema();

    GraphQL graphQL = GraphQL.newGraphQL(schema).build();

    ExecutionResult result = graphQL.execute(queryString);

    assertThat(result.<Map<String, Map<String, String>>>getData()).isEqualTo(EXPECTED_DATA);
  }

  @Test
  public void itHandlesNotReusedMapTypeWhenAddedToDictionary() throws Exception {
    String schemaString = readResourceToString("schema2.graphqls");
    String queryString = readResourceToString("query.graphql");

    GraphQLSchema schema = SchemaParser
        .newParser()
        .resolvers(new QueryResolver())
        .dictionary("Greeting2", Greeting.class)
        .schemaString(schemaString)
        .build()
        .makeExecutableSchema();

    GraphQL graphQL = GraphQL.newGraphQL(schema).build();

    ExecutionResult result = graphQL.execute(queryString);

    assertThat(result.<Map<String, Map<String, String>>>getData()).isEqualTo(EXPECTED_DATA);
  }

  @Test
  public void itHandlesNotReusedMapTypeWhenNotAddedToDictionary() throws Exception {
    String schemaString = readResourceToString("schema2.graphqls");
    String queryString = readResourceToString("query.graphql");

    GraphQLSchema schema = SchemaParser
        .newParser()
        .resolvers(new QueryResolver())
        .schemaString(schemaString)
        .build()
        .makeExecutableSchema();

    GraphQL graphQL = GraphQL.newGraphQL(schema).build();

    ExecutionResult result = graphQL.execute(queryString);

    assertThat(result.<Map<String, Map<String, String>>>getData()).isEqualTo(EXPECTED_DATA);
  }

  private static String readResourceToString(String resourceName) throws IOException {
    return Resources.toString(
        Resources.getResource(resourceName),
        StandardCharsets.UTF_8
    );
  }

  public static class Greeting {
    private final String value;

    public Greeting(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }

  public static class QueryResolver extends AbstractMap<String, Greeting> implements GraphQLQueryResolver {

    public Greeting greetOne() {
      return new Greeting("Hello one");
    }

    @Override
    public Greeting get(Object key) {
      if ("greetTwo".equals(key)) {
        return new Greeting("Hello two");
      } else {
        throw new IllegalArgumentException();
      }
    }

    @Override
    public Set<Entry<String, Greeting>> entrySet() {
      throw new UnsupportedOperationException();
    }
  }
}
