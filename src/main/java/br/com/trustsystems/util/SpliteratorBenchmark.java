package br.com.trustsystems.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.SECONDS;

public class SpliteratorBenchmark {

    public static void main(String[] args) throws IOException, URISyntaxException {
        final Path inputPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("airports.txt").toURI());
        measureProcessing(FixedBatchSpliterator.withBatchSize(Files.lines(inputPath), 10));
    }

    private static void measureProcessing(Stream<String> input) throws IOException {
        final long start = System.nanoTime();
        try (Stream<String> lines = input) {
            final double cpuTime = lines.parallel()
                    .mapToLong(SpliteratorBenchmark::processLine).sum(), realTime = System.nanoTime() - start;
            final int cores = Runtime.getRuntime().availableProcessors();
            System.out.println("          Cores: " + cores);
            System.out.format("       CPU time: %.2f s\n", cpuTime / SECONDS.toNanos(1));
            System.out.format("      Real time: %.2f s\n", realTime / SECONDS.toNanos(1));
            System.out.format("CPU utilization: %.2f%%\n\n", 100.0 * cpuTime / realTime / cores);
        }
    }

    private static long processLine(String line) {

        final long localStart = System.nanoTime();
        System.out.println(line);
        return System.nanoTime() - localStart;
    }
}

