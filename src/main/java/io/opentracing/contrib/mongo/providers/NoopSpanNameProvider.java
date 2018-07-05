package io.opentracing.contrib.mongo.providers;

public class NoopSpanNameProvider implements MongoSpanNameProvider {

  @Override
  public String generateName(String operationName) {
    return ((operationName == null) ? NO_OPERATION : operationName);
  }
}
