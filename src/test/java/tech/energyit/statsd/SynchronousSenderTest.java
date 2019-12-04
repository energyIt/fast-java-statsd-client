package tech.energyit.statsd;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SynchronousSenderTest {

    @Mock
    private StatsDClientErrorHandler errorHandler;
    @Mock
    private InetSocketAddress socketAddress;
    @Mock
    private DatagramChannel datagramChannel;

    private SynchronousSender sender;

    @Before
    public void setup() throws IOException {
        sender = newSender();
        verify(datagramChannel).connect(same(socketAddress));
    }

    private SynchronousSender newSender() {
        return SynchronousSender.builder()
                .blockingChannel(false)
                .withSocketSupplier(() -> datagramChannel)
                .withAddressLookup(() -> socketAddress)
                .withErrorHandler(errorHandler)
                .build();
    }

    @Test
    public void sendShouldWriteToChannel() throws IOException {
        ByteBuffer msgAsBuffer = ByteBuffer.wrap("test-message-1".getBytes());
        when(datagramChannel.write(same(msgAsBuffer))).thenReturn(msgAsBuffer.limit());
        sender.send(msgAsBuffer);

        verify(datagramChannel).write(same(msgAsBuffer));
    }

    @Test
    public void closeShouldCloseChannel() throws IOException {
        sender.close();
        verify(datagramChannel).close();
    }

    @Test
    public void failingCloseMustInvokeErrorHandler() throws IOException {
        doThrow(IOException.class).when(datagramChannel).close();
        sender.close();
        verify(errorHandler).handle(any(IOException.class));
    }

    @Test
    public void ifChannelIgnoresSomeBytesErrorHandlerMustBeNotified() throws IOException {
        ByteBuffer msgAsBuffer = ByteBuffer.wrap("test-message-1".getBytes());
        when(datagramChannel.write(same(msgAsBuffer))).thenReturn(1);
        sender.send(msgAsBuffer);

        verify(datagramChannel).write(same(msgAsBuffer));
        verify(errorHandler).handle(anyString(), any());
    }

    @Test
    public void ifWritingToChannelThrowsErrorErrorHandlerMustBeNotified() throws IOException {
        ByteBuffer msgAsBuffer = ByteBuffer.wrap("test-message-1".getBytes());
        when(datagramChannel.write(same(msgAsBuffer))).thenThrow(new IOException("some io error"));
        sender.send(msgAsBuffer);

        verify(datagramChannel).write(same(msgAsBuffer));
        verify(errorHandler).handle(any(IOException.class));
    }

    @Test(expected = IllegalStateException.class)
    public void ifConnectingToChannelThrowsErrorExcecptionMustBeThrown() throws IOException {
        when(datagramChannel.connect(same(socketAddress))).thenThrow(new IOException("some io error"));
        newSender();
    }
}
