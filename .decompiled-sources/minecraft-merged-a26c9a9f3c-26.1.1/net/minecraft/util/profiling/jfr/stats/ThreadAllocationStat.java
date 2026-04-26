/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.profiling.jfr.stats;

import com.google.common.base.MoreObjects;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordedThread;

public record ThreadAllocationStat(Instant timestamp, String threadName, long totalBytes) {
    private static final String UNKNOWN_THREAD = "unknown";

    public static ThreadAllocationStat from(RecordedEvent event) {
        RecordedThread recoredThread = event.getThread("thread");
        String threadName = recoredThread == null ? UNKNOWN_THREAD : MoreObjects.firstNonNull(recoredThread.getJavaName(), UNKNOWN_THREAD);
        return new ThreadAllocationStat(event.getStartTime(), threadName, event.getLong("allocated"));
    }

    public static Summary summary(List<ThreadAllocationStat> stats) {
        TreeMap<String, Double> allocationsPerSecondByThread = new TreeMap<String, Double>();
        Map<String, List<ThreadAllocationStat>> byThread = stats.stream().collect(Collectors.groupingBy(it -> it.threadName));
        byThread.forEach((thread, threadStats) -> {
            if (threadStats.size() < 2) {
                return;
            }
            ThreadAllocationStat first = (ThreadAllocationStat)threadStats.get(0);
            ThreadAllocationStat last = (ThreadAllocationStat)threadStats.get(threadStats.size() - 1);
            long duration = Duration.between(first.timestamp, last.timestamp).getSeconds();
            long diff = last.totalBytes - first.totalBytes;
            allocationsPerSecondByThread.put((String)thread, (double)diff / (double)duration);
        });
        return new Summary(allocationsPerSecondByThread);
    }

    public record Summary(Map<String, Double> allocationsPerSecondByThread) {
    }
}

