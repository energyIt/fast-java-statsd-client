# Fast Java Statsd Client

[![Build Status](https://travis-ci.org/energyIt/fast-java-statsd-client.svg?branch=master)](https://travis-ci.org/energyIt/fast-java-statsd-client)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=tech.energyit%3Afast-java-statsd-client&metric=alert_status)](https://sonarcloud.io/dashboard?id=tech.energyit%3Afast-java-statsd-client)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=tech.energyit%3Afast-java-statsd-client&metric=coverage)](https://sonarcloud.io/dashboard?id=tech.energyit%3Afast-java-statsd-client)

A statsd client library implemented in Java.  Allows Java applications to easily communicate via statsd and reduce the the allocaiton or performance overhead to ZERO.

This version was inspired by [java-dogstatsd-client](https://github.com/indeedeng/java-dogstatsd-client) but our major requirement was to reduce allocations and the performance impact on the running jvm.

## Allocation Rate Comparison
```
Benchmark                                                          Mode  Cnt     Score     Error   Units
countWithTagViaDatadogClient                                       avgt    5     1.641 ±   0.452   us/op
countWithTagViaDatadogClient:·gc.alloc.rate.norm                   avgt    5  2288.080 ± 551.177    B/op
countWithTagViaFastClient                                          avgt    5     3.050 ±   0.435   us/op
countWithTagViaFastClient:·gc.alloc.rate.norm                      avgt    5    24.000 ±   0.002    B/op
countWithTwoTagsViaDatadogClient                                   avgt    5     1.641 ±   0.519   us/op
countWithTwoTagsViaDatadogClient:·gc.alloc.rate                    avgt    5  1368.544 ± 688.381  MB/sec
countWithTwoTagsViaFastClient                                      avgt    5     3.184 ±   0.689   us/op
countWithTwoTagsViaFastClient:·gc.alloc.rate.norm                  avgt    5    24.001 ±   0.002    B/op
```
See `StatsdClientBenchmark`

NOTE: if no statsd server is listening on the ports, DatagramChannels generate IOExceptions which is expensive. If the clients uses a ErrorHandler that also generates stacktrace, the impact is big.
