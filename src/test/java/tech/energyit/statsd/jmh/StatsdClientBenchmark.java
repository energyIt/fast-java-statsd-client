package tech.energyit.statsd.jmh;

import com.timgroup.statsd.NonBlockingStatsDClient;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import tech.energyit.statsd.*;
import tech.energyit.statsd.utils.DummyStatsDServer;

import java.util.concurrent.TimeUnit;

@Fork(1)
@Warmup(iterations = 2)
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class StatsdClientBenchmark {

    private static final int STATSD_SERVER_PORT = DummyStatsDServer.STATSD_SERVER_PORT;
    private static final String LOCALHOST = "localhost";
    private static final String PREFIX = "my.prefix";

    private static final String TAG1_STRING = "tag1:val1";
    private static final String TAG2_STRING = "tag2:val2";

    private static final Tag TAG1 = new TagImpl("tag1".getBytes(), "val1".getBytes());
    private static final Tag TAG2 = new TagImpl("tag2".getBytes(), "val2".getBytes());

    private static final String METRIC = "my.metric";
    private static final byte[] METRIC_RAW = METRIC.getBytes();

    private SynchronousSender sender;
    private FastStatsDClient statsDClient;

    private NonBlockingStatsDClient dataDogClient;

    @Setup
    public void init() {
        sender = new SynchronousSender(LOCALHOST, STATSD_SERVER_PORT);
        statsDClient = new FastStatsDClient(PREFIX, sender);

        dataDogClient = new NonBlockingStatsDClient(PREFIX, LOCALHOST, STATSD_SERVER_PORT);

    }

    @TearDown
    public void cleanup() {
        sender.close();
        dataDogClient.close();
    }

    @Benchmark
    public void countWithTwoTagsViaFastClient(Blackhole bh) {
        statsDClient.count(METRIC_RAW, bh.i1, TAG1, TAG2);
    }

    @Benchmark
    public void countWithTagViaFastClient(Blackhole bh) {
        statsDClient.count(METRIC_RAW, bh.i1, TAG1);
    }

    @Benchmark
    public void countWithTwoTagsViaDatadogClient(Blackhole bh) {
        dataDogClient.count(METRIC, bh.i1, TAG1_STRING, TAG2_STRING);
    }

    @Benchmark
    public void countWithTagViaDatadogClient(Blackhole bh) {
        dataDogClient.count(METRIC, bh.i1, TAG1_STRING);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .addProfiler(GCProfiler.class)
                .include(StatsdClientBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }

}