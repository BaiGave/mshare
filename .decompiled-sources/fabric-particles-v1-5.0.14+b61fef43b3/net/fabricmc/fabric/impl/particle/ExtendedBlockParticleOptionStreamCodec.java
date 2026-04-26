/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.particle;

import net.fabricmc.fabric.impl.particle.BlockParticleOptionExtension;
import net.fabricmc.fabric.impl.particle.ExtendedBlockParticleOptionSync;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class ExtendedBlockParticleOptionStreamCodec
implements StreamCodec<RegistryFriendlyByteBuf, BlockParticleOption> {
    private static final int PACKET_MARKER = -1;
    private final StreamCodec<? super RegistryFriendlyByteBuf, BlockParticleOption> fallback;

    public ExtendedBlockParticleOptionStreamCodec(StreamCodec<? super RegistryFriendlyByteBuf, BlockParticleOption> fallback) {
        this.fallback = fallback;
    }

    @Override
    public BlockParticleOption decode(RegistryFriendlyByteBuf buf) {
        int index = buf.readerIndex();
        if (buf.readVarInt() != -1) {
            buf.readerIndex(index);
            return (BlockParticleOption)this.fallback.decode(buf);
        }
        BlockParticleOption value = (BlockParticleOption)this.fallback.decode(buf);
        BlockPos pos = (BlockPos)BlockPos.STREAM_CODEC.decode(buf);
        ((BlockParticleOptionExtension)((Object)value)).fabric_setBlockPos(pos);
        return value;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf, BlockParticleOption value) {
        BlockPos pos = value.getBlockPos();
        if (pos == null || ExtendedBlockParticleOptionSync.shouldEncodeFallback()) {
            this.fallback.encode(buf, value);
            return;
        }
        buf.writeVarInt(-1);
        this.fallback.encode(buf, value);
        BlockPos.STREAM_CODEC.encode(buf, pos);
    }
}

