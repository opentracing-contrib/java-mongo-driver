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
package io.opentracing.contrib.mongo.reactivestreams;


import java.util.List;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.reactivestreams.Publisher;
import com.mongodb.ClientSessionOptions;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoDriverInformation;
import com.mongodb.reactivestreams.client.*;
import io.opentracing.Tracer;
import io.opentracing.contrib.mongo.common.TracingCommandListener;

/**
 * Tracing decorator for {@link MongoClient}
 */
public class TracingReactiveStreamsMongoClient implements MongoClient {

    private final MongoClient mongoClient;

    public TracingReactiveStreamsMongoClient(final Tracer tracer, final MongoClientSettings settings) {
        this(tracer, settings, null);
    }

    public TracingReactiveStreamsMongoClient(final Tracer tracer, final MongoClientSettings settings, final MongoDriverInformation mongoDriverInformation) {
        TracingCommandListener tracingCommandListener = new TracingCommandListener.Builder(tracer).build();
        this.mongoClient = MongoClients.create(MongoClientSettings.builder(settings).addCommandListener(tracingCommandListener).build(), mongoDriverInformation);
    }

    @Override
    public MongoDatabase getDatabase(String name) {
        return mongoClient.getDatabase(name);
    }

    @Override
    public void close() {
        mongoClient.close();
    }

    @Override
    public Publisher<String> listDatabaseNames() {
        return mongoClient.listDatabaseNames();
    }

    @Override
    public Publisher<String> listDatabaseNames(ClientSession clientSession) {
        return mongoClient.listDatabaseNames(clientSession);
    }

    @Override
    public ListDatabasesPublisher<Document> listDatabases() {
        return mongoClient.listDatabases();
    }

    @Override
    public <TResult> ListDatabasesPublisher<TResult> listDatabases(Class<TResult> aClass) {
        return mongoClient.listDatabases(aClass);
    }

    @Override
    public ListDatabasesPublisher<Document> listDatabases(ClientSession clientSession) {
        return mongoClient.listDatabases(clientSession);
    }

    @Override
    public <TResult> ListDatabasesPublisher<TResult> listDatabases(ClientSession clientSession, Class<TResult> aClass) {
        return mongoClient.listDatabases(clientSession, aClass);
    }

    @Override
    public ChangeStreamPublisher<Document> watch() {
        return mongoClient.watch();
    }

    @Override
    public <TResult> ChangeStreamPublisher<TResult> watch(Class<TResult> aClass) {
        return mongoClient.watch(aClass);
    }

    @Override
    public ChangeStreamPublisher<Document> watch(List<? extends Bson> list) {
        return mongoClient.watch(list);
    }

    @Override
    public <TResult> ChangeStreamPublisher<TResult> watch(List<? extends Bson> list, Class<TResult> aClass) {
        return mongoClient.watch(list, aClass);
    }

    @Override
    public ChangeStreamPublisher<Document> watch(ClientSession clientSession) {
        return mongoClient.watch(clientSession);
    }

    @Override
    public <TResult> ChangeStreamPublisher<TResult> watch(ClientSession clientSession, Class<TResult> aClass) {
        return mongoClient.watch(clientSession, aClass);
    }

    @Override
    public ChangeStreamPublisher<Document> watch(ClientSession clientSession, List<? extends Bson> list) {
        return mongoClient.watch(clientSession, list);
    }

    @Override
    public <TResult> ChangeStreamPublisher<TResult> watch(ClientSession clientSession, List<? extends Bson> list, Class<TResult> aClass) {
        return mongoClient.watch(clientSession, list, aClass);
    }

    @Override
    public Publisher<ClientSession> startSession() {
        return mongoClient.startSession();
    }

    @Override
    public Publisher<ClientSession> startSession(ClientSessionOptions clientSessionOptions) {
        return mongoClient.startSession(clientSessionOptions);
    }

}
