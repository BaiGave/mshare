/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.serialization;

import net.fabricmc.fabric.api.serialization.v1.value.FabricValueOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.TagValueOutput;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={TagValueOutput.class})
public class TagValueOutputMixin
implements FabricValueOutput {
    @Shadow
    @Final
    private CompoundTag output;

    @Override
    public void putByteArray(String key, byte[] value) {
        this.output.putByteArray(key, value);
    }

    @Override
    public void putLongArray(String key, long[] value) {
        this.output.putLongArray(key, value);
    }
}

