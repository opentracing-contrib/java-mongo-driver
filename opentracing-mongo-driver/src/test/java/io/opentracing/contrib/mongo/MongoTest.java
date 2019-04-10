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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
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
import java.util.Date;
import java.util.List;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
    mongodConfig = new MongodConfigBuilder()
        .version(Version.Main.PRODUCTION)
        .net(new Net(bindIp, port, Network.localhostIsIPv6()))
        .build();

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
  public void sync() throws Exception {
    MongoClient mongoClient = new TracingMongoClient(
        mockTracer,
        new ServerAddress(mongodConfig.net().getServerAddress(), mongodConfig.net().getPort()));

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
      assertEquals(TracingCommandListener.COMPONENT_NAME,
          mockSpan.tags().get(Tags.COMPONENT.getKey()));
      assertNotNull(mockSpan.tags().get(Tags.DB_STATEMENT.getKey()));
      assertEquals("mongo", mockSpan.tags().get(Tags.DB_TYPE.getKey()));
      assertEquals("test", mockSpan.tags().get(Tags.DB_INSTANCE.getKey()));
      assertEquals(0, mockSpan.generatedErrors().size());

      assertNotNull(mockSpan.tags().get(Tags.DB_STATEMENT.getKey()));
      assertNotNull(mockSpan.tags().get(Tags.PEER_HOSTNAME.getKey()));
    }
  }

}
