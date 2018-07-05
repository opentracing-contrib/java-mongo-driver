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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mongodb.event.CommandFailedEvent;
import com.mongodb.event.CommandListener;
import com.mongodb.event.CommandStartedEvent;
import com.mongodb.event.CommandSucceededEvent;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.mongo.providers.MongoSpanNameProvider;
import io.opentracing.contrib.mongo.providers.NoopSpanNameProvider;
import io.opentracing.tag.Tags;

/**
 * In Async Mongo driver methods of this Listener run in different threads therefore cache is used
 */
public class TracingCommandListener implements CommandListener {

  static final String COMPONENT_NAME = "java-mongo";
  private final Tracer tracer;

  private final MongoSpanNameProvider mongoSpanNameProvider;
  /**
   * Cache for (request id, span) pairs
   */
  private final Map<Integer, Span> cache = new ConcurrentHashMap<>();

  TracingCommandListener(Tracer tracer) {
    this.tracer = tracer;
    this.mongoSpanNameProvider = new NoopSpanNameProvider();
  }

  TracingCommandListener(Tracer tracer, MongoSpanNameProvider customNameProvider) {
    this.tracer = tracer;
    this.mongoSpanNameProvider = customNameProvider;
  }

  @Override
  public void commandStarted(CommandStartedEvent event) {
    Span span = buildSpan(event);
    cache.put(event.getRequestId(), span);
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
    Tracer.SpanBuilder spanBuilder = tracer.buildSpan(mongoSpanNameProvider.generateName(event.getCommandName()))
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
