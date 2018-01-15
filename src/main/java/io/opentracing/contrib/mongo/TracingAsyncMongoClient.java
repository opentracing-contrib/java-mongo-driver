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


import com.mongodb.ClientSessionOptions;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.ListDatabasesIterable;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClientSettings;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoDatabase;
import com.mongodb.async.client.MongoIterable;
import com.mongodb.client.MongoDriverInformation;
import com.mongodb.session.ClientSession;
import io.opentracing.Tracer;
import org.bson.Document;


/**
 * Tracing decorator for {@link MongoClient}
 */
public class TracingAsyncMongoClient implements MongoClient {

  private final MongoClient mongoClient;


  public TracingAsyncMongoClient(final Tracer tracer, final MongoClientSettings settings) {
    this(tracer, settings, null);
  }

  public TracingAsyncMongoClient(final Tracer tracer, final MongoClientSettings settings,
      final MongoDriverInformation mongoDriverInformation) {
    TracingCommandListener tracingCommandListener = new TracingCommandListener(tracer);
    this.mongoClient = MongoClients.create(MongoClientSettings.builder(settings)
            .addCommandListener(tracingCommandListener)
            .build(),
        mongoDriverInformation);
  }

  @Override
  public void startSession(ClientSessionOptions clientSessionOptions,
      SingleResultCallback<ClientSession> singleResultCallback) {

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MongoDatabase getDatabase(String name) {
    return mongoClient.getDatabase(name);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void close() {
    mongoClient.close();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MongoClientSettings getSettings() {
    return mongoClient.getSettings();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MongoIterable<String> listDatabaseNames() {
    return mongoClient.listDatabaseNames();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MongoIterable<String> listDatabaseNames(ClientSession clientSession) {
    return mongoClient.listDatabaseNames(clientSession);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ListDatabasesIterable<Document> listDatabases() {
    return mongoClient.listDatabases();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ListDatabasesIterable<Document> listDatabases(ClientSession clientSession) {
    return mongoClient.listDatabases(clientSession);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <TResult> ListDatabasesIterable<TResult> listDatabases(Class<TResult> tResultClass) {
    return mongoClient.listDatabases(tResultClass);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <TResult> ListDatabasesIterable<TResult> listDatabases(ClientSession clientSession,
      Class<TResult> aClass) {
    return mongoClient.listDatabases(clientSession, aClass);
  }
}
