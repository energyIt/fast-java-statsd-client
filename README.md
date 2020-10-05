# Fast Java Statsd Client

[![Build Status](https://travis-ci.org/energyIt/fast-java-statsd-client.svg?branch=master)](https://travis-ci.org/energyIt/fast-java-statsd-client)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=tech.energyit%3Afast-java-statsd-client&metric=alert_status)](https://sonarcloud.io/dashboard?id=tech.energyit%3Afast-java-statsd-client)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=tech.energyit%3Afast-java-statsd-client&metric=coverage)](https://sonarcloud.io/dashboard?id=tech.energyit%3Afast-java-statsd-client)

A Statsd client library implemented in Java. 
Allows Java applications to be easily and efficiently monitored by publishing metrics via statsd protocol.
The client implements all metric types defined by [the statsd spec](https://github.com/b/statsd_spec) and few extra to support some extensions like DataDog.

Our main goal was to be fast and do no allocations, and indeed, the library literally produces [zero garbage in runtime](#How-Fast-Is-It).

## Installation

The client jar is distributed via Maven central, and can be imported like this:
```xml
<dependency>
    <groupId>tech.energyit</groupId>
    <artifactId>fast-java-statsd-client</artifactId>
    <version>1.0</version>
</dependency>
```

or for Gradle :
```
dependencies {
        implementation "tech.energyit:fast-java-statsd-client:1.0"
}
```

## Usage

```java
// create a sender - provides lot's of options how to send data via UDP, but this is likely the best for you:
SynchronousSender sender = SynchronousSender.builder()
                          .withHostAndPort(STATSD_SERVER_HOST, STATSD_SERVER_PORT)
                          .build();
// create client and set prefix
StatsDClient client = new FastStatsDClient("tradeApp", sender);

// monitor your app
client.count(ORDER_COUNT, count, productTag);
client.time(PROCESSING_TIME, durationInMs, productTag);
// ...  

// close the sender with your app
sender.close();
```

For more details see [SampleMonitorApp.java](./src/test/java/tech/energyit/statsd/samples/SampleMonitorApp.java)

## How Fast Is It ?
You best find for yourself. To make it easy for you, we prepared [StatsdClientBenchmark.java](./src/test/java/tech/energyit/statsd/jmh/StatsdClientBenchmark.java) 
which compares different setups of [FastStatsDClient.java](./src/main/java/tech/energyit/statsd/FastStatsDClient.java) and also with the most used java client from DataDog.
Just make sure you have some statsd server listening on the port (e.g. by starting  [DummyStatsDServer.java](./src/test/java/tech/energyit/statsd/utils/DummyStatsDServer.java) )

If no statsd server is listening on the port, DatagramChannels generate IOExceptions which is expensive. 
If the client uses an ErrorHandler that also generates a stacktrace, the impact can be big, and the benched results will be misleading.

This is the result running with openjdk version "11.0.7":
```
Benchmark                                                                                       Mode  Cnt     Score   Error   Units
StatsdClientBenchmark.countLongsViaSyncFastClient                                               avgt          3.078           us/op
StatsdClientBenchmark.countLongsViaSyncFastClient:·gc.alloc.rate.norm                           avgt          ≈ 10⁻³            B/op
StatsdClientBenchmark.countLongsViaSyncFastClient:·gc.count                                     avgt            ≈ 0          counts

StatsdClientBenchmark.countDoublesViaSyncFastClient                                             avgt          3.083           us/op
StatsdClientBenchmark.countDoublesViaSyncFastClient:·gc.alloc.rate.norm                         avgt          ≈ 10⁻³            B/op
StatsdClientBenchmark.countDoublesViaSyncFastClient:·gc.count                                   avgt            ≈ 0          counts

StatsdClientBenchmark.countDoublesViaSyncFastClientUsingExactDoubles                            avgt          3.503           us/op
StatsdClientBenchmark.countDoublesViaSyncFastClientUsingExactDoubles:·gc.alloc.rate.norm        avgt         80.009            B/op
StatsdClientBenchmark.countDoublesViaSyncFastClientUsingExactDoubles:·gc.count                  avgt          1.000          counts

StatsdClientBenchmark.countLongsViaAsyncFastClient                                              avgt          0.119           us/op
StatsdClientBenchmark.countLongsViaAsyncFastClient:·gc.alloc.rate.norm                          avgt          0.029            B/op
StatsdClientBenchmark.countLongsViaAsyncFastClient:·gc.count                                    avgt          7.000          counts

StatsdClientBenchmark.countLongsViaAsyncFastClientWithFallbackToSyncSender                      avgt          5.674           us/op
StatsdClientBenchmark.countLongsViaAsyncFastClientWithFallbackToSyncSender:·gc.alloc.rate.norm  avgt          2.191            B/op
StatsdClientBenchmark.countLongsViaAsyncFastClientWithFallbackToSyncSender:·gc.count            avgt            ≈ 0          counts

StatsdClientBenchmark.countLongsViaDatadogClient                                                avgt          0.559           us/op
StatsdClientBenchmark.countLongsViaDatadogClient:·gc.alloc.rate.norm                            avgt        400.000            B/op
StatsdClientBenchmark.countLongsViaDatadogClient:·gc.count                                      avgt         25.000          counts
```
