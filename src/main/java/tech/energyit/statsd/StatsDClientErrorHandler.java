package tech.energyit.statsd;

/**
 * Describes a handler capable of processing exceptions that occur during StatsD client operations.
 * 
 * @author Tom Denley
 *
 */
public interface StatsDClientErrorHandler {

    /**
     * Handle the given exception, which occurred during a StatsD client operation.
     * 
     * @param exception
     *     the {@link Exception} that occurred
     */
    void handle(Exception exception);

    /**
     * Handle the given handle, represented as a string message.
     * Allows to avoid exception creation.
     *
     * @param errorFormat - error message template using String's format
     * @param args to be used in the message template
     */
    void handle(String errorFormat, Object... args);

    StatsDClientErrorHandler NO_OP_HANDLER = new StatsDClientErrorHandler() {

        @Override
        public void handle(final Exception e) { /* No-op */ }

        @Override
        public void handle(String errorFormat, Object... args) { /* No-op */ }
    };
}
