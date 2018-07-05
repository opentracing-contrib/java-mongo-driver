package io.opentracing.contrib.mongo.providers;

public interface MongoSpanNameProvider {

  String NO_OPERATION = "unknown";

  String generateName(final String operationName);
}
