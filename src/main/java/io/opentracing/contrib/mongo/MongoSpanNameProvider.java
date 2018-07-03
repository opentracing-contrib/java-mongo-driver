/*
 * Copyright 2017-2018 The OpenTracing Authors
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

import java.util.function.Function;

public class MongoSpanNameProvider {

  private static final String NO_OPERATION = "unknown";

  public static Function<String, String> OPERATION_NAME = (operationName) -> ((operationName == null) ? NO_OPERATION : operationName);

  public static Function<String, String> PREFIX_OPERATION_NAME(final String prefix) {
    return (operationName) ->
        ((prefix == null) ? "" : prefix)
            + ((operationName == null) ? NO_OPERATION : operationName);
  }
}
