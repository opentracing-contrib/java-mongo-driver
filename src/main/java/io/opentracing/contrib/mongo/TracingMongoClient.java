package io.opentracing.contrib.mongo;


import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDriverInformation;
import io.opentracing.Tracer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tracing Mongo Client
 *
 * @see MongoClient
 */
public class TracingMongoClient extends MongoClient {

  public TracingMongoClient(Tracer tracer) {
    this(tracer, new ServerAddress());
  }

  public TracingMongoClient(Tracer tracer, final String host) {
    this(tracer, new ServerAddress(host));
  }

  public TracingMongoClient(Tracer tracer, final String host, final MongoClientOptions options) {
    this(tracer, new ServerAddress(host), options);
  }

  public TracingMongoClient(Tracer tracer, final String host, final int port) {
    this(tracer, new ServerAddress(host, port));
  }

  public TracingMongoClient(Tracer tracer, final ServerAddress addr) {
    this(tracer, addr, new MongoClientOptions.Builder().build());
  }

  public TracingMongoClient(Tracer tracer, final ServerAddress addr,
      final List<MongoCredential> credentialsList) {
    this(tracer, addr, credentialsList, new MongoClientOptions.Builder().build());
  }

  public TracingMongoClient(Tracer tracer, final ServerAddress addr,
      final MongoClientOptions options) {
    super(addr, MongoClientOptions.builder(options).addCommandListener(new TracingCommandListener(
        tracer)).build());
  }

  public TracingMongoClient(Tracer tracer, final ServerAddress addr,
      final List<MongoCredential> credentialsList, final MongoClientOptions options) {
    super(addr, credentialsList,
        MongoClientOptions.builder(options).addCommandListener(new TracingCommandListener(
            tracer)).build());
  }

  public TracingMongoClient(Tracer tracer, final List<ServerAddress> seeds) {
    this(tracer, seeds, new MongoClientOptions.Builder().build());
  }

  public TracingMongoClient(Tracer tracer, final List<ServerAddress> seeds,
      final List<MongoCredential> credentialsList) {
    this(tracer, seeds, credentialsList, new MongoClientOptions.Builder().build());
  }

  public TracingMongoClient(Tracer tracer, final List<ServerAddress> seeds,
      final MongoClientOptions options) {
    super(seeds, MongoClientOptions.builder(options).addCommandListener(new TracingCommandListener(
        tracer)).build());
  }

  public TracingMongoClient(Tracer tracer, final List<ServerAddress> seeds,
      final List<MongoCredential> credentialsList, final MongoClientOptions options) {
    super(seeds, credentialsList,
        MongoClientOptions.builder(options).addCommandListener(new TracingCommandListener(
            tracer)).build());
  }

  public TracingMongoClient(Tracer tracer, final MongoClientURI uri) {
    this(tracer, uri, null);
  }

  public TracingMongoClient(Tracer tracer, final MongoClientURI uri,
      final MongoDriverInformation mongoDriverInformation) {
    this(tracer, toServerAddressList(uri.getHosts()),
        uri.getCredentials() != null ? Collections.singletonList(uri.getCredentials())
            : Collections.<MongoCredential>emptyList(),
        MongoClientOptions.builder().addCommandListener(new TracingCommandListener(tracer)).build(),
        mongoDriverInformation);
  }

  public TracingMongoClient(Tracer tracer, final ServerAddress addr,
      final List<MongoCredential> credentialsList, final MongoClientOptions options,
      final MongoDriverInformation mongoDriverInformation) {
    super(addr, credentialsList,
        MongoClientOptions.builder(options).addCommandListener(new TracingCommandListener(
            tracer)).build(), mongoDriverInformation);
  }

  public TracingMongoClient(Tracer tracer, final List<ServerAddress> seeds,
      final List<MongoCredential> credentialsList, final MongoClientOptions options,
      final MongoDriverInformation mongoDriverInformation) {
    super(seeds, credentialsList,
        MongoClientOptions.builder(options).addCommandListener(new TracingCommandListener(
            tracer)).build(), mongoDriverInformation);
  }

  private static List<ServerAddress> toServerAddressList(List<String> hosts) {
    List<ServerAddress> list = new ArrayList<>();
    for (String host : hosts) {
      list.add(new ServerAddress(host));
    }
    return list;
  }
}
