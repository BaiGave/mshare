/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.renderer.block.particle;

import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModelSet;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={BlockStateModelSet.class})
abstract class BlockStateModelSetMixin
implements FabricBlockStateModelSet {
    BlockStateModelSetMixin() {
    }
}

