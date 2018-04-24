package com.energyit.statsd;

/**
 * Describes a client connection to a StatsD server, which may be used to post metrics
 * in the form of counters, timers, and gauges.
 *
 * <p>See the spec : https://github.com/b/statsd_spec</p>
 *
 * @author Milos Gregor
 */
// TODO : put other methods from statsd spec
public interface StatsDClient {

    /**
     * Records the latest fixed value for the specified named gauge.
     *
     * @param aspect
     *     the name of the gauge
     * @param value
     *     the new reading of the gauge
     */
    void gauge(byte[] aspect, long value, Tag... tags);

    /**
     * Adjusts the specified counter by a given delta.
     *
     * @param aspect
     *     the name of the counter to adjust
     * @param delta
     *     the amount to adjust the counter by
     * @param tags
     *     array of tags to be added to the data
     */
    void count(byte[] aspect, long delta, Tag... tags);

    /**
     * Adjusts the specified counter by a given delta.
     *
     * @param aspect
     *     the name of the counter to adjust
     * @param delta
     *     the amount to adjust the counter by
     * @param sampleRate
     * 		percentage of time metric to be sent
     * @param tags
     *     array of tags to be added to the data
     */
    void count(byte[] aspect, long delta, double sampleRate, Tag... tags);

    /**
     * Records an execution time in milliseconds for the specified named operation.
     *
     * @param aspect
     *     the name of the timed operation
     * @param timeInMs
     *     the time in milliseconds
     * @param tags
     *     array of tags to be added to the data
     */
    void time(byte[] aspect, long timeInMs, Tag... tags);

}
