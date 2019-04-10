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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
  private final MongoSpanNameProvider mongoSpanNameProvider;
  /**
   * Cache for (request id, span) pairs
   */
  private final Map<Integer, Span> cache = new ConcurrentHashMap<>();


  public static class Builder {
    private Tracer tracer;
    private List<ExcludedCommand> excludedCommands;
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

      return new TracingCommandListener(tracer, spanNameProvider, excludedCommands);
    }
  }

  public TracingCommandListener(Tracer tracer, MongoSpanNameProvider customNameProvider,
      List<ExcludedCommand> excludedCommands) {
    this.tracer = tracer;
    this.mongoSpanNameProvider = customNameProvider;
    this.excludedCommands = new ArrayList<>(excludedCommands);
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
      span.finish();
    }
  }

  @Override
  public void commandFailed(CommandFailedEvent event) {
    Span span = cache.remove(event.getRequestId());
    if (span != null) {
      onError(span, event.getThrowable());
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
        .buildSpan(mongoSpanNameProvider.generateName(event.getCommandName()))
        .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);

    Span span = spanBuilder.start();
    decorate(span, event);

    return span;
  }

  private static void decorate(Span span, CommandStartedEvent event) {
    Tags.COMPONENT.set(span, COMPONENT_NAME);
    Tags.DB_STATEMENT.set(span, event.getCommand().toString());
    Tags.DB_INSTANCE.set(span, event.getDatabaseName());

    Tags.PEER_HOSTNAME.set(span, event.getConnectionDescription().getServerAddress().getHost());

    InetAddress inetAddress = event.getConnectionDescription().getServerAddress().getSocketAddress()
        .getAddress();

    if (inetAddress instanceof Inet4Address) {
      byte[] address = inetAddress.getAddress();
      Tags.PEER_HOST_IPV4.set(span, ByteBuffer.wrap(address).getInt());
    } else {
      Tags.PEER_HOST_IPV6.set(span, inetAddress.getHostAddress());
    }

    Tags.PEER_PORT.set(span, event.getConnectionDescription().getServerAddress().getPort());
    Tags.DB_TYPE.set(span, "mongo");
  }

  private static void onError(Span span, Throwable throwable) {
    Tags.ERROR.set(span, Boolean.TRUE);
    span.log(errorLogs(throwable));
  }

  private static Map<String, Object> errorLogs(Throwable throwable) {
    Map<String, Object> errorLogs = new HashMap<>(4);
    errorLogs.put("event", Tags.ERROR.getKey());
    errorLogs.put("error.kind", throwable.getClass().getName());
    errorLogs.put("error.object", throwable);

    errorLogs.put("message", throwable.getMessage());

    StringWriter sw = new StringWriter();
    throwable.printStackTrace(new PrintWriter(sw));
    errorLogs.put("stack", sw.toString());

    return errorLogs;
  }
}
