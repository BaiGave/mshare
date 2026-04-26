/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.buffers;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public interface GpuFence
extends AutoCloseable {
    @Override
    public void close();

    public boolean awaitCompletion(long var1);
}

