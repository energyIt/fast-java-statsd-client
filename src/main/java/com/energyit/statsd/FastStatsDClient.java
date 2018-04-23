package com.energyit.statsd;

import java.io.Closeable;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.concurrent.ThreadLocalRandom;


/**
 *
 */
public final class FastStatsDClient implements StatsDClient, Closeable {


    private Buffer msgBuffer = ByteBuffer.allocateDirect(1024);
    private final byte[] prefix;
    private final Tag[] constantTagsRendered;

    public FastStatsDClient(final String prefix, Tag[] constantTags) {
        if ((prefix != null) && (!prefix.isEmpty())) {
            this.prefix = (prefix + '.').getBytes();
        } else {
            this.prefix = new byte[0];
        }
        constantTagsRendered = constantTags;

    }

    /**
     * Cleanly shut down this StatsD client. This method may throw an exception if
     * the socket cannot be closed.
     */
    @Override
    public void close() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void count(final byte[] aspect, final long delta, final double sampleRate, final Tag... tags) {
        if (isInvalidSample(sampleRate)) {
            return;
        }
        send(String.format("%s%s:%d|c|@%f%s", prefix, aspect, delta, sampleRate, tagString(tags)));
    }

    private void formatMessage(
            final ByteBuffer buffer, final byte[] metric, final MetricType metricType,
            final long value, final double sampleRate, final Tag... tags) {
        buffer.clear();
        buffer.put(prefix);
        buffer.putChar('.');
        buffer.put(metric);
        buffer.putChar(':');
        buffer.putLong(value);
        buffer.putChar('|');
        buffer.put(metricType.key);
        buffer.putChar('|');
        if (sampleRate != 1) {
            buffer.putChar('@');
            buffer.putDouble(sampleRate);
        }
        if (tags != null && tags.length > 0) {
            buffer.putChar('|');
            buffer.putChar('#');
            for (int i = 0; i < tags.length; i++) {
                Tag tag = tags[i];
                buffer.put(tag.getName());
                buffer.putChar(':');
                buffer.put(tag.getValue());
                if (i < tags.length - 1) {
                    buffer.putChar(',');
                }
            }


        }
        buffer.flip();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void gauge(final byte[] aspect, final long value, final Tag... tags) {

        send(String.format("%s%s:%d|g|%s", prefix, aspect, value, tagString(tags)));
    }

    private String tagString(Tag[] tags) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void time(final byte[] aspect, final long timeInMs, final Tag... tags) {
        send(String.format("%s%s:%d|ms|%s", prefix, aspect, timeInMs, tagString(tags)));
    }

    private void send(final String message) {
//        queue.offer(message);
    }

    private boolean isInvalidSample(double sampleRate) {
        return sampleRate != 1 && ThreadLocalRandom.current().nextDouble() > sampleRate;
    }

    enum MetricType {
        GAUGE("g"), TIMER("ms"), COUNTER("c");

        private MetricType(String key) {
            this.key = key.getBytes();
        }

        private byte[] key;
    }

}
