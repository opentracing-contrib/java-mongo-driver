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


import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.bson.BsonNull;
import org.bson.BsonString;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.event.CommandFailedEvent;
import com.mongodb.event.CommandStartedEvent;
import com.mongodb.event.CommandSucceededEvent;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import io.opentracing.Span;
import io.opentracing.contrib.mongo.common.ExcludedCommand;
import io.opentracing.contrib.mongo.common.SpanDecorator;
import io.opentracing.contrib.mongo.common.TracingCommandListener;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.tag.Tags;

public class MongoTest {

    private static final String FOO = "FOO";
    private static final String BAR = "BAR";
    private static final String BAZ = "BAZ";
    private static final MockTracer mockTracer = new MockTracer();
    private static final List<SpanDecorator> customDecorator = Collections.<SpanDecorator>singletonList(new SpanDecorator() {

        @Override
        public void commandStarted(CommandStartedEvent event, Span span) {
            span.setTag(FOO, FOO);
        }

        @Override
        public void commandSucceeded(CommandSucceededEvent event, Span span) {
            span.setTag(BAR, BAR);
        }

        @Override
        public void commandFailed(CommandFailedEvent event, Span span) {
            span.setTag(BAZ, BAZ);
        }

    });
    private MongodExecutable mongodExecutable;
    private IMongodConfig mongodConfig;

    @Before
    public void before() throws Exception {
        mockTracer.reset();

        MongodStarter starter = MongodStarter.getDefaultInstance();

        String bindIp = "127.0.0.1";
        int port = 12345;
        mongodConfig = new MongodConfigBuilder().version(Version.Main.PRODUCTION).net(new Net(bindIp, port, Network.localhostIsIPv6())).build();

        mongodExecutable = starter.prepare(mongodConfig);
        mongodExecutable.start();
    }

    @After
    public void after() {
        if (mongodExecutable != null) {
            mongodExecutable.stop();
        }
    }

    private String getConnectionString(ServerAddress address) {
        return "mongodb://" + address.toString();
    }

    @Test
    public void testExcludeInsert() throws Exception {
        List<ExcludedCommand> excludedCommands = new ArrayList<>();
        ExcludedCommand excludedCommand = new ExcludedCommand();
        excludedCommand.put("insert", new BsonString("testCol"));
        excludedCommands.add(excludedCommand);


        MongoClient mongoClient = new TracingMongoClient(new TracingCommandListener.Builder(mockTracer).withExcludedCommands(excludedCommands).build(),
                getConnectionString(new ServerAddress(mongodConfig.net().getServerAddress(), mongodConfig.net().getPort())), "test-api");

        MongoDatabase db = mongoClient.getDatabase("test");
        MongoCollection<Document> col = db.getCollection("testCol");
        col.insertOne(new Document("testDoc", new Date()));
        mongoClient.close();

        List<MockSpan> finished = mockTracer.finishedSpans();
        assertEquals(0, finished.size());

        assertNull(mockTracer.activeSpan());
    }

    @Test
    public void testExcludeAll() throws Exception {
        List<ExcludedCommand> excludedCommands = new ArrayList<>();

        ExcludedCommand excludedCommand = new ExcludedCommand();
        excludedCommand.put("getMore", BsonNull.VALUE);
        excludedCommand.put("collection", new BsonString("testCol"));
        excludedCommands.add(new ExcludedCommand("insert", new BsonString("testCol")));
        excludedCommands.add(new ExcludedCommand("find", BsonNull.VALUE));
        excludedCommands.add(excludedCommand);

        MongoClient mongoClient = new TracingMongoClient(new TracingCommandListener.Builder(mockTracer).withExcludedCommands(excludedCommands).build(),
                getConnectionString(new ServerAddress(mongodConfig.net().getServerAddress(), mongodConfig.net().getPort())), "test-api");
        MongoDatabase db = mongoClient.getDatabase("test");
        MongoCollection<Document> col = db.getCollection("testCol");

        for (int i = 0; i < 100_000; i++) {
            col.insertOne(new Document("testDoc", new Date()));
        }
        List<String> jsons = new ArrayList<>();
        final MongoCursor<Document> cursor = col.find().iterator();
        while (cursor.hasNext()) {
            jsons.add(cursor.next().toJson());
        }
        assertEquals(100_000, jsons.size());

        mongoClient.close();

        List<MockSpan> finished = mockTracer.finishedSpans();
        assertEquals(0, finished.size());

        assertNull(mockTracer.activeSpan());
    }

