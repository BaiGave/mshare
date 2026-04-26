/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.item.v1;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface FabricComponentMapBuilder {
    default public <T> T getOrCreate(DataComponentType<T> type, Supplier<T> fallback) {
        throw new AssertionError((Object)"Implemented in Mixin");
    }

    default public <T> T getOrDefault(DataComponentType<T> type, T defaultValue) {
        Objects.requireNonNull(defaultValue, "Cannot insert null values to component map builder");
        return (T)this.getOrCreate(type, () -> defaultValue);
    }

    default public <T> List<T> getOrEmpty(DataComponentType<List<T>> type) {
        throw new AssertionError((Object)"Implemented in Mixin");
    }

    default public boolean contains(DataComponentType<?> type) {
        throw new AssertionError((Object)"Implemented in Mixin");
    }
}

