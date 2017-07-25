package io.opentracing.contrib.mongo;

import static org.junit.Assert.assertEquals;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDriverInformation;
import io.opentracing.NoopTracerFactory;
import java.net.UnknownHostException;
import org.junit.Test;

public class TracingMongoClientTest {

  @Test
  public void only_one_listener_added() throws UnknownHostException {
    MongoClient mongoClient = new TracingMongoClient(
        NoopTracerFactory.create(),
        new MongoClientURI("mongodb://localhost"),
        MongoDriverInformation.builder().build());
    assertEquals(1, mongoClient.getMongoClientOptions().getCommandListeners().size());
  }
}