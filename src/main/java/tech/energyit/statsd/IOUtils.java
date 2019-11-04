package tech.energyit.statsd;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.DatagramChannel;

public class IOUtils {

    public static InetAddress inetAddress(String hostname) {
        try {
            return InetAddress.getByName(hostname);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Cannot create address", e);
        }
    }

    public static DatagramChannel newDatagramChannel() {
        try {
            return DatagramChannel.open();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to open channel", e);
        }
    }
}
