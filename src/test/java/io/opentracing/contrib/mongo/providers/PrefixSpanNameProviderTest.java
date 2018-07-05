package io.opentracing.contrib.mongo.providers;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PrefixSpanNameProviderTest {

  private final MongoSpanNameProvider provider = new PrefixSpanNameProvider("mongo.");

  @Test
  public void testOperationNameExists() {
    assertEquals("mongo.insert", provider.generateName("insert"));
  }

  @Test
  public void testNullOperationName() {
    assertEquals("mongo.unknown", provider.generateName(null));
  }

  @Test
  public void testNullPrefixName() {
    assertEquals("insert", new PrefixSpanNameProvider(null).generateName("insert"));
  }
}
