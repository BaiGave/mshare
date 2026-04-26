/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.renderer.block.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BlockMarker;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={BlockMarker.class})
abstract class BlockMarkerMixin {
    BlockMarkerMixin() {
    }

    @Redirect(method={"<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDDLnet/minecraft/world/level/block/state/BlockState;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/renderer/block/BlockStateModelSet;getParticleMaterial(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/resources/model/sprite/Material$Baked;"))
    private static Material.Baked getParticleMaterialProxy(BlockStateModelSet models, BlockState state, ClientLevel level, double x, double y, double z, BlockState state1) {
        return models.getParticleMaterial(state, level, BlockPos.containing(x, y, z));
    }
}

