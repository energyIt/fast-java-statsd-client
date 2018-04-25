# Fast Java Statsd Client

[![Build Status](https://travis-ci.org/energyIt/fast-java-statsd-client.svg?branch=master)](https://travis-ci.org/energyIt/fast-java-statsd-client)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=com.energyIt.statsd%3Afast-java-statsd-client&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.energyIt.statsd%3Afast-java-statsd-client)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=com.energyIt.statsd%3Afast-java-statsd-client&metric=coverage)](https://sonarcloud.io/dashboard?id=com.energyIt.statsd%3Afast-java-statsd-client)

A statsd client library implemented in Java.  Allows for Java applications to easily communicate with statsd.

This version was inspired by [java-dogstatsd-client](https://github.com/indeedeng/java-dogstatsd-client) but our major requirement was to reduce allocations and the performance impact on the running jvm.
