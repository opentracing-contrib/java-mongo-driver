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

import java.util.function.Function;

import org.junit.Before;


//import com.mongodb.event.CommandStartedEvent;

import io.opentracing.Tracer;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;

//import static org.junit.Assert.assertEquals;

public class TracingCommandListenerTest {

  Tracer tracer = new MockTracer();
  private MockSpan span;
  private String prefix = "mongo.";
  Function<String, String> prefixSpanName;
  TracingCommandListener withProvider;
  TracingCommandListener withoutProvider;

  @Before
  public void setUp() {
    prefixSpanName = MongoSpanNameProvider.PREFIX_OPERATION_NAME(prefix);
    withProvider = new TracingCommandListener(tracer, prefixSpanName);
    withoutProvider = new TracingCommandListener(tracer);
  }

}
