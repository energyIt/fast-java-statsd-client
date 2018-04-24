//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.energyit.statsd;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public final class DummyStatsDServer implements Closeable {

    private final List<String> messagesReceived = new ArrayList<>();
    private final DatagramSocket server;

    public DummyStatsDServer(int port) throws SocketException {
        this.server = new DatagramSocket(port);
        Thread thread = new Thread(() -> {
            while (!this.server.isClosed()) {
                try {
                    DatagramPacket packet = new DatagramPacket(new byte[1500], 1500);
                    this.server.receive(packet);
                    String[] var2 = (new String(packet.getData(), FastStatsDClient.MESSAGE_CHARSET)).split("\n");
                    int var3 = var2.length;

                    for (int var4 = 0; var4 < var3; ++var4) {
                        String msg = var2[var4];
                        List var6 = this.messagesReceived;
                        synchronized (this.messagesReceived) {
                            this.messagesReceived.add(msg.trim());
                        }
                    }
                } catch (IOException var9) {
                    ;
                }
            }

        });
        thread.setDaemon(true);
        thread.start();
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
}
