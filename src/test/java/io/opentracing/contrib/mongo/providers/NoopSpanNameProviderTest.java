package io.opentracing.contrib.mongo.providers;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NoopSpanNameProviderTest {

  private final MongoSpanNameProvider provider = new NoopSpanNameProvider();

  @Test
  public void testOperationNameExists() {
    assertEquals("insert", provider.generateName("insert"));
  }

  @Test
  public void testNullOperationName() {
    assertEquals("unknown", provider.generateName(null));
  }
}
