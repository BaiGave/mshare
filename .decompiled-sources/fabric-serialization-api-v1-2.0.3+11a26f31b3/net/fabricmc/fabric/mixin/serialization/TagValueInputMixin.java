/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.serialization;

import java.util.Collection;
import java.util.Optional;
import net.fabricmc.fabric.api.serialization.v1.value.FabricValueInput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.TagValueInput;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={TagValueInput.class})
public class TagValueInputMixin
implements FabricValueInput {
    @Shadow
    @Final
    private CompoundTag input;

    @Override
    public Collection<String> keySet() {
        return this.input.keySet();
    }

    @Override
    public boolean contains(String key) {
        return this.input.contains(key);
    }

    @Override
    public Optional<byte[]> getOptionalByteArray(String key) {
        return this.input.getByteArray(key);
    }

    @Override
    public Optional<long[]> getOptionalLongArray(String key) {
        return this.input.getLongArray(key);
    }
}

