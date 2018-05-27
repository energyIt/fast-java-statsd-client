package tech.energyit.statsd.utils;

import tech.energyit.statsd.Sender;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class LoggingSender implements Sender {

    private List<String> messages = new ArrayList<>();

    @Override
    public synchronized void send(ByteBuffer msg) {
        byte[] bytes = new byte[msg.remaining()];
        msg.get(bytes);
        messages.add(new String(bytes));
    }

    public synchronized List<String> getMessages() {
        return new ArrayList<>(messages);
    }
}
