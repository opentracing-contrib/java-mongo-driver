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


import static org.junit.Assert.*;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import io.opentracing.contrib.mongo.common.TracingCommandListener;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.tag.Tags;

public class MongoTest {

    private static final MockTracer mockTracer = new MockTracer();
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

    @Test
    public void reactiveStreams() throws Exception {
        MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(new ConnectionString("mongodb://localhost:" + mongodConfig.net().getPort())).build();

        MongoClient mongoClient = new TracingReactiveStreamsMongoClient(mockTracer, settings);

        MongoDatabase database = mongoClient.getDatabase("test");
        MongoCollection<Document> collection = database.getCollection("test");

        final CountDownLatch latch = new CountDownLatch(2);
        // collection
        collection.insertOne(new Document("testDoc", new Date())).subscribe(new Subscriber<InsertOneResult>() {

            @Override
            public void onSubscribe(final Subscription s) {
                s.request(1); // <--- Data requested and the insertion will now occur
            }

            @Override
            public void onError(final Throwable t) {}

            @Override
            public void onComplete() {
                latch.countDown();
            }

            @Override
            public void onNext(InsertOneResult t) {}

        });

        collection.insertOne(new Document("testDoc", new Date())).subscribe(new Subscriber<InsertOneResult>() {

            @Override
            public void onSubscribe(final Subscription s) {
                s.request(1); // <--- Data requested and the insertion will now occur
            }

            @Override
            public void onError(final Throwable t) {}

            @Override
            public void onComplete() {
                latch.countDown();
            }

            @Override
            public void onNext(InsertOneResult t) {}

        });

        assertTrue(latch.await(30, TimeUnit.SECONDS));
        mongoClient.close();

        List<MockSpan> finished = mockTracer.finishedSpans();
        assertEquals(2, finished.size());

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
