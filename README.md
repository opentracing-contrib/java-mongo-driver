[![Build Status][ci-img]][ci] [![Released Version][maven-img]][maven]

# OpenTracing Mongo Driver Instrumentation
OpenTracing instrumentation for Mongo Driver.

## Installation

pom.xml
```xml
<dependency>
    <groupId>io.opentracing.contrib</groupId>
    <artifactId>opentracing-mongo-driver</artifactId>
    <version>0.0.3</version>
</dependency>
```

## Usage

```java
// Instantiate tracer
Tracer tracer = ...


// Instantiate Synchronous Tracing MongoClient
MongoClient mongoClient = new TracingMongoClient(tracer, ...);

// Instantiate Asynchronous Tracing MongoClient
MongoClient mongoClient = new TracingAsyncMongoClient(tracer, ...);

```

[ci-img]: https://travis-ci.org/opentracing-contrib/java-mongo-driver.svg?branch=master
[ci]: https://travis-ci.org/opentracing-contrib/java-mongo-driver
[maven-img]: https://img.shields.io/maven-central/v/io.opentracing.contrib/opentracing-mongo-driver.svg
[maven]: http://search.maven.org/#search%7Cga%7C1%7Copentracing-mongo-driver
