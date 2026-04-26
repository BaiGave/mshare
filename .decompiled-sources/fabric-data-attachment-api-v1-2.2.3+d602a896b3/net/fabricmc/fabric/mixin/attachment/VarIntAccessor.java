/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.attachment;

import net.minecraft.network.VarInt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={VarInt.class})
public interface VarIntAccessor {
    @Accessor(value="MAX_VARINT_SIZE")
    public static int getMaxByteSize() {
        throw new UnsupportedOperationException("implemented via mixin");
    }
}

