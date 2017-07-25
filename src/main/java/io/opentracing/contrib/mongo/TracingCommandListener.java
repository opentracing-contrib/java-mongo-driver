package io.opentracing.contrib.mongo;

import com.mongodb.event.CommandFailedEvent;
import com.mongodb.event.CommandListener;
import com.mongodb.event.CommandStartedEvent;
import com.mongodb.event.CommandSucceededEvent;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In Async Mongo driver methods of this Listener run in different threads therefore cache is used
 */
public class TracingCommandListener implements CommandListener {

  static final String COMPONENT_NAME = "java-mongo";
  private final Tracer tracer;
  /**
   * Cache for (request id, span) pairs
   */
  private final Map<Integer, Span> cache = new ConcurrentHashMap<>();

  TracingCommandListener(Tracer tracer) {
    this.tracer = tracer;
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

  private Span buildSpan(CommandStartedEvent event) {
    Tracer.SpanBuilder spanBuilder = tracer.buildSpan(event.getCommandName())
        .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);

    Span span = spanBuilder.startManual();
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
