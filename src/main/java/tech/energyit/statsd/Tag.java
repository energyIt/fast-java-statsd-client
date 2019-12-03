package tech.energyit.statsd;

/**
 * StatsD tag value provider.
 */
public interface Tag {
    byte[] getName();

    byte[] getValue();
}