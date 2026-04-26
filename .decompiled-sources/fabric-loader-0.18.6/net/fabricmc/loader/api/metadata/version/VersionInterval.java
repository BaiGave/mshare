/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.api.metadata.version;

import java.util.Collection;
import java.util.List;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.impl.util.version.VersionIntervalImpl;

public interface VersionInterval {
    public static final VersionInterval INFINITE = new VersionIntervalImpl(null, false, null, false);

    public boolean isSemantic();

    public Version getMin();

    public boolean isMinInclusive();

    public Version getMax();

    public boolean isMaxInclusive();

    default public VersionInterval and(VersionInterval o) {
        return VersionInterval.and(this, o);
    }

    default public List<VersionInterval> or(Collection<VersionInterval> o) {
        return VersionInterval.or(o, this);
    }

    default public List<VersionInterval> not() {
        return VersionInterval.not(this);
    }

    public static VersionInterval and(VersionInterval a, VersionInterval b) {
        return VersionIntervalImpl.and(a, b);
    }

    public static List<VersionInterval> and(Collection<VersionInterval> a, Collection<VersionInterval> b) {
        return VersionIntervalImpl.and(a, b);
    }

    public static List<VersionInterval> or(Collection<VersionInterval> a, VersionInterval b) {
        return VersionIntervalImpl.or(a, b);
    }

    public static List<VersionInterval> not(VersionInterval interval) {
        return VersionIntervalImpl.not(interval);
    }

    public static List<VersionInterval> not(Collection<VersionInterval> intervals) {
        return VersionIntervalImpl.not(intervals);
    }
}

