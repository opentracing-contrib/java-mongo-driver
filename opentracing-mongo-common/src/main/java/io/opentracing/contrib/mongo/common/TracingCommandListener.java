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
package io.opentracing.contrib.mongo.common;

import com.mongodb.event.CommandFailedEvent;
import com.mongodb.event.CommandListener;
import com.mongodb.event.CommandStartedEvent;
import com.mongodb.event.CommandSucceededEvent;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.mongo.common.providers.MongoSpanNameProvider;
import io.opentracing.contrib.mongo.common.providers.NoopSpanNameProvider;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.bson.BsonNull;
import org.bson.BsonValue;

/**
 * In Async Mongo driver methods of this Listener run in different threads therefore cache is used
 */
public class TracingCommandListener implements CommandListener {
  public static final String COMPONENT_NAME = "java-mongo";

  private final Tracer tracer;
  private final List<ExcludedCommand> excludedCommands;
  private final List<SpanDecorator> decorators;
  private final MongoSpanNameProvider mongoSpanNameProvider;
  /**
   * Cache for (request id, span) pairs
   */
  private final Map<Integer, Span> cache = new ConcurrentHashMap<>();


  public static class Builder {
    private Tracer tracer;
    private List<ExcludedCommand> excludedCommands;
    private List<SpanDecorator> decorators;
    private MongoSpanNameProvider spanNameProvider;

    public Builder() {
    }

    public Builder(Tracer tracer) {
      this.tracer = tracer;
    }

    public Builder withTracer(Tracer tracer) {
      this.tracer = tracer;
      return this;
    }

    public Builder withExcludedCommands(List<ExcludedCommand> excludedCommands) {
      this.excludedCommands = excludedCommands;
      return this;
    }

    /**
     * Specify decorators for use by this listener. By default, {@link SpanDecorator#DEFAULT}.
     * Decorators are applied in list iteration order.
     */
    public Builder withSpanDecorators(List<SpanDecorator> decorators) {
      this.decorators = new ArrayList<>(decorators);
      return this;
    }

    public Builder withSpanNameProvider(MongoSpanNameProvider spanNameProvider) {
      this.spanNameProvider = spanNameProvider;
      return this;
    }

    public TracingCommandListener build() {
      if (tracer == null) {
        tracer = GlobalTracer.get();
      }
      if (spanNameProvider == null) {
        spanNameProvider = new NoopSpanNameProvider();
      }
      if (excludedCommands == null) {
        excludedCommands = Collections.emptyList();
      }
      if (decorators == null) {
        decorators = Collections.singletonList(SpanDecorator.DEFAULT);
      }

      return new TracingCommandListener(tracer, spanNameProvider, excludedCommands, decorators);
    }
  }

  public TracingCommandListener(Tracer tracer, MongoSpanNameProvider customNameProvider,
      List<ExcludedCommand> excludedCommands) {
    this(tracer, customNameProvider, excludedCommands,
        Collections.singletonList(SpanDecorator.DEFAULT));
  }

  public TracingCommandListener(Tracer tracer, MongoSpanNameProvider customNameProvider,
      List<ExcludedCommand> excludedCommands, List<SpanDecorator> decorators) {
    this.tracer = tracer;
    this.mongoSpanNameProvider = customNameProvider;
    this.excludedCommands = new ArrayList<>(excludedCommands);
    this.decorators = decorators;
  }


  @Override
  public void commandStarted(CommandStartedEvent event) {
    Span span = buildSpan(event);
    if (span != null) {
      cache.put(event.getRequestId(), span);
    }
  }

  @Override
  public void commandSucceeded(CommandSucceededEvent event) {
    Span span = cache.remove(event.getRequestId());
    if (span != null) {
      for (SpanDecorator decorator : decorators) {
        decorator.commandSucceeded(event, span);
      }
      span.finish();
    }
  }

  @Override
  public void commandFailed(CommandFailedEvent event) {
    Span span = cache.remove(event.getRequestId());
    if (span != null) {
      for (SpanDecorator decorator : decorators) {
        decorator.commandFailed(event, span);
      }
      span.finish();
    }
  }

  Span buildSpan(CommandStartedEvent event) {
    for (ExcludedCommand excludedCommand : excludedCommands) {

      boolean skip = true;
      for (Entry<String, BsonValue> entry : excludedCommand.entrySet()) {
        if (!event.getCommand().containsKey(entry.getKey())) {
          skip = false;
          break;
        }
        if (entry.getValue() != BsonNull.VALUE
            && !entry.getValue().equals(event.getCommand().get(entry.getKey()))) {
          skip = false;
          break;
        }
      }
      if (skip) {
        return null;
      }
    }

    Tracer.SpanBuilder spanBuilder = tracer
        .buildSpan(mongoSpanNameProvider.generateName(event))
        .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);

        if (tracer.activeSpan() != null) {
            spanBuilder.asChildOf(tracer.activeSpan());
        }

        Span span = spanBuilder.start();
        for (SpanDecorator decorator : decorators) {
          decorator.commandStarted(event, span);
        }

    return span;
  }

}
