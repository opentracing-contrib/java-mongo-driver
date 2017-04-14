package io.opentracing.contrib.mongo;


import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDriverInformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tracing Mongo Client
 *
 * @see MongoClient
 */
public class TracingMongoClient extends MongoClient {

    public TracingMongoClient() {
        this(new ServerAddress());
    }

    public TracingMongoClient(final String host) {
        this(new ServerAddress(host));
    }

    public TracingMongoClient(final String host, final MongoClientOptions options) {
        this(new ServerAddress(host), options);
    }

    public TracingMongoClient(final String host, final int port) {
        this(new ServerAddress(host, port));
    }

    public TracingMongoClient(final ServerAddress addr) {
        this(addr, new MongoClientOptions.Builder().build());
    }

    public TracingMongoClient(final ServerAddress addr, final List<MongoCredential> credentialsList) {
        this(addr, credentialsList, new MongoClientOptions.Builder().build());
    }

    public TracingMongoClient(final ServerAddress addr, final MongoClientOptions options) {
        super(addr, MongoClientOptions.builder(options).addCommandListener(new TracingCommandListener()).build());
    }

    public TracingMongoClient(final ServerAddress addr, final List<MongoCredential> credentialsList, final MongoClientOptions options) {
        super(addr, credentialsList, MongoClientOptions.builder(options).addCommandListener(new TracingCommandListener()).build());
    }

    public TracingMongoClient(final List<ServerAddress> seeds) {
        this(seeds, new MongoClientOptions.Builder().build());
    }

    public TracingMongoClient(final List<ServerAddress> seeds, final List<MongoCredential> credentialsList) {
        this(seeds, credentialsList, new MongoClientOptions.Builder().build());
    }

    public TracingMongoClient(final List<ServerAddress> seeds, final MongoClientOptions options) {
        super(seeds, MongoClientOptions.builder(options).addCommandListener(new TracingCommandListener()).build());
    }

    public TracingMongoClient(final List<ServerAddress> seeds, final List<MongoCredential> credentialsList, final MongoClientOptions options) {
        super(seeds, credentialsList, MongoClientOptions.builder(options).addCommandListener(new TracingCommandListener()).build());
    }

    public TracingMongoClient(final MongoClientURI uri) {
        this(uri, null);
    }

    public TracingMongoClient(final MongoClientURI uri, final MongoDriverInformation mongoDriverInformation) {
        this(toServerAddressList(uri.getHosts()),
                uri.getCredentials() != null ? Collections.singletonList(uri.getCredentials()) : Collections.<MongoCredential>emptyList(),
                MongoClientOptions.builder().addCommandListener(new TracingCommandListener()).build(),
                mongoDriverInformation);
    }

    public TracingMongoClient(final ServerAddress addr, final List<MongoCredential> credentialsList, final MongoClientOptions options,
                              final MongoDriverInformation mongoDriverInformation) {
        super(addr, credentialsList, MongoClientOptions.builder(options).addCommandListener(new TracingCommandListener()).build(), mongoDriverInformation);
    }

    public TracingMongoClient(final List<ServerAddress> seeds, final List<MongoCredential> credentialsList, final MongoClientOptions options,
                              final MongoDriverInformation mongoDriverInformation) {
        super(seeds, credentialsList, MongoClientOptions.builder(options).addCommandListener(new TracingCommandListener()).build(), mongoDriverInformation);
    }

    private static List<ServerAddress> toServerAddressList(List<String> hosts) {
        List<ServerAddress> list = new ArrayList<>();
        for (String host : hosts) {
            list.add(new ServerAddress(host));
        }
        return list;
    }
}