    @Test
    public void testSuccessDecorator() throws Exception {
        MongoClient mongoClient = new TracingMongoClient(new TracingCommandListener.Builder(mockTracer).withSpanDecorators(customDecorator).build(),
                getConnectionString(new ServerAddress(mongodConfig.net().getServerAddress(), mongodConfig.net().getPort())), "test-api");
        MongoDatabase db = mongoClient.getDatabase("test");
        MongoCollection<Document> col = db.getCollection("testCol");

        col.insertOne(new Document("testDoc", new Date()));

        mongoClient.close();

        List<MockSpan> finished = mockTracer.finishedSpans();
        assertEquals(1, finished.size());
        MockSpan span = finished.iterator().next();

        assertEquals(FOO, span.tags().get(FOO));
        assertEquals(BAR, span.tags().get(BAR));
        assertNull(span.tags().get(BAZ));

        assertNull(mockTracer.activeSpan());
    }

    @Test
    public void testFailureDecorator() throws Exception {
        MongoClient mongoClient = new TracingMongoClient(new TracingCommandListener.Builder(mockTracer).withSpanDecorators(customDecorator).build(),
                getConnectionString(new ServerAddress(mongodConfig.net().getServerAddress(), mongodConfig.net().getPort())), "test-api");
        MongoDatabase db = mongoClient.getDatabase("test");

        try {
            db.runCommand(new Document()); // this is not a valid command
            fail();
        } catch (RuntimeException ignored) {
        }

        mongoClient.close();

        List<MockSpan> finished = mockTracer.finishedSpans();
        assertEquals(1, finished.size());
        MockSpan span = finished.iterator().next();

        assertEquals(FOO, span.tags().get(FOO));
        assertNull(span.tags().get(BAR));
        assertEquals(BAZ, span.tags().get(BAZ));

        assertNull(mockTracer.activeSpan());
    }

    @Test
    public void sync() throws Exception {
        MongoClient mongoClient = new TracingMongoClient(new TracingCommandListener.Builder(mockTracer).build(),
                getConnectionString(new ServerAddress(mongodConfig.net().getServerAddress(), mongodConfig.net().getPort())), "test-api");

        MongoDatabase db = mongoClient.getDatabase("test");
        MongoCollection<Document> col = db.getCollection("testCol");
        col.insertOne(new Document("testDoc", new Date()));

        mongoClient.close();

        List<MockSpan> finished = mockTracer.finishedSpans();
        assertEquals(1, finished.size());

        checkSpans(finished);
        assertNull(mockTracer.activeSpan());
    }

    private void checkSpans(List<MockSpan> mockSpans) {
        for (MockSpan mockSpan : mockSpans) {
            String operationName = mockSpan.operationName();
            assertEquals("insert", operationName);
            assertEquals(Tags.SPAN_KIND_CLIENT, mockSpan.tags().get(Tags.SPAN_KIND.getKey()));
            assertEquals(TracingCommandListener.COMPONENT_NAME, mockSpan.tags().get(Tags.COMPONENT.getKey()));
            assertNotNull(mockSpan.tags().get(Tags.DB_STATEMENT.getKey()));
            assertEquals("mongo", mockSpan.tags().get(Tags.DB_TYPE.getKey()));
            assertEquals("test", mockSpan.tags().get(Tags.DB_INSTANCE.getKey()));
            assertEquals(0, mockSpan.generatedErrors().size());

            assertNotNull(mockSpan.tags().get(Tags.DB_STATEMENT.getKey()));
            assertNotNull(mockSpan.tags().get(Tags.PEER_HOSTNAME.getKey()));
        }
    }

}
