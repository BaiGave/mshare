/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.registry.sync;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.fabric.impl.registry.sync.RemapException;
import net.minecraft.resources.Identifier;

public interface RemappableRegistry {
    public void remap(Object2IntMap<Identifier> var1, RemapMode var2) throws RemapException;

    public void unmap() throws RemapException;

    public static enum RemapMode {
        AUTHORITATIVE,
        REMOTE;

    }
}

