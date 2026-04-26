/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import java.util.Queue;
import net.minecraft.util.ArrayListDeque;

public class SuppressedExceptionCollector {
    private static final int LATEST_ENTRY_COUNT = 8;
    private final Queue<LongEntry> latestEntries = new ArrayListDeque<LongEntry>();
    private final Object2IntLinkedOpenHashMap<ShortEntry> entryCounts = new Object2IntLinkedOpenHashMap();

    private static long currentTimeMs() {
        return System.currentTimeMillis();
    }

    public synchronized void addEntry(String location, Throwable throwable) {
        long now = SuppressedExceptionCollector.currentTimeMs();
        String message = throwable.getMessage();
        this.latestEntries.add(new LongEntry(now, location, throwable.getClass(), message));
        while (this.latestEntries.size() > 8) {
            this.latestEntries.remove();
        }
        ShortEntry key = new ShortEntry(location, throwable.getClass());
        int currentValue = this.entryCounts.getInt(key);
        this.entryCounts.putAndMoveToFirst(key, currentValue + 1);
    }

    public synchronized String dump() {
        long current = SuppressedExceptionCollector.currentTimeMs();
        StringBuilder result = new StringBuilder();
        if (!this.latestEntries.isEmpty()) {
            result.append("\n\t\tLatest entries:\n");
            for (LongEntry longEntry : this.latestEntries) {
                result.append("\t\t\t").append(longEntry.location).append(":").append(longEntry.cls).append(": ").append(longEntry.message).append(" (").append(current - longEntry.timestampMs).append("ms ago)").append("\n");
            }
        }
        if (!this.entryCounts.isEmpty()) {
            if (result.isEmpty()) {
                result.append("\n");
            }
            result.append("\t\tEntry counts:\n");
            for (Object2IntMap.Entry entry : Object2IntMaps.fastIterable(this.entryCounts)) {
                result.append("\t\t\t").append(((ShortEntry)entry.getKey()).location).append(":").append(((ShortEntry)entry.getKey()).cls).append(" x ").append(entry.getIntValue()).append("\n");
            }
        }
        if (result.isEmpty()) {
            return "~~NONE~~";
        }
        return result.toString();
    }

    private record LongEntry(long timestampMs, String location, Class<? extends Throwable> cls, String message) {
    }

    private record ShortEntry(String location, Class<? extends Throwable> cls) {
    }
}

