/*
 * Copyright 2017-2018 The OpenTracing Authors
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

import org.bson.BsonDocument;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.ServerAddress;
import com.mongodb.connection.ClusterId;
import com.mongodb.connection.ConnectionDescription;
import com.mongodb.connection.ServerId;
import com.mongodb.event.CommandStartedEvent;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.mongo.providers.MongoSpanNameProvider;
import io.opentracing.contrib.mongo.providers.NoopSpanNameProvider;
import io.opentracing.contrib.mongo.providers.PrefixSpanNameProvider;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;

import static org.junit.Assert.assertEquals;

public class TracingCommandListenerTest {

  private Tracer tracer = new MockTracer();

  private MongoSpanNameProvider prefixSpanName;

  private MongoSpanNameProvider operationName;
  private TracingCommandListener withProvider;
  private TracingCommandListener withoutProvider;
  private CommandStartedEvent event;
  private Span span;

  @Before
  public void setUp() {
    operationName = new NoopSpanNameProvider();
    prefixSpanName = new PrefixSpanNameProvider("mongo.");
    withProvider = new TracingCommandListener(tracer, prefixSpanName);
    withoutProvider = new TracingCommandListener(tracer);
    event = new CommandStartedEvent(
        1
        , new ConnectionDescription(new ServerId(new ClusterId(), new ServerAddress()))
        , "databaseName"
        , "commandName"
        , new BsonDocument()
    );
  }

  @Test
  public void testDefault() {
    span = withoutProvider.buildSpan(event);
    MockSpan mockSpan = (MockSpan) span;
    assertEquals(mockSpan.operationName(), operationName.generateName(event.getCommandName()));
  }

  @Test
  public void testPrefix() {
    span = withProvider.buildSpan(event);
    MockSpan mockSpan = (MockSpan) span;
    assertEquals(mockSpan.operationName(), prefixSpanName.generateName(event.getCommandName()));
  }
}
