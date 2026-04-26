/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.object.builder;

import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={EntityDataSerializers.class})
public interface EntityDataSerializersAccessor {
    @Accessor(value="SERIALIZERS")
    public static CrudeIncrementalIntIdentityHashBiMap<EntityDataSerializer<?>> fabric_getDataHandlers() {
        throw new AssertionError((Object)"Untransformed @Accessor");
    }
}

