package tech.energyit.statsd;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * {@link StatsDClient} implementation
 * uses thread-local {@link ByteBuffer} for efficient message format.
 * For long values it has ZERO allocations.
 *
 * Important note : Maximal (total) message size can only be {@value MAX_BUFFER_LENGTH}B,
 */
public final class FastStatsDClient implements StatsDClient {

    public static final Charset MESSAGE_CHARSET = StandardCharsets.UTF_8;
    public static final int INITIAL_BUFFER_SIZE = 128;
    public static final int MAX_BUFFER_LENGTH = 1024 * 1024;

    static final double NO_SAMPLE_RATE = 1.0;

    private static final ThreadLocal<ByteBuffer> MSG_BUFFER = ThreadLocal.withInitial(() -> createByteBuffer(INITIAL_BUFFER_SIZE));

    private final byte[] prefix;
    private final Sender sender;
    private final boolean exactDoubles;

    public FastStatsDClient(Sender sender) {
        this(null, sender);
    }

    public FastStatsDClient(final String prefix, final Sender sender) {
        this(prefix, sender, false);
    }

    public FastStatsDClient(final String prefix, final Sender sender, boolean exactDoubles) {
        if ((prefix != null) && (!prefix.isEmpty())) {
            this.prefix = (prefix + '.').getBytes(MESSAGE_CHARSET);
        } else {
            this.prefix = new byte[0];
        }
        this.sender = sender;
        this.exactDoubles = exactDoubles;
    }


    @Override
    public void count(final byte[] aspect, final long delta, final Tag... tags) {
        send(aspect, delta, MetricType.COUNTER, NO_SAMPLE_RATE, tags);
    }


    @Override
    public void count(final byte[] aspect, final long delta, final double sampleRate, final Tag... tags) {
        if (isInvalidSample(sampleRate)) {
            return;
        }
        send(aspect, delta, MetricType.COUNTER, sampleRate, tags);
    }


    @Override
    public void count(final byte[] aspect, final double delta, final Tag... tags) {
        send(aspect, delta, MetricType.COUNTER, NO_SAMPLE_RATE, tags);
    }


    @Override
    public void count(final byte[] aspect, final double delta, final double sampleRate, final Tag... tags) {
        if (isInvalidSample(sampleRate)) {
            return;
        }
        send(aspect, delta, MetricType.COUNTER, sampleRate, tags);
    }

    @Override
    public void gauge(final byte[] aspect, final long value, final Tag... tags) {
        send(aspect, value, MetricType.GAUGE, NO_SAMPLE_RATE, tags);
    }


    @Override
    public void gauge(byte[] aspect, long value, double sampleRate, Tag... tags) {
        if (isInvalidSample(sampleRate)) {
            return;
        }
        send(aspect, value, MetricType.GAUGE, sampleRate, tags);
    }


    @Override
    public void gauge(byte[] aspect, double value, Tag... tags) {
        send(aspect, value, MetricType.GAUGE, NO_SAMPLE_RATE, tags);
    }


    @Override
    public void gauge(byte[] aspect, double value, double sampleRate, Tag... tags) {
        if (isInvalidSample(sampleRate)) {
            return;
        }
        send(aspect, value, MetricType.GAUGE, sampleRate, tags);
    }


    @Override
    public void time(final byte[] aspect, final long timeInMs, final Tag... tags) {
        send(aspect, timeInMs, MetricType.TIMER, NO_SAMPLE_RATE, tags);
    }

    @Override
    public void time(byte[] aspect, long timeInMs, double sampleRate, Tag... tags) {
        if (isInvalidSample(sampleRate)) {
            return;
        }
        send(aspect, timeInMs, MetricType.TIMER, sampleRate, tags);
    }

    @Override
    public void histogram(byte[] aspect, long value, Tag... tags) {
        send(aspect, value, MetricType.HISTOGRAM, NO_SAMPLE_RATE, tags);
    }

    @Override
    public void histogram(byte[] aspect, long value, double sampleRate, Tag... tags) {
        if (isInvalidSample(sampleRate)) {
            return;
        }
        send(aspect, value, MetricType.HISTOGRAM, sampleRate, tags);
    }

    @Override
    public void histogram(byte[] aspect, double value, Tag... tags) {
        send(aspect, value, MetricType.HISTOGRAM, NO_SAMPLE_RATE, tags);
    }

    @Override
    public void histogram(byte[] aspect, double value, double sampleRate, Tag... tags) {
        if (isInvalidSample(sampleRate)) {
            return;
        }
        send(aspect, value, MetricType.HISTOGRAM, sampleRate, tags);
    }

    @Override
    public void set(byte[] aspect, long value, Tag... tags) {
        send(aspect, value, MetricType.SET, NO_SAMPLE_RATE, tags);
    }

