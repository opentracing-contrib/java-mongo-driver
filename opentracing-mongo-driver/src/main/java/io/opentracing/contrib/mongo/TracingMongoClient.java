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

import java.util.ArrayList;
import java.util.List;
import com.mongodb.*;
import io.opentracing.contrib.mongo.common.TracingCommandListener;

/**
 * Tracing Mongo Client
 *
 * @see MongoClient
 */
public class TracingMongoClient extends MongoClient {

    public TracingMongoClient(TracingCommandListener listener) {
        this(listener, new ServerAddress());
    }

    public TracingMongoClient(TracingCommandListener listener, final String host) {
        this(listener, new ServerAddress(host));
    }

    public TracingMongoClient(TracingCommandListener listener, final String host, final MongoClientOptions options) {
        this(listener, new ServerAddress(host), options);
    }

    public TracingMongoClient(TracingCommandListener listener, final String host, final int port) {
        this(listener, new ServerAddress(host, port));
    }

    public TracingMongoClient(TracingCommandListener listener, final ServerAddress addr) {
        this(listener, addr, new MongoClientOptions.Builder().build());
    }

    public TracingMongoClient(TracingCommandListener listener, final ServerAddress addr, final MongoCredential credentialsList) {
        this(listener, addr, credentialsList, new MongoClientOptions.Builder().build());
    }

    public TracingMongoClient(TracingCommandListener listener, final ServerAddress addr, final MongoClientOptions options) {
        super(addr, MongoClientOptions.builder(options).addCommandListener(listener).build());
    }

    public TracingMongoClient(TracingCommandListener listener, final ServerAddress addr, final MongoCredential credentialsList, final MongoClientOptions options) {
        super(addr, credentialsList, MongoClientOptions.builder(options).addCommandListener(listener).build());
    }


    public TracingMongoClient(TracingCommandListener listener, final List<ServerAddress> seeds) {
        this(listener, seeds, new MongoClientOptions.Builder().build());
    }

    public TracingMongoClient(TracingCommandListener listener, final List<ServerAddress> seeds, final MongoCredential credentialsList) {
        this(listener, seeds, credentialsList, new MongoClientOptions.Builder().build());
    }

    public TracingMongoClient(TracingCommandListener listener, final List<ServerAddress> seeds, final MongoClientOptions options) {
        super(seeds, MongoClientOptions.builder(options).addCommandListener(listener).build());
    }

    public TracingMongoClient(TracingCommandListener listener, final List<ServerAddress> seeds, final MongoCredential credentialsList, final MongoClientOptions options) {
        super(seeds, credentialsList, MongoClientOptions.builder(options).addCommandListener(listener).build());
    }


    public TracingMongoClient(TracingCommandListener listener, final MongoClientURI uri) {
        this(listener, uri, null);
    }

    public TracingMongoClient(TracingCommandListener listener, final List<ServerAddress> seeds, final MongoCredential credential, final MongoClientOptions options,
            final MongoDriverInformation mongoDriverInformation) {
        super(seeds, credential, MongoClientOptions.builder(options).addCommandListener(listener).build(), mongoDriverInformation);
    }

    public TracingMongoClient(TracingCommandListener listener, final MongoClientURI uri, final MongoDriverInformation mongoDriverInformation) {
        this(listener, toServerAddressList(uri.getHosts()), uri.getCredentials() != null ? uri.getCredentials() : null, uri.getOptions(), mongoDriverInformation);
    }

    public TracingMongoClient(TracingCommandListener listener, final ServerAddress addr, final MongoCredential credentialsList, final MongoClientOptions options,
            final MongoDriverInformation mongoDriverInformation) {
        super(addr, credentialsList, MongoClientOptions.builder(options).addCommandListener(listener).build(), mongoDriverInformation);
    }


    private static List<ServerAddress> toServerAddressList(List<String> hosts) {
        List<ServerAddress> list = new ArrayList<>();
        for (String host : hosts) {
            list.add(new ServerAddress(host));
        }
        return list;
    }

}
