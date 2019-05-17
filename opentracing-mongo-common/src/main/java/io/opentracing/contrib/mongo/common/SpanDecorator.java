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
import com.mongodb.event.CommandStartedEvent;
import com.mongodb.event.CommandSucceededEvent;
import io.opentracing.Span;
import io.opentracing.tag.Tags;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static io.opentracing.contrib.mongo.common.TracingCommandListener.COMPONENT_NAME;

public interface SpanDecorator {

  SpanDecorator DEFAULT = new DefaultSpanDecorator();

  void commandStarted(CommandStartedEvent event, Span span);

  void commandSucceeded(CommandSucceededEvent event, Span span);

  void commandFailed(CommandFailedEvent event, Span span);

}

final class DefaultSpanDecorator implements SpanDecorator {

  @Override
  public void commandStarted(CommandStartedEvent event, Span span) {
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

  @Override
  public void commandSucceeded(CommandSucceededEvent event, Span span) { }

  @Override
  public void commandFailed(CommandFailedEvent event, Span span) {
    Tags.ERROR.set(span, Boolean.TRUE);
    span.log(errorLogs(event.getThrowable()));
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