    @Override
    public void set(byte[] aspect, long value, double sampleRate, Tag... tags) {
        if (isInvalidSample(sampleRate)) {
            return;
        }
        send(aspect, value, MetricType.SET, sampleRate, tags);
    }

    @Override
    public void set(byte[] aspect, double value, Tag... tags) {
        send(aspect, value, MetricType.SET, NO_SAMPLE_RATE, tags);
    }

    @Override
    public void set(byte[] aspect, double value, double sampleRate, Tag... tags) {
        if (isInvalidSample(sampleRate)) {
            return;
        }
        send(aspect, value, MetricType.SET, sampleRate, tags);
    }

    @Override
    public void meter(byte[] aspect, long value, Tag... tags) {
        send(aspect, value, MetricType.METER, NO_SAMPLE_RATE, tags);
    }

    @Override
    public void meter(byte[] aspect, double value, Tag... tags) {
        send(aspect, value, MetricType.METER, NO_SAMPLE_RATE, tags);
    }

    public void clear() {
        MSG_BUFFER.remove();
    }

    /**
     * format and send with long value.
     *
     * @throws IllegalArgumentException if the message is too large
     */
    private void send(byte[] metricName, long value, MetricType metricType, double sampleRate, Tag[] tags) {
        ByteBuffer buffer = MSG_BUFFER.get();
        boolean formatted = false;
        while (!formatted) {
            try {
                buffer.clear();
                putPrefix(metricName, buffer);
                putLong(buffer, value);
                putSuffix(buffer, metricType, sampleRate, tags);
                buffer.flip();
                formatted = true;
            } catch (BufferOverflowException e) {
                // bigger messages are exceptional so using Exceptions should be good enough
                buffer = createByteBuffer(newCapacity(buffer.capacity()));
                MSG_BUFFER.set(buffer);
            }
        }
        sender.send(buffer);
    }

    /**
     * format and send with double value.
     * @throws IllegalArgumentException if the message is too large
     */
    private void send(byte[] metricName, double value, MetricType metricType, double sampleRate, Tag[] tags) {
        ByteBuffer buffer = MSG_BUFFER.get();
        boolean formatted = false;
        while (!formatted) {
            try {
                buffer.clear();
                putPrefix(metricName, buffer);
                putDouble(buffer, value, exactDoubles);
                putSuffix(buffer, metricType, sampleRate, tags);
                buffer.flip();
                formatted = true;
            } catch (BufferOverflowException e) {
                // bigger messages are exceptional so using Exceptions should be good enough
                buffer = createByteBuffer(newCapacity(buffer.capacity()));
                MSG_BUFFER.set(buffer);
            }
        }
        sender.send(buffer);
    }

    private void putPrefix(byte[] metricName, ByteBuffer buffer) {
        buffer.put(prefix);
        buffer.put(metricName);
        buffer.put((byte) ':');
    }

    private void putSuffix(ByteBuffer buffer, MetricType metricType, double sampleRate, Tag[] tags) {
        buffer.put((byte) '|');
        buffer.put(metricType.key);
        if (sampleRate != NO_SAMPLE_RATE) {
            buffer.put((byte) '|');
            buffer.put((byte) '@');
            putDouble(buffer, sampleRate, exactDoubles);
        }
        if (tags != null && tags.length > 0) {
            buffer.put((byte) '|');
            buffer.put((byte) '#');
            for (int i = 0; i < tags.length; i++) {
                Tag tag = tags[i];
                buffer.put(tag.getName());
                buffer.put((byte) ':');
                buffer.put(tag.getValue());
                if (i < tags.length - 1) {
                    buffer.put((byte) ',');
                }
            }

        }
    }

    private static int newCapacity(final int currentCapacity) {
        if (currentCapacity >= MAX_BUFFER_LENGTH) {
            throw new IllegalArgumentException("Message too big. This is maximum : " + MAX_BUFFER_LENGTH);
        }
        long value = 2L * currentCapacity;
        if (value >= MAX_BUFFER_LENGTH) {
            value = MAX_BUFFER_LENGTH;
        }
        return (int) value;
    }


    private static void putLong(ByteBuffer bb, long v) {
        Numbers.putLongAsAsciiBytes(v, bb);
    }

    private static void putDouble(ByteBuffer bb, double v, boolean exactDoubles) {
        Numbers.putDoubleAsAsciiBytes(v, bb, MESSAGE_CHARSET, exactDoubles);
    }

    private static boolean isInvalidSample(double sampleRate) {
        return sampleRate <= 0 || sampleRate > 1;
    }

    private static ByteBuffer createByteBuffer(int capacity) {
        return ByteBuffer.allocateDirect(capacity);
    }

    enum MetricType {
        GAUGE("g"), TIMER("ms"), COUNTER("c"), HISTOGRAM("h"), SET("s"), METER("m");

        MetricType(String key) {
            this.key = key.getBytes(MESSAGE_CHARSET);
        }

        private final byte[] key;
    }

}
