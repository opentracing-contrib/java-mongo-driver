package io.opentracing.contrib.mongo;


import com.mongodb.async.client.ListDatabasesIterable;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClientSettings;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoDatabase;
import com.mongodb.async.client.MongoIterable;
import com.mongodb.client.MongoDriverInformation;
import io.opentracing.Tracer;
import org.bson.Document;


/**
 * Tracing decorator for {@link MongoClient}
 */
public class TracingAsyncMongoClient implements MongoClient {

  private final MongoClient mongoClient;
  private final TracingCommandListener tracingCommandListener;


  public TracingAsyncMongoClient(final Tracer tracer, final MongoClientSettings settings) {
    this(tracer, settings, null);
  }

  public TracingAsyncMongoClient(final Tracer tracer, final MongoClientSettings settings,
      final MongoDriverInformation mongoDriverInformation) {
    this.tracingCommandListener = new TracingCommandListener(tracer);
    this.mongoClient = MongoClients.create(MongoClientSettings.builder(settings)
            .addCommandListener(tracingCommandListener)
            .build(),
        mongoDriverInformation);
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
  public ListDatabasesIterable<Document> listDatabases() {
    return mongoClient.listDatabases();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <TResult> ListDatabasesIterable<TResult> listDatabases(Class<TResult> tResultClass) {
    return mongoClient.listDatabases(tResultClass);
  }
}
