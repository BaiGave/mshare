/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Locale;
import net.minecraft.CrashReportDetail;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class CrashReportCategory {
    private final String title;
    private final List<Entry> entries = Lists.newArrayList();
    private StackTraceElement[] stackTrace = new StackTraceElement[0];

    public CrashReportCategory(String title) {
        this.title = title;
    }

    public static String formatLocation(double x, double y, double z) {
        return String.format(Locale.ROOT, "%.2f,%.2f,%.2f", x, y, z);
    }

    public static String formatLocation(LevelHeightAccessor levelHeightAccessor, double x, double y, double z) {
        return String.format(Locale.ROOT, "%.2f,%.2f,%.2f - %s", x, y, z, CrashReportCategory.formatLocation(levelHeightAccessor, BlockPos.containing(x, y, z)));
    }

    public static String formatLocation(LevelHeightAccessor levelHeightAccessor, BlockPos pos) {
        return CrashReportCategory.formatLocation(levelHeightAccessor, pos.getX(), pos.getY(), pos.getZ());
    }

    public static String formatLocation(LevelHeightAccessor levelHeightAccessor, int x, int y, int z) {
        int maxBlockZ;
        int maxBlockY;
        int maxBlockX;
        int minBlockZ;
        int minBlockY;
        int minBlockX;
        StringBuilder result = new StringBuilder();
        try {
            result.append(String.format(Locale.ROOT, "World: (%d,%d,%d)", x, y, z));
        }
        catch (Throwable ignored) {
            result.append("(Error finding world loc)");
        }
        result.append(", ");
        try {
            int sectionX = SectionPos.blockToSectionCoord(x);
            int sectionY = SectionPos.blockToSectionCoord(y);
            int sectionZ = SectionPos.blockToSectionCoord(z);
            int relativeX = x & 0xF;
            int relativeY = y & 0xF;
            int relativeZ = z & 0xF;
            minBlockX = SectionPos.sectionToBlockCoord(sectionX);
            minBlockY = levelHeightAccessor.getMinY();
            minBlockZ = SectionPos.sectionToBlockCoord(sectionZ);
            maxBlockX = SectionPos.sectionToBlockCoord(sectionX + 1) - 1;
            maxBlockY = levelHeightAccessor.getMaxY();
            maxBlockZ = SectionPos.sectionToBlockCoord(sectionZ + 1) - 1;
            result.append(String.format(Locale.ROOT, "Section: (at %d,%d,%d in %d,%d,%d; chunk contains blocks %d,%d,%d to %d,%d,%d)", relativeX, relativeY, relativeZ, sectionX, sectionY, sectionZ, minBlockX, minBlockY, minBlockZ, maxBlockX, maxBlockY, maxBlockZ));
        }
        catch (Throwable ignored) {
            result.append("(Error finding chunk loc)");
        }
        result.append(", ");
        try {
            int regionX = x >> 9;
            int regionZ = z >> 9;
            int minChunkX = regionX << 5;
            int minChunkZ = regionZ << 5;
            int maxChunkX = (regionX + 1 << 5) - 1;
            int maxChunkZ = (regionZ + 1 << 5) - 1;
            minBlockX = regionX << 9;
            minBlockY = levelHeightAccessor.getMinY();
            minBlockZ = regionZ << 9;
            maxBlockX = (regionX + 1 << 9) - 1;
            maxBlockY = levelHeightAccessor.getMaxY();
            maxBlockZ = (regionZ + 1 << 9) - 1;
            result.append(String.format(Locale.ROOT, "Region: (%d,%d; contains chunks %d,%d to %d,%d, blocks %d,%d,%d to %d,%d,%d)", regionX, regionZ, minChunkX, minChunkZ, maxChunkX, maxChunkZ, minBlockX, minBlockY, minBlockZ, maxBlockX, maxBlockY, maxBlockZ));
        }
        catch (Throwable ignored) {
            result.append("(Error finding world loc)");
        }
        return result.toString();
    }

    public CrashReportCategory setDetail(String key, CrashReportDetail<String> callback) {
        try {
            this.setDetail(key, callback.call());
        }
        catch (Throwable t) {
            this.setDetailError(key, t);
        }
        return this;
    }

    public CrashReportCategory setDetail(String key, Object value) {
        this.entries.add(new Entry(key, value));
        return this;
    }

    public void setDetailError(String key, Throwable t) {
        this.setDetail(key, t);
    }

    public int fillInStackTrace(int nestedOffset) {
        StackTraceElement[] full = Thread.currentThread().getStackTrace();
        if (full.length <= 0) {
            return 0;
        }
        this.stackTrace = new StackTraceElement[full.length - 3 - nestedOffset];
        System.arraycopy(full, 3 + nestedOffset, this.stackTrace, 0, this.stackTrace.length);
        return this.stackTrace.length;
    }

    public boolean validateStackTrace(StackTraceElement source, StackTraceElement next) {
        if (this.stackTrace.length == 0 || source == null) {
            return false;
        }
        StackTraceElement current = this.stackTrace[0];
        if (!(current.isNativeMethod() == source.isNativeMethod() && current.getClassName().equals(source.getClassName()) && current.getFileName().equals(source.getFileName()) && current.getMethodName().equals(source.getMethodName()))) {
            return false;
        }
        if (next != null != this.stackTrace.length > 1) {
            return false;
        }
        if (next != null && !this.stackTrace[1].equals(next)) {
            return false;
        }
        this.stackTrace[0] = source;
        return true;
    }

    public void trimStacktrace(int length) {
        StackTraceElement[] swap = new StackTraceElement[this.stackTrace.length - length];
        System.arraycopy(this.stackTrace, 0, swap, 0, swap.length);
        this.stackTrace = swap;
    }

    public void getDetails(StringBuilder builder) {
        builder.append("-- ").append(this.title).append(" --\n");
        builder.append("Details:");
        for (Entry entry : this.entries) {
            builder.append("\n\t");
            builder.append(entry.getKey());
            builder.append(": ");
            builder.append(entry.getValue());
        }
        if (this.stackTrace != null && this.stackTrace.length > 0) {
            builder.append("\nStacktrace:");
            for (StackTraceElement element : this.stackTrace) {
                builder.append("\n\tat ");
                builder.append(element);
            }
        }
    }

    public StackTraceElement[] getStacktrace() {
        return this.stackTrace;
    }

    public static void populateBlockDetails(CrashReportCategory category, LevelHeightAccessor levelHeightAccessor, BlockPos pos, BlockState state) {
        category.setDetail("Block", state::toString);
        CrashReportCategory.populateBlockLocationDetails(category, levelHeightAccessor, pos);
    }

    public static CrashReportCategory populateBlockLocationDetails(CrashReportCategory category, LevelHeightAccessor levelHeightAccessor, BlockPos pos) {
        return category.setDetail("Block location", () -> CrashReportCategory.formatLocation(levelHeightAccessor, pos));
    }

    private static class Entry {
        private final String key;
        private final String value;

        public Entry(String key, @Nullable Object value) {
            this.key = key;
            if (value == null) {
                this.value = "~~NULL~~";
            } else if (value instanceof Throwable) {
                Throwable t = (Throwable)value;
                this.value = "~~ERROR~~ " + t.getClass().getSimpleName() + ": " + t.getMessage();
            } else {
                this.value = value.toString();
            }
        }

        public String getKey() {
            return this.key;
        }

        public String getValue() {
            return this.value;
        }
    }
}

