/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.renderer.block.model;

import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={BlockStateModelPart.class})
interface BlockStateModelPartMixin
extends FabricBlockStateModelPart {
}

