/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.event.lifecycle;

import net.minecraft.server.level.FullChunkStatus;

public interface FullChunkStatusEventTracker {
    public void fabric_setCurrentEventFullChunkStatus(FullChunkStatus var1);

    public FullChunkStatus fabric_getCurrentEventFullChunkStatus();
}

