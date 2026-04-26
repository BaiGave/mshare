/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.particle;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.fabricmc.fabric.api.particle.v1.FabricBlockParticleOption;
import net.fabricmc.fabric.impl.particle.BlockParticleOptionExtension;
import net.fabricmc.fabric.impl.particle.ExtendedBlockParticleOptionStreamCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={BlockParticleOption.class})
abstract class BlockParticleOptionMixin
implements FabricBlockParticleOption,
BlockParticleOptionExtension {
    @Unique
    private @Nullable BlockPos blockPos;

    BlockParticleOptionMixin() {
    }

    @Override
    public @Nullable BlockPos getBlockPos() {
        return this.blockPos;
    }

    @Override
    public void fabric_setBlockPos(@Nullable BlockPos pos) {
        this.blockPos = pos;
    }

    @ModifyReturnValue(method={"streamCodec"}, at={@At(value="RETURN")})
    private static StreamCodec<? super RegistryFriendlyByteBuf, BlockParticleOption> modifyStreamCodec(StreamCodec<? super RegistryFriendlyByteBuf, BlockParticleOption> codec) {
        return new ExtendedBlockParticleOptionStreamCodec(codec);
    }
}

