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

import com.mongodb.event.CommandStartedEvent;
import org.bson.BsonDocument;

public class OperationCollectionSpanNameProvider extends NoopSpanNameProvider {

  @Override
  public String generateName(CommandStartedEvent event) {
    if (event == null || event.getCommand() == null) {
      return NO_OPERATION;
    }
    final BsonDocument cmd = event.getCommand();
    String col = cmd.getString(cmd.getFirstKey()).getValue();
    return super.generateName(event) + " " + col;
  }
}
