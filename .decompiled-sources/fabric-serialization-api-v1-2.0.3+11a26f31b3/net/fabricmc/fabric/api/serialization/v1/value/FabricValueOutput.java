/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.serialization.v1.value;

import net.fabricmc.fabric.impl.serialization.SpecialCodecs;
import net.minecraft.world.level.storage.ValueOutput;

public interface FabricValueOutput {
    default public void putLongArray(String key, long[] value) {
        ((ValueOutput)this).store(key, SpecialCodecs.LONG_ARRAY, value);
    }

    default public void putByteArray(String key, byte[] value) {
        ((ValueOutput)this).store(key, SpecialCodecs.BYTE_ARRAY, value);
    }
}

