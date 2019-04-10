/*
 * Copyright 2017-2019 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.opentracing.contrib.mongo;

import static org.junit.Assert.assertEquals;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoDriverInformation;
import io.opentracing.noop.NoopTracerFactory;
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
    mongoClient.close();
  }
}