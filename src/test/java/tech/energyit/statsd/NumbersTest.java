package tech.energyit.statsd;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@RunWith(MockitoJUnitRunner.class)
public class NumbersTest {

    @Test
    public void positiveLongsArePut() {
        assertThatLongIsEncoded(1000L, "1000");
    }

    @Test
    public void negativeLongsArePut() {
        assertThatLongIsEncoded(-1000L, "-1000");
    }

    @Test
    public void positiveDoubleWithNoFraction() {
        assertThatDoubleIsEncoded(100.00, "100.0", false);
        assertThatDoubleIsEncoded(100.00, "100.0", true);
    }

    @Test
    public void negativeDoubleWithNoFraction() {
        assertThatDoubleIsEncoded(-100.00, "-100.0", false);
        assertThatDoubleIsEncoded(-100.00, "-100.0", true);
    }

    @Test
    public void positiveDoubleWithZerosBeforeFractionDigits() {

        assertThatDoubleIsEncoded(100.001, "100.001", false);
        assertThatDoubleIsEncoded(100.001, "100.001", true);
    }

    @Test
    public void negativeDoubleWithZerosBeforeFractionDigits() {
        assertThatDoubleIsEncoded(-100.001, "-100.001", false);
        assertThatDoubleIsEncoded(-100.001, "-100.001", true);
    }

    @Test
    public void positiveDoubleZerosAfterFractionDigitsAreIgnored() {
        assertThatDoubleIsEncoded(100.00100, "100.001", false);
        assertThatDoubleIsEncoded(100.00100, "100.001", true);
    }

    @Test
    public void negativeDoubleZerosAfterFractionDigitsAreIgnored() {
        assertThatDoubleIsEncoded(-100.00100, "-100.001", false);
        assertThatDoubleIsEncoded(-100.00100, "-100.001", true);
    }

    @Test
    public void positiveDoubleScientificLiteral() {
        assertThatDoubleIsEncoded(1e3, "1000.0", false);
        assertThatDoubleIsEncoded(0.0001e3, "0.1", false);

        assertThatDoubleIsEncoded(1e3, "1000.0", true);
        assertThatDoubleIsEncoded(0.0001e3, "0.1", true);
    }

    @Test
    public void positiveDoubleScientificLiteralNegativeExponent() {
        assertThatDoubleIsEncoded(10000.1e-3, "10.0001", false);
        assertThatDoubleIsEncoded(1e-3, "0.001", false);

        assertThatDoubleIsEncoded(10000.1e-3, "10.0001", true);
        assertThatDoubleIsEncoded(1e-3, "0.001", true);
    }

    @Test
    public void negativeDoubleScientificLiteral() {
        assertThatDoubleIsEncoded(-1e3, "-1000.0", false);
        assertThatDoubleIsEncoded(-0.0001e3, "-0.1", false);

        assertThatDoubleIsEncoded(-1e3, "-1000.0", true);
        assertThatDoubleIsEncoded(-0.0001e3, "-0.1", true);
    }


    @Test
    public void negativeDoubleScientificLiteralNegativeExponent() {
        assertThatDoubleIsEncoded(-1234.5e-3, "-1.2345", false);
        assertThatDoubleIsEncoded(-1.2e-3, "-0.0012", false);

        assertThatDoubleIsEncoded(-1234.5e-3, "-1.2345", true);
        assertThatDoubleIsEncoded(-1.2e-3, "-0.0012", true);
    }

    private void assertThatLongIsEncoded(long value, String expected) {
        ByteBuffer bb = ByteBuffer.allocate(expected.length());
        Numbers.putLongAsAsciiBytes(value, bb);
        byte[] msg = new byte[expected.length()];
        ((ByteBuffer) bb.flip()).get(msg);
        Assertions.assertThat(new String(msg)).isEqualTo(expected);
    }


    private void assertThatDoubleIsEncoded(double value, String expected, boolean exactDouble) {
        ByteBuffer bb = ByteBuffer.allocate(expected.length());
        Numbers.putDoubleAsAsciiBytes(value, bb, StandardCharsets.UTF_8, exactDouble);
        byte[] msg = new byte[expected.length()];
        ((ByteBuffer) bb.flip()).get(msg);
        Assertions.assertThat(new String(msg)).isEqualTo(expected);
    }

}
