package com.energyit.statsd;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class LoggingSender implements Sender {

    private List<String> messages = new ArrayList<>();

    @Override
    public void send(ByteBuffer msg) {
        byte[] bytes = new byte[msg.remaining()];
        msg.get(bytes);
        messages.add(new String(bytes));
    }

    public List<String> getMessages() {
        return messages;
    }
}
