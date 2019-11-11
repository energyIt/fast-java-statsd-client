package tech.energyit.statsd.async;

import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import tech.energyit.statsd.FastStatsDClient;
import tech.energyit.statsd.StatsDClientErrorHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AsynchronousSenderTest {

    @Mock
    private StatsDClientErrorHandler errorHandler;
    @Mock
    private InetSocketAddress socketAddress;
    @Mock
    private DatagramChannel datagramChannel;

    private AsynchronousSender sender;

    @Before
    public void setup() throws IOException {
        sender = AsynchronousSender.builder()
                .withSocketSupplier(() -> datagramChannel)
                .withAddressLookup(() -> socketAddress)
                .withErrorHandler(errorHandler)
                .withRingbufferSize(4)
                .publishSynchronouslyWhenRingbufferIsFull()
                .build();
        verify(datagramChannel).connect(same(socketAddress));
    }

    @After
    public void teardown() {
        sender.close();
    }

    @Test
    public void sendShouldWriteTheMessageToChannel() throws IOException {
        String stringMessage = "test-message-1";
        ByteBuffer msgAsBuffer = ByteBuffer.wrap(stringMessage.getBytes());

        sender.send(msgAsBuffer);

        //wait until msg is read
        Awaitility.await().atMost(Duration.FIVE_HUNDRED_MILLISECONDS).until(() -> msgAsBuffer.position() == msgAsBuffer.limit());
        ArgumentCaptor<ByteBuffer> sentMsg = ArgumentCaptor.forClass(ByteBuffer.class);
        verify(datagramChannel).write(sentMsg.capture());
        byte[] sentMessage = new byte[stringMessage.length()];
        ((ByteBuffer) sentMsg.getValue().flip()).get(sentMessage);

        Assertions.assertThat(new String(sentMessage)).isEqualTo(stringMessage);
    }

    @Test
    public void sendingMoreMessageThanRingbufferSizeShouldNotBlockAnything() throws IOException {
        String stringMessage = "test-message-1";
        ByteBuffer msgAsBuffer = ByteBuffer.wrap(stringMessage.getBytes());

        final int batchSize = 100;
        for (int i = 0; i < batchSize; i++) {
            msgAsBuffer.flip();
            sender.send(msgAsBuffer);
        }

        //wait until last msg is read
        Awaitility.await().atMost(Duration.FIVE_HUNDRED_MILLISECONDS).until(() -> msgAsBuffer.position() == msgAsBuffer.limit());
        verify(datagramChannel, times(batchSize)).write(any(ByteBuffer.class));
    }

    @Test
    public void messageBiggerThanDefaultBufferShouldBeWrittenToChannel() throws IOException {
        byte[] msg = new byte[FastStatsDClient.INITIAL_BUFFER_SIZE + 100];
        for (int i = 0; i < msg.length; i++) {
            msg[0] = (byte) i;
        }
        ByteBuffer msgAsBuffer = ByteBuffer.wrap(msg);
        sender.send(msgAsBuffer);

        //wait until msg is read
        Awaitility.await().atMost(Duration.FIVE_HUNDRED_MILLISECONDS).until(() -> msgAsBuffer.position() == msgAsBuffer.limit());
        ArgumentCaptor<ByteBuffer> sentMsg = ArgumentCaptor.forClass(ByteBuffer.class);
        verify(datagramChannel).write(sentMsg.capture());
        byte[] sentMessage = new byte[msg.length];
        ((ByteBuffer) sentMsg.getValue().flip()).get(sentMessage);

        Assertions.assertThat(sentMessage).isEqualTo(msg);
    }

    @Test
    public void skippingMethodShouldStillWriteAtLeastAllEventsFromRingBuffer() throws IOException {
        int ringbufferSize = 16;
        sender = AsynchronousSender.builder()
                .withHostAndPort("localhost", 8125)
                .withSocketSupplier(() -> datagramChannel)
                .withErrorHandler(errorHandler)
                .withRingbufferSize(ringbufferSize)
                .skipMessageWhenRingbufferIsFull()
                .build();

        String stringMessage = "test-message-1";
        ByteBuffer msgAsBuffer = ByteBuffer.wrap(stringMessage.getBytes());

        int publishedMessages = ringbufferSize + 3;
        for (int i = 0; i < publishedMessages; i++) {
            msgAsBuffer.flip();
            sender.send(msgAsBuffer);
        }

        //wait until last msg is read
        Awaitility.await().atMost(Duration.FIVE_HUNDRED_MILLISECONDS).until(() -> msgAsBuffer.position() == msgAsBuffer.limit());
        verify(datagramChannel, atLeast(ringbufferSize)).write(any(ByteBuffer.class));
    }

}
