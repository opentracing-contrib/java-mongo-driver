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
package io.opentracing.contrib.mongo;

import static com.mongodb.client.MongoClients.create;
import java.util.List;
import org.bson.Document;
import org.bson.conversions.Bson;
import com.mongodb.ClientSessionOptions;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ReadPreference;
import com.mongodb.client.*;
import com.mongodb.connection.ClusterDescription;
import io.opentracing.contrib.mongo.common.TracingCommandListener;

/**
 * Tracing Mongo Client
 *
 * @see MongoClient
 */
public class TracingMongoClient implements MongoClient {

    private MongoClient mongoClient;


    public TracingMongoClient(TracingCommandListener listener, String mongoDbUri, String applicationName) {
        this(listener, mongoDbUri, applicationName, ReadPreference.primaryPreferred());
    }

    public TracingMongoClient(TracingCommandListener listener, String mongoDbUri, String applicationName, ReadPreference readPreference) {
        MongoClientSettings settings = MongoClientSettings.builder().addCommandListener(listener).applyConnectionString(new ConnectionString(mongoDbUri)).applicationName(applicationName)
                .readPreference(readPreference).build();
        this.mongoClient = create(settings);
    }

    /**
     * @return
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return mongoClient.hashCode();
    }

    /**
     * @param databaseName
     * @return
     * @see com.mongodb.client.internal.MongoClientImpl#getDatabase(java.lang.String)
     */
    @Override
    public MongoDatabase getDatabase(String databaseName) {
        return mongoClient.getDatabase(databaseName);
    }

    /**
     * @return
     * @see com.mongodb.client.internal.MongoClientImpl#listDatabaseNames()
     */
    @Override
    public MongoIterable<String> listDatabaseNames() {
        return mongoClient.listDatabaseNames();
    }

    /**
     * @param clientSession
     * @return
     * @see com.mongodb.client.internal.MongoClientImpl#listDatabaseNames(com.mongodb.client.ClientSession)
     */
    @Override
    public MongoIterable<String> listDatabaseNames(ClientSession clientSession) {
        return mongoClient.listDatabaseNames(clientSession);
    }

    /**
     * @return
     * @see com.mongodb.client.internal.MongoClientImpl#listDatabases()
     */
    @Override
    public ListDatabasesIterable<Document> listDatabases() {
        return mongoClient.listDatabases();
    }

    /**
     * @param <T>
     * @param clazz
     * @return
     * @see com.mongodb.client.internal.MongoClientImpl#listDatabases(java.lang.Class)
     */
    @Override
    public <T> ListDatabasesIterable<T> listDatabases(Class<T> clazz) {
        return mongoClient.listDatabases(clazz);
    }

    /**
     * @param clientSession
     * @return
     * @see com.mongodb.client.internal.MongoClientImpl#listDatabases(com.mongodb.client.ClientSession)
     */
    @Override
    public ListDatabasesIterable<Document> listDatabases(ClientSession clientSession) {
        return mongoClient.listDatabases(clientSession);
    }

    /**
     * @param <T>
     * @param clientSession
     * @param clazz
     * @return
     * @see com.mongodb.client.internal.MongoClientImpl#listDatabases(com.mongodb.client.ClientSession,
     *      java.lang.Class)
     */
    @Override
    public <T> ListDatabasesIterable<T> listDatabases(ClientSession clientSession, Class<T> clazz) {
        return mongoClient.listDatabases(clientSession, clazz);
    }

    /**
     * @param obj
     * @return
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return mongoClient.equals(obj);
    }

    /**
     * @return
     * @see com.mongodb.client.internal.MongoClientImpl#startSession()
     */
    @Override
    public ClientSession startSession() {
        return mongoClient.startSession();
    }

    /**
     * @param options
     * @return
     * @see com.mongodb.client.internal.MongoClientImpl#startSession(com.mongodb.ClientSessionOptions)
     */
    @Override
    public ClientSession startSession(ClientSessionOptions options) {
        return mongoClient.startSession(options);
    }

    /**
     * 
     * @see com.mongodb.client.internal.MongoClientImpl#close()
     */
    @Override
    public void close() {
        mongoClient.close();
    }

    /**
     * @return
     * @see com.mongodb.client.internal.MongoClientImpl#watch()
     */
    @Override
    public ChangeStreamIterable<Document> watch() {
        return mongoClient.watch();
    }

    /**
     * @param <TResult>
     * @param resultClass
     * @return
     * @see com.mongodb.client.internal.MongoClientImpl#watch(java.lang.Class)
     */
    @Override
    public <TResult> ChangeStreamIterable<TResult> watch(Class<TResult> resultClass) {
        return mongoClient.watch(resultClass);
    }

    /**
     * @param pipeline
     * @return
     * @see com.mongodb.client.internal.MongoClientImpl#watch(java.util.List)
     */
    @Override
    public ChangeStreamIterable<Document> watch(List<? extends Bson> pipeline) {
        return mongoClient.watch(pipeline);
    }

    /**
     * @param <TResult>
     * @param pipeline
     * @param resultClass
     * @return
     * @see com.mongodb.client.internal.MongoClientImpl#watch(java.util.List, java.lang.Class)
     */
    @Override
    public <TResult> ChangeStreamIterable<TResult> watch(List<? extends Bson> pipeline, Class<TResult> resultClass) {
        return mongoClient.watch(pipeline, resultClass);
    }

    /**
     * @param clientSession
     * @return
     * @see com.mongodb.client.internal.MongoClientImpl#watch(com.mongodb.client.ClientSession)
     */
    @Override
    public ChangeStreamIterable<Document> watch(ClientSession clientSession) {
        return mongoClient.watch(clientSession);
    }

    /**
     * @param <TResult>
     * @param clientSession
     * @param resultClass
     * @return
     * @see com.mongodb.client.internal.MongoClientImpl#watch(com.mongodb.client.ClientSession,
     *      java.lang.Class)
     */
    @Override
    public <TResult> ChangeStreamIterable<TResult> watch(ClientSession clientSession, Class<TResult> resultClass) {
        return mongoClient.watch(clientSession, resultClass);
    }

    /**
     * @param clientSession
     * @param pipeline
     * @return
     * @see com.mongodb.client.internal.MongoClientImpl#watch(com.mongodb.client.ClientSession,
     *      java.util.List)
     */
    @Override
    public ChangeStreamIterable<Document> watch(ClientSession clientSession, List<? extends Bson> pipeline) {
        return mongoClient.watch(clientSession, pipeline);
    }

    /**
     * @param <TResult>
     * @param clientSession
     * @param pipeline
     * @param resultClass
     * @return
     * @see com.mongodb.client.internal.MongoClientImpl#watch(com.mongodb.client.ClientSession,
     *      java.util.List, java.lang.Class)
     */
    @Override
    public <TResult> ChangeStreamIterable<TResult> watch(ClientSession clientSession, List<? extends Bson> pipeline, Class<TResult> resultClass) {
        return mongoClient.watch(clientSession, pipeline, resultClass);
    }

    /**
     * @return
     * @see com.mongodb.client.internal.MongoClientImpl#getClusterDescription()
     */
    @Override
    public ClusterDescription getClusterDescription() {
        return mongoClient.getClusterDescription();
    }

    /**
     * @return
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return mongoClient.toString();
    }

}
