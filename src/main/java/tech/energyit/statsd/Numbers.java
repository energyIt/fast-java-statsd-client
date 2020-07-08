package tech.energyit.statsd;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * utilities to work with numbers.
 *
 * @author Milos Gregor
 */
final class Numbers {

    private static final int ROUNDING_MULTIPLIER = 1000000000;

    private Numbers() { /* never to be called */}

    private static final char[] DigitTens = {
            '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
            '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
            '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
            '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
            '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
            '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
            '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
            '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
            '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
            '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
    };

    private static final char[] DigitOnes = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    };

    private static final char[] digits = {
            '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b',
            'c', 'd', 'e', 'f', 'g', 'h',
            'i', 'j', 'k', 'l', 'm', 'n',
            'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z'
    };

    private static int stringSize(long x) {
        long p = 10;
        for (int i = 1; i < 19; i++) {
            if (x < p)
                return i;
            p = 10 * p;
        }
        return 19;
    }

    /**
     * Inspired by protected method Long.getChars().
     *
     * @param i   - long to put to buffer
     * @param buf - buffer to be written to
     */
    static void putLongAsAsciiBytes(long i, ByteBuffer buf) {
        int numberOfChars = (i < 0) ? stringSize(-i) + 1 : stringSize(i);
        buf.position(buf.position() + numberOfChars);

        long q;
        int r;
        int charPos = buf.position();
        char sign = 0;

        if (i < 0) {
            sign = '-';
            i = -i;
        }

        // Get 2 digits/iteration using longs until quotient fits into an int
        while (i > Integer.MAX_VALUE) {
            q = i / 100;
            // really: r = i - (q * 100)
            r = (int) (i - ((q << 6) + (q << 5) + (q << 2)));
            i = q;
            buf.put(--charPos, (byte) DigitOnes[r]);
            buf.put(--charPos, (byte) DigitTens[r]);
        }

        // Get 2 digits/iteration using ints
        int q2;
        int i2 = (int) i;
        while (i2 >= 65536) {
            q2 = i2 / 100;
            // really: r = i2 - (q * 100)
            r = i2 - ((q2 << 6) + (q2 << 5) + (q2 << 2));
            i2 = q2;
            buf.put(--charPos, (byte) DigitOnes[r]);
            buf.put(--charPos, (byte) DigitTens[r]);
        }

        // Fall thru to fast mode for smaller numbers
        // assert(i2 <= 65536, i2)
        for (; ; ) {
            q2 = (i2 * 52429) >>> (16 + 3);
            r = i2 - ((q2 << 3) + (q2 << 1));  // r = i2-(q2*10) ...
            buf.put(--charPos, (byte) digits[r]);
            i2 = q2;
            if (i2 == 0)
                break;
        }
        if (sign != 0) {
            buf.put(--charPos, (byte) sign);
        }
    }

    /**
     * @param v              double to be put to the buffer encoded as ascii bytes
     * @param bb             buffer to put to
     * @param messageCharset - charset to be used for exact encoding
     * @param exact          if exact is false, it is much faster and doubles are rounded only up to 9 decimal places
     */
    public static void putDoubleAsAsciiBytes(double v, ByteBuffer bb, Charset messageCharset, boolean exact) {
        if (exact || v >= Long.MAX_VALUE || v <= Long.MIN_VALUE) {
            bb.put(String.valueOf(v).getBytes(messageCharset));
        } else {
            long digits = (long) v;
            if (digits == 0L && v < 0) {
                bb.put((byte) '-');
            }
            putLongAsAsciiBytes(digits, bb);
            bb.put((byte) '.');
            long fraction = Math.round(Math.abs(v - digits) * ROUNDING_MULTIPLIER);
            if (fraction > 0) {
                // put x zeros before fraction digits:
                long shift = 10;
                while (fraction * shift < ROUNDING_MULTIPLIER) {
                    shift *= 10;
                    bb.put((byte) '0');
                }
                // avoid zeros at the end of fraction:
                long divisor = 10;
                while (fraction % divisor == 0) {
                    divisor *= 10;
                }
                putLongAsAsciiBytes(fraction / (divisor / 10), bb);
            } else {
                putLongAsAsciiBytes(0L, bb);
            }
        }

    }

}
