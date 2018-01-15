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

  @Deprecated
  public TracingMongoClient(Tracer tracer, final ServerAddress addr,
      final List<MongoCredential> credentialsList, final MongoClientOptions options) {
    super(addr, credentialsList,
        MongoClientOptions.builder(options).addCommandListener(new TracingCommandListener(
            tracer)).build());
  }


  public TracingMongoClient(Tracer tracer, final ServerAddress addr,
      final MongoCredential credential, final MongoClientOptions options) {
    super(addr, credential,
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

  @Deprecated
  public TracingMongoClient(Tracer tracer, final List<ServerAddress> seeds,
      final List<MongoCredential> credentialsList, final MongoClientOptions options) {
    super(seeds, credentialsList,
        MongoClientOptions.builder(options).addCommandListener(new TracingCommandListener(
            tracer)).build());
  }

  public TracingMongoClient(Tracer tracer, final List<ServerAddress> seeds,
      final MongoCredential credential, final MongoClientOptions options) {
    super(seeds, credential,
        MongoClientOptions.builder(options).addCommandListener(new TracingCommandListener(
            tracer)).build());
  }

  public TracingMongoClient(Tracer tracer, final MongoClientURI uri) {
    this(tracer, uri, null);
  }

  public TracingMongoClient(Tracer tracer, final List<ServerAddress> seeds,
      final MongoCredential credential, final MongoClientOptions options,
      final MongoDriverInformation mongoDriverInformation) {
    super(seeds, credential,
        MongoClientOptions.builder(options).addCommandListener(new TracingCommandListener(
            tracer)).build(), mongoDriverInformation);
  }

  @SuppressWarnings("deprecation")
  public TracingMongoClient(Tracer tracer, final MongoClientURI uri,
      final MongoDriverInformation mongoDriverInformation) {
    this(tracer, toServerAddressList(uri.getHosts()),
        uri.getCredentials() != null ? Collections.singletonList(uri.getCredentials())
            : Collections.<MongoCredential>emptyList(),
        uri.getOptions(),
        mongoDriverInformation);
  }

  @Deprecated
  public TracingMongoClient(Tracer tracer, final ServerAddress addr,
      final List<MongoCredential> credentialsList, final MongoClientOptions options,
      final MongoDriverInformation mongoDriverInformation) {
    super(addr, credentialsList,
        MongoClientOptions.builder(options).addCommandListener(new TracingCommandListener(
            tracer)).build(), mongoDriverInformation);
  }

  @Deprecated
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
