/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.serialization.v1.value;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.impl.serialization.SpecialCodecs;
import net.minecraft.world.level.storage.ValueInput;

public interface FabricValueInput {
    default public Collection<String> keySet() {
        return ((ValueInput)this).read(SpecialCodecs.KEYS_EXTRACT).orElse(List.of());
    }

    default public boolean contains(String key) {
        return ((ValueInput)this).read(SpecialCodecs.contains(key)).orElseThrow();
    }

    default public Optional<long[]> getOptionalLongArray(String key) {
        return ((ValueInput)this).read(key, SpecialCodecs.LONG_ARRAY);
    }

    default public Optional<byte[]> getOptionalByteArray(String key) {
        return ((ValueInput)this).read(key, SpecialCodecs.BYTE_ARRAY);
    }
}

