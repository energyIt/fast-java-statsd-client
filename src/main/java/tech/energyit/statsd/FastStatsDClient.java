package tech.energyit.statsd;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Important note : Maximal (total) message size can only be {@value MAX_BUFFER_LENGTH}B,
 */
public final class FastStatsDClient implements StatsDClient {

    public static final Charset MESSAGE_CHARSET = Charset.forName("UTF-8");
    public static final int INITIAL_BUFFER_SIZE = 128;
    public static final int MAX_BUFFER_LENGTH = 1024 * 1024;

    static final double NO_SAMPLE_RATE = 1.0;

    private static final ThreadLocal<ByteBuffer> MSG_BUFFER = ThreadLocal.withInitial(() -> createByteBuffer(INITIAL_BUFFER_SIZE));

    private final byte[] prefix;
    private final Sender sender;

    public FastStatsDClient(Sender sender) {
        this(null, sender);
    }

    public FastStatsDClient(final String prefix, final Sender sender) {
        if ((prefix != null) && (!prefix.isEmpty())) {
            this.prefix = (prefix + '.').getBytes(MESSAGE_CHARSET);
        } else {
            this.prefix = new byte[0];
        }
        this.sender = sender;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void count(final byte[] aspect, final long delta, final Tag... tags) {
        send(aspect, delta, MetricType.COUNTER, NO_SAMPLE_RATE, tags);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void count(final byte[] aspect, final long delta, final double sampleRate, final Tag... tags) {
        if (isInvalidSample(sampleRate)) {
            return;
        }
        send(aspect, delta, MetricType.COUNTER, sampleRate, tags);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void gauge(final byte[] aspect, final long value, final Tag... tags) {
        send(aspect, value, MetricType.GAUGE, NO_SAMPLE_RATE, tags);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void time(final byte[] aspect, final long timeInMs, final Tag... tags) {
        send(aspect, timeInMs, MetricType.TIMER, NO_SAMPLE_RATE, tags);
    }

    /**
     * @throws IllegalArgumentException if the message is too large
     */
    private void send(byte[] aspect, long value, MetricType metricType, double sampleRate, Tag[] tags) {
        ByteBuffer buffer = MSG_BUFFER.get();
        boolean formatted = false;
        while(!formatted) {
            try {
                formatMessage(buffer, prefix, aspect, metricType, value, sampleRate, tags);
                formatted = true;
            } catch (BufferOverflowException e) {
                // bigger messages are exceptional so using Exceptions should be good enough
                buffer = createByteBuffer(newCapacity(buffer.capacity()));
                MSG_BUFFER.set(buffer);
            }
        }
        sender.send(buffer);
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

    private static void formatMessage(
            final ByteBuffer buffer, final byte[] prefix, final byte[] metric, final MetricType metricType,
            final long value, final double sampleRate, final Tag... tags) {
        buffer.clear();
        buffer.put(prefix);
        buffer.put(metric);
        buffer.put((byte) ':');
        putLong(buffer, value);
        buffer.put((byte) '|');
        buffer.put(metricType.key);
        if (sampleRate != NO_SAMPLE_RATE) {
            buffer.put((byte) '|');
            buffer.put((byte) '@');
            putDouble(buffer, sampleRate);
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
        buffer.flip();
    }

    private static void putLong(ByteBuffer bb, long v) {
        Numbers.putLongAsAsciiBytes(v, bb);
    }

    private static void putDouble(ByteBuffer bb, double v) {
        bb.put(String.valueOf(v).getBytes(MESSAGE_CHARSET));
    }

    private static boolean isInvalidSample(double sampleRate) {
        return sampleRate <= 0 || sampleRate > 1;
    }

    private static ByteBuffer createByteBuffer(int capacity) {
        return ByteBuffer.allocateDirect(capacity);
    }

    enum MetricType {
        GAUGE("g"), TIMER("ms"), COUNTER("c");

        MetricType(String key) {
            this.key = key.getBytes(MESSAGE_CHARSET);
        }

        private final byte[] key;
    }

}
