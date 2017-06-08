package io.opentracing.contrib.mongo;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.MongoClientSettings;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.connection.ClusterSettings;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.tag.Tags;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MongoTest {

  private static final MockTracer mockTracer = new MockTracer(MockTracer.Propagator.TEXT_MAP);
  private MongodExecutable mongodExecutable;
  private IMongodConfig mongodConfig;

  @Before
  public void before() throws Exception {
    mockTracer.reset();

    Command command = Command.MongoD;

    //IDirectory artifactStorePath = new FixedPath("test");
    //ITempNaming executableNaming = new UUIDTempNaming();

    IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
        .defaults(command)
                /*.artifactStore(new ExtractedArtifactStoreBuilder()
                        .defaults(command)
                        .download(new DownloadConfigBuilder()
                                .defaultsForCommand(command)
                                .artifactStorePath(artifactStorePath))
                        .executableNaming(executableNaming))*/
        .build();

    mongodConfig = new MongodConfigBuilder()
        .version(Version.Main.PRODUCTION)
        .build();

    MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);

    mongodExecutable = runtime.prepare(mongodConfig);
    mongodExecutable.start();
  }

  @After
  public void after() {
    if (mongodExecutable != null) {
      mongodExecutable.stop();
    }
  }

  @Test
  public void async() throws Exception {
    ClusterSettings clusterSettings = ClusterSettings.builder().hosts(Collections.singletonList(
        new ServerAddress(mongodConfig.net().getServerAddress(), mongodConfig.net().getPort())))
        .build();
    MongoClientSettings settings = MongoClientSettings.builder().clusterSettings(clusterSettings)
        .build();

    com.mongodb.async.client.MongoClient mongoClient = new TracingAsyncMongoClient(mockTracer, settings);

    com.mongodb.async.client.MongoDatabase database = mongoClient.getDatabase("test");
    com.mongodb.async.client.MongoCollection<Document> collection = database.getCollection("test");

    final CountDownLatch latch = new CountDownLatch(2);

    //collection.
    collection.insertOne(new Document("testDoc", new Date()), new SingleResultCallback<Void>() {
      @Override
      public void onResult(Void result, Throwable t) {
        latch.countDown();
      }
    });

    collection.insertOne(new Document("testDoc", new Date()), new SingleResultCallback<Void>() {
      @Override
      public void onResult(Void result, Throwable t) {
        latch.countDown();
      }
    });

    assertTrue(latch.await(30, TimeUnit.SECONDS));

    List<MockSpan> finished = mockTracer.finishedSpans();
    assertEquals(2, finished.size());

    checkSpans(finished);
    assertNull(mockTracer.activeSpan());
  }

  @Test
  public void sync() throws Exception {
    MongoClient mongo = new TracingMongoClient(
        mockTracer,
        new ServerAddress(mongodConfig.net().getServerAddress(), mongodConfig.net().getPort()));

    MongoDatabase db = mongo.getDatabase("test");
    MongoCollection<Document> col = db.getCollection("testCol");
    col.insertOne(new Document("testDoc", new Date()));

    List<MockSpan> finished = mockTracer.finishedSpans();
    assertEquals(1, finished.size());

    checkSpans(finished);
    assertNull(mockTracer.activeSpan());
  }

  private void checkSpans(List<MockSpan> mockSpans) {
    for (MockSpan mockSpan : mockSpans) {
      String operationName = mockSpan.operationName();
      assertTrue(operationName.equals("insert"));
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
