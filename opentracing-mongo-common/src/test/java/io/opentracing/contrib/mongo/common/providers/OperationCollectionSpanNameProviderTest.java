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
package io.opentracing.contrib.mongo.common.providers;

import static org.junit.Assert.assertEquals;

import com.mongodb.ServerAddress;
import com.mongodb.connection.ClusterId;
import com.mongodb.connection.ConnectionDescription;
import com.mongodb.connection.ServerId;
import com.mongodb.event.CommandStartedEvent;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.junit.Test;

public class OperationCollectionSpanNameProviderTest {

  private final MongoSpanNameProvider provider = new OperationCollectionSpanNameProvider();

  private final CommandStartedEvent TEST_EVENT = new CommandStartedEvent(1,
      new ConnectionDescription(new ServerId(new ClusterId(), new ServerAddress())),
      "database-name", "insert",
      new BsonDocument().append("insert", new BsonString("collection-name")));

  @Test
  public void testOperationNameExists() {
    assertEquals("insert collection-name", provider.generateName(TEST_EVENT));
  }

  @Test
  public void testNullOperationName() {
    assertEquals("unknown", provider.generateName(null));
  }
}