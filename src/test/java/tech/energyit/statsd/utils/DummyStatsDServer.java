package tech.energyit.statsd.utils;

import tech.energyit.statsd.FastStatsDClient;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class DummyStatsDServer implements Closeable {

    public static final int STATSD_SERVER_PORT = 17254;

    private final List<String> messagesReceived = new ArrayList<>();
    private final DatagramSocket server;

    public DummyStatsDServer(int port) throws SocketException {
        this.server = new DatagramSocket(port);
        start(this::consumePackets);
    }

    public DummyStatsDServer(int port, Consumer<DatagramPacket> packetConsumer) throws SocketException {
        this.server = new DatagramSocket(port);
        start(packetConsumer);
    }

    private void start(Consumer<DatagramPacket> packetConsumer) {
        Thread thread = new Thread(() -> {
            while (!this.server.isClosed()) {
                try {
                    DatagramPacket packet = new DatagramPacket(new byte[1500], 1500);
                    this.server.receive(packet);
                    packetConsumer.accept(packet);
                } catch (IOException var9) {
                    var9.printStackTrace();
                }
            }

        });
        thread.setDaemon(true);
        thread.start();
    }


    private void consumePackets(DatagramPacket packet) {
        String[] var2 = (new String(packet.getData(), FastStatsDClient.MESSAGE_CHARSET)).split("\n");
        int var3 = var2.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            String msg = var2[var4];
            List var6 = this.messagesReceived;
            synchronized (this.messagesReceived) {
                this.messagesReceived.add(msg.trim());
            }
        }
    }

    public void waitForMessage(int minCount) {
        while (this.messageSize() < minCount) {
            try {
                Thread.sleep(50L);
            } catch (InterruptedException var3) {
                ;
            }
        }

    }

    private int messageSize() {
        synchronized (this.messagesReceived) {
            return this.messagesReceived.size();
        }
    }

    public boolean messageReceived(String metricName) {
        return this.messagesReceived().stream().anyMatch((s) -> {
            if (s.contains(metricName)) {
                return true;
            } else {
                return false;
            }
        });
    }

    public List<String> messagesReceived() {
        synchronized (this.messagesReceived) {
            return new ArrayList<>(this.messagesReceived);
        }
    }

    public void close() {
        this.server.close();
    }

    public void clear() {
        synchronized (this.messagesReceived) {
            this.messagesReceived.clear();
        }
    }

    public static void main(String[] args) throws SocketException, InterruptedException {
        DummyStatsDServer dummyStatsDServer = new DummyStatsDServer(STATSD_SERVER_PORT, (p) -> {/* do nothing */});
        while (true) {
            Thread.sleep(1000);
        }
    }
}
