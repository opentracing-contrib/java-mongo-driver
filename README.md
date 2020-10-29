[![Build Status][ci-img]][ci] [![Coverage Status][cov-img]][cov] [![Released Version][maven-img]][maven] [![Apache-2.0 license](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

# OpenTracing Mongo Driver Instrumentation
OpenTracing instrumentation for Mongo Driver.

## Installation

### Mongo Driver

pom.xml
```xml
<dependency>
    <groupId>io.opentracing.contrib</groupId>
    <artifactId>opentracing-mongo-driver</artifactId>
    <version>VERSION</version>
</dependency>
```

### Mongo Driver Reactive Streams

pom.xml
```xml
<dependency>
    <groupId>io.opentracing.contrib</groupId>
    <artifactId>opentracing-mongo-driver-reactivestreams</artifactId>
    <version>VERSION</version>
</dependency>
```

## Usage

```java
// Instantiate tracer
Tracer tracer = ...

// Optionally register tracer with GlobalTracer
GlobalTracer.register(tracer);
``` 

There are 2 ways to instrument `MongoClient`:
- using Mongo Tracing Client
- using `MongoClientSettings.Builder` with `TracingCommandListener`

### Mongo Tracing Client 

```java
// Instantiate TracingCommandListener
TracingCommandListener listener = new TracingCommandListener.Builder(tracer).build()

// Instantiate Synchronous Tracing MongoClient
MongoClient mongoClient = new TracingMongoClient(listener, ...);

// Instantiate Asynchronous Tracing MongoClient
MongoClient mongoClient = new TracingAsyncMongoClient(listener, ...);

```

### `MongoClientSettings.Builder` with `TracingCommandListener`
```java
// Instantiate TracingCommandListener
TracingCommandListener listener = new TracingCommandListener.Builder(tracer).build()

// Add TracingCommandListener to MongoClientSettings.Builder
MongoClient mongoClient = MongoClients.create(
        MongoClientSettings.builder()
                .addCommandListener(listener)
                ...
                .build());

```

### Mongo Span Name
By default, span names are set to the operation performed by the Mongo client. To customize the span name, provide a MongoSpanNameProvider to the client that alters the span name. If a provider is not provided, the span name will remain the default.

```java

// Create TracingCommandListener with custom span name provider
TracingCommandListener listener = new TracingCommandListener.Builder(tracer)\
    .withSpanNameProvider(new PrefixSpanNameProvider("mongo."))
    .build();

// Create TracingMongoClient
TracingMongoClient client = new TracingMongoClient(
    listener, 
    replicaSetAddresses, 
    credentials, 
    clientOptions 
    );
Document doc = new Document();
client.getDatabase("db").getCollection("collection).insertOne(doc);
// Span name is now set to "mongo.insert"
```

### Exclude commands from tracing
To exclude specific Mongo commands from tracing add `ExcludedCommand` to `TracingCommandListener`:
```java
List<ExcludedCommand> excludedCommands = new ArrayList<>();
ExcludedCommand excludedCommand = new ExcludedCommand();
excludedCommand.put("getMore",  BsonNull.VALUE);
excludedCommand.put("collection",  new BsonString("oplog.rs"));

excludedCommands.add(excludedCommand);

    
TracingCommandListener listener =  new TracingCommandListener.Builder(tracer)
    .withExcludedCommands(excludedCommands).build();    
    
```

## License

[Apache 2.0 License](./LICENSE).

[ci-img]: https://travis-ci.org/opentracing-contrib/java-mongo-driver.svg?branch=master
[ci]: https://travis-ci.org/opentracing-contrib/java-mongo-driver
[cov-img]: https://coveralls.io/repos/github/opentracing-contrib/java-mongo-driver/badge.svg?branch=master
[cov]: https://coveralls.io/github/opentracing-contrib/java-mongo-driver?branch=master
[maven-img]: https://img.shields.io/maven-central/v/io.opentracing.contrib/opentracing-mongo-driver.svg
[maven]: http://search.maven.org/#search%7Cga%7C1%7Copentracing-mongo-driver
