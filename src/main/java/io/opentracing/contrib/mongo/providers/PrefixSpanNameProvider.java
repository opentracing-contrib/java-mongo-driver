package io.opentracing.contrib.mongo.providers;

public class PrefixSpanNameProvider implements MongoSpanNameProvider {

  private static final String NO_OPERATION = "unknown";

  private final String prefix;

  public PrefixSpanNameProvider(String prefix) {
    this.prefix = prefix;
  }

  @Override
  public String generateName(String operationName) {
    return ((prefix == null) ? "" : prefix)
      + ((operationName == null) ? NO_OPERATION : operationName);
  }
}
