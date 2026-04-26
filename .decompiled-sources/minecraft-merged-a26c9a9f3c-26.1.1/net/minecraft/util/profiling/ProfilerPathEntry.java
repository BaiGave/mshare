/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.profiling;

import it.unimi.dsi.fastutil.objects.Object2LongMap;

public interface ProfilerPathEntry {
    public long getDuration();

    public long getMaxDuration();

    public long getCount();

    public Object2LongMap<String> getCounters();
}

