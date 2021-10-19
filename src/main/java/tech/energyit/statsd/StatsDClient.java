package tech.energyit.statsd;

/**
 * Describes a client connection to a StatsD server, which may be used to post metrics
 * in the form of counters, timers, gauges, histograms or sets..
 *
 * <p>See the spec : https://github.com/statsd/statsd/blob/master/docs/metric_types.md</p>
 *
 * @author Milos Gregor
 */
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
     * Records the delta value for the specified named gauge.
     *
     * @param aspect
     *     the name of the gauge
     * @param deltaSign
     *     either '+' or '-' to indicate either increase or decrease gauge value
     * @param value
     *     the (positive!) change of value
     */
    void gauge(byte[] aspect, char deltaSign, long value, Tag... tags);

    /**
     * Records the latest fixed value for the specified named gauge.
     *
     * @param aspect     the name of the gauge
     * @param value      the new reading of the gauge
     * @param sampleRate percentage of time metric to be sent
     * @param tags       array of tags to be added to the data
     */
    void gauge(byte[] aspect, long value, double sampleRate, Tag... tags);

    /**
     * Records the latest fixed value for the specified named gauge.
     *
     * @param aspect the name of the gauge
     * @param value  the new reading of the gauge
     */
    void gauge(byte[] aspect, double value, Tag... tags);

    /**
     * Records the delta value for the specified named gauge.
     *
     * @param aspect
     *     the name of the gauge
     * @param deltaSign
     *     either '+' or '-' to indicate either increase or decrease gauge value
     * @param value
     *     the (positive!) change of value
     */
    void gauge(byte[] aspect, char deltaSign, double value, Tag... tags);

    /**
     * Records the latest fixed value for the specified named gauge.
     *
     * @param aspect     the name of the gauge
     * @param value      the new reading of the gauge
     * @param sampleRate percentage of time metric to be sent
     * @param tags       array of tags to be added to the data
     */
    void gauge(byte[] aspect, double value, double sampleRate, Tag... tags);

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
     * Adjusts the specified counter by a given delta.
     *
     * @param aspect
     *     the name of the counter to adjust
     * @param delta
     *     the amount to adjust the counter by
     * @param tags
     *     array of tags to be added to the data
     */
    void count(byte[] aspect, double delta, Tag... tags);

    /**
     * Adjusts the specified counter by a given delta.
     *
     * @param aspect     the name of the counter to adjust
     * @param delta      the amount to adjust the counter by
     * @param sampleRate percentage of time metric to be sent
     * @param tags       array of tags to be added to the data
     */
    void count(byte[] aspect, double delta, double sampleRate, Tag... tags);

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

    /**
     * Records an execution time in milliseconds for the specified named operation.
     *
     * @param aspect
     *     the name of the timed operation
     * @param timeInMs
     *     the time in milliseconds
     * @param sampleRate
     *      percentage of time metric to be sent
     * @param tags
     *     array of tags to be added to the data
     */
    void time(byte[] aspect, long timeInMs, double sampleRate, Tag... tags);

    /**
     * Records a value for the specified named histogram.
     *
     * @param aspect the name of the histogram
     * @param value  the value to be incorporated in the histogram
     * @param tags   array of tags to be added to the data
     */
    void histogram(byte[] aspect, long value, Tag... tags);

    /**
     * Records a value for the specified named histogram.
     *
     * @param aspect     the name of the histogram
     * @param value      the value to be incorporated in the histogram
     * @param sampleRate percentage of time metric to be sent
     * @param tags       array of tags to be added to the data
     */
    void histogram(byte[] aspect, long value, double sampleRate, Tag... tags);

    /**
     * Records a value for the specified named histogram.
     *
     * @param aspect the name of the histogram
     * @param value  the value to be incorporated in the histogram
     * @param tags   array of tags to be added to the data
     */
    void histogram(byte[] aspect, double value, Tag... tags);

    /**
     * Records a value for the specified named histogram.
     *
     * @param aspect     the name of the histogram
     * @param value      the value to be incorporated in the histogram
     * @param sampleRate percentage of time metric to be sent
     * @param tags       array of tags to be added to the data
     */
    void histogram(byte[] aspect, double value, double sampleRate, Tag... tags);

    /**
     * Records a value for the specified named distribution.
     *
     * @param aspect the name of the distribution
     * @param value  the value to be incorporated in the distribution
     * @param tags   array of tags to be added to the data
     */
    void set(byte[] aspect, long value, Tag... tags);

    /**
     * Records a value for the specified named set.
     *
     * @param aspect     the name of the set
     * @param value      the value to be incorporated in the set
     * @param sampleRate percentage of time metric to be sent
     * @param tags       array of tags to be added to the data
     */
    void set(byte[] aspect, long value, double sampleRate, Tag... tags);

    /**
     * Records a value for the specified named set.
     *
     * @param aspect the name of the set
     * @param value  the value to be incorporated in the set
     * @param tags   array of tags to be added to the data
     */
    void set(byte[] aspect, double value, Tag... tags);

    /**
     * Records a value for the specified named set.
     *
     * @param aspect     the name of the set
     * @param value      the value to be incorporated in the set
     * @param sampleRate percentage of time metric to be sent
     * @param tags       array of tags to be added to the data
     */
    void set(byte[] aspect, double value, double sampleRate, Tag... tags);

    /**
     * Records a value for the specified named meter.
     *
     * @param aspect the name of the meter
     * @param value  the value to be incorporated in the meter
     * @param tags   array of tags to be added to the data
     */
    void meter(byte[] aspect, long value, Tag... tags);

    /**
     * Records a value for the specified named meter.
     *
     * @param aspect the name of the meter
     * @param value  the value to be incorporated in the meter
     * @param tags   array of tags to be added to the data
     */
    void meter(byte[] aspect, double value, Tag... tags);
}
