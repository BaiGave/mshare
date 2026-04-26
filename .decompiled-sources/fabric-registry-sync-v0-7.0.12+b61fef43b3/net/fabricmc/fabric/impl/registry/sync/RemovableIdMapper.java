/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.registry.sync;

import it.unimi.dsi.fastutil.ints.Int2IntMap;

public interface RemovableIdMapper<T> {
    public void fabric_clear();

    public void fabric_remove(T var1);

    public void fabric_removeId(int var1);

    public void fabric_remapId(int var1, int var2);

    public void fabric_remapIds(Int2IntMap var1);
}

