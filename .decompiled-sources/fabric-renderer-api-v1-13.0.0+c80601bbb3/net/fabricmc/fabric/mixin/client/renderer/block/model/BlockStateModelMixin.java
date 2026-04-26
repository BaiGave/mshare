/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.renderer.block.model;

import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={BlockStateModel.class})
interface BlockStateModelMixin
extends FabricBlockStateModel {
}

