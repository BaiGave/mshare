/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.attachment;

import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ClientboundCustomPayloadPacket.class})
public interface ClientboundCustomPayloadPacketAccessor {
    @Accessor(value="MAX_PAYLOAD_SIZE")
    public static int getMaxPayloadSize() {
        throw new UnsupportedOperationException("Implemented via mixin");
    }
}

