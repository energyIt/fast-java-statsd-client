package tech.energyit.statsd.jmh;

import com.timgroup.statsd.NonBlockingStatsDClient;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import tech.energyit.statsd.FastStatsDClient;
import tech.energyit.statsd.SynchronousSender;
import tech.energyit.statsd.Tag;
import tech.energyit.statsd.TagImpl;
import tech.energyit.statsd.async.AsynchronousSender;
import tech.energyit.statsd.utils.DummyStatsDServer;

import java.util.concurrent.TimeUnit;

@Fork(1)
@Warmup(iterations = 1)
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@BenchmarkMode(Mode.AverageTime)
@Measurement(iterations = 1)
public class StatsdClientBenchmark {

    private static final int STATSD_SERVER_PORT = DummyStatsDServer.STATSD_SERVER_PORT;
    private static final String LOCALHOST = "localhost";
    private static final String PREFIX = "my.prefix";

    private static final String TAG1_STRING = "tag1:val1";
    private static final String TAG2_STRING = "tag2:val2";

    private static final Tag TAG1 = new TagImpl("tag1".getBytes(), "val1".getBytes());
    private static final Tag TAG2 = new TagImpl("tag2".getBytes(), "val2".getBytes());
    private static final Tag[] THE_TWO_TAGS = {TAG1, TAG2};
    private static final String METRIC = "my.metric";
    private static final byte[] METRIC_RAW = METRIC.getBytes();

    private SynchronousSender syncSender;
    private FastStatsDClient statsDClient;
    private FastStatsDClient statsDClientWithExactDoubles;
    private AsynchronousSender asyncSender;
    private FastStatsDClient asyncStatsDClient;
    private AsynchronousSender asyncSenderWithFallback;
    private FastStatsDClient asyncStatsDClient2;

    private NonBlockingStatsDClient dataDogClient;

    @Setup
    public void init() {
        syncSender = SynchronousSender.builder()
                .withHostAndPort(LOCALHOST, STATSD_SERVER_PORT)
                .build();
        asyncSender = AsynchronousSender.builder()
                .withHostAndPort(LOCALHOST, STATSD_SERVER_PORT)
                .withRingbufferSize(1024)
                .skipMessageWhenRingbufferIsFull()
                .build();
        asyncSenderWithFallback = AsynchronousSender.builder()
                .withHostAndPort(LOCALHOST, STATSD_SERVER_PORT)
                .withRingbufferSize(1024)
                .publishSynchronouslyWhenRingbufferIsFull()
                .build();
        statsDClient = new FastStatsDClient(PREFIX, syncSender, false);
        statsDClientWithExactDoubles = new FastStatsDClient(PREFIX, syncSender, true);
        asyncStatsDClient = new FastStatsDClient(PREFIX, asyncSender, false);
        asyncStatsDClient2 = new FastStatsDClient(PREFIX, asyncSenderWithFallback, false);
        dataDogClient = new NonBlockingStatsDClient(PREFIX, LOCALHOST, STATSD_SERVER_PORT);

    }

    @TearDown
    public void cleanup() {
        syncSender.close();
        asyncSender.close();
        asyncSenderWithFallback.close();
        dataDogClient.close();
    }

    @Benchmark
    public void countLongsViaSyncFastClient(Blackhole bh) {
        statsDClient.count(METRIC_RAW, bh.i1, THE_TWO_TAGS);
    }

    @Benchmark
    public void countDoublesViaSyncFastClientUsingExactDoubles(Blackhole bh) {
        statsDClientWithExactDoubles.count(METRIC_RAW, bh.d1, THE_TWO_TAGS);
    }

    @Benchmark
    public void countDoublesViaSyncFastClient(Blackhole bh) {
        statsDClient.count(METRIC_RAW, bh.d1, THE_TWO_TAGS);
    }

    @Benchmark
    public void countLongsViaAsyncFastClient(Blackhole bh) {
        asyncStatsDClient.count(METRIC_RAW, bh.i1, THE_TWO_TAGS);
    }

    @Benchmark
    public void countLongsViaAsyncFastClientWithFallbackToSyncSender(Blackhole bh) {
        asyncStatsDClient2.count(METRIC_RAW, bh.i1, THE_TWO_TAGS);
    }

    @Benchmark
    public void countLongsViaDatadogClient(Blackhole bh) {
        dataDogClient.count(METRIC, bh.i1, TAG1_STRING, TAG2_STRING);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .addProfiler(GCProfiler.class)
                .include(StatsdClientBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }

}