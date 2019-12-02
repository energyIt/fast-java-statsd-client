# Fast Java Statsd Client

[![Build Status](https://travis-ci.org/energyIt/fast-java-statsd-client.svg?branch=master)](https://travis-ci.org/energyIt/fast-java-statsd-client)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=tech.energyit%3Afast-java-statsd-client&metric=alert_status)](https://sonarcloud.io/dashboard?id=tech.energyit%3Afast-java-statsd-client)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=tech.energyit%3Afast-java-statsd-client&metric=coverage)](https://sonarcloud.io/dashboard?id=tech.energyit%3Afast-java-statsd-client)

A statsd client library implemented in Java.  Allows Java applications to easily communicate via statsd and reduce the the allocaiton or performance overhead to ZERO.

This version was inspired by [java-dogstatsd-client](https://github.com/indeedeng/java-dogstatsd-client) but our major requirement was to reduce allocations and the performance impact on the running jvm.

## Allocation Rate Comparison
```
Benchmark                                                                                    Mode  Cnt     Score   Error   Units
 StatsdClientBenchmark.countWithTwoTagsViaSyncFastClient                                     avgt          3.015           us/op
 StatsdClientBenchmark.countWithTwoTagsViaSyncFastClient:·gc.alloc.rate                      avgt         ≈ 10⁻³          MB/sec
 StatsdClientBenchmark.countWithTwoTagsViaSyncFastClient:·gc.alloc.rate.norm                 avgt          0.002            B/op

 StatsdClientBenchmark.countWithTwoTagsViaAsyncFastClient                                    avgt          0.116           us/op
 StatsdClientBenchmark.countWithTwoTagsViaAsyncFastClient:·gc.alloc.rate                     avgt          0.080          MB/sec
 StatsdClientBenchmark.countWithTwoTagsViaAsyncFastClient:·gc.alloc.rate.norm                avgt          0.011            B/op
 
 StatsdClientBenchmark.countWithTwoTagsViaAsyncFastClient2                                   avgt          5.969           us/op
 StatsdClientBenchmark.countWithTwoTagsViaAsyncFastClient2:·gc.alloc.rate                    avgt          0.008          MB/sec
 StatsdClientBenchmark.countWithTwoTagsViaAsyncFastClient2:·gc.alloc.rate.norm               avgt          0.055            B/op

 StatsdClientBenchmark.countWithTwoTagsViaDatadogClient                                      avgt          1.477           us/op
 StatsdClientBenchmark.countWithTwoTagsViaDatadogClient:·gc.alloc.rate                       avgt        339.016          MB/sec
 StatsdClientBenchmark.countWithTwoTagsViaDatadogClient:·gc.alloc.rate.norm                  avgt        639.998            B/op
```
See `StatsdClientBenchmark`

NOTE: if no statsd server is listening on the ports, DatagramChannels generate IOExceptions which is expensive. If the clients uses a ErrorHandler that also generates stacktrace, the impact is big.
