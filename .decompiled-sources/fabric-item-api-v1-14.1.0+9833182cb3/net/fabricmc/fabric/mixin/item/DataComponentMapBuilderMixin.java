/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.item;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.item.v1.FabricComponentMapBuilder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={DataComponentMap.Builder.class})
abstract class DataComponentMapBuilderMixin
implements FabricComponentMapBuilder {
    @Shadow
    @Final
    private Reference2ObjectMap<DataComponentType<?>, Object> map;

    DataComponentMapBuilderMixin() {
    }

    @Shadow
    public abstract <T> DataComponentMap.Builder set(DataComponentType<T> var1, @Nullable T var2);

    @Override
    public <T> T getOrCreate(DataComponentType<T> type, Supplier<T> fallback) {
        if (!this.map.containsKey(type)) {
            T defaultValue = fallback.get();
            Objects.requireNonNull(defaultValue, "Cannot insert null values to component map builder");
            this.set(type, defaultValue);
        }
        return (T)this.map.get(type);
    }

    @Override
    public <T> List<T> getOrEmpty(DataComponentType<List<T>> type) {
        ArrayList existing = new ArrayList(this.getOrCreate(type, Collections::emptyList));
        this.set(type, existing);
        return existing;
    }

    @Override
    public boolean contains(DataComponentType<?> type) {
        return this.map.containsKey(type);
    }
}

