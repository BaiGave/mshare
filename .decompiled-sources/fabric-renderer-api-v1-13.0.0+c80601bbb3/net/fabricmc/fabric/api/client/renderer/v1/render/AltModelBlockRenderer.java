/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.renderer.v1.render;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public interface AltModelBlockRenderer {
    public void tesselateBlock(QuadEmitter var1, float var2, float var3, float var4, BlockAndTintGetter var5, BlockPos var6, BlockState var7, BlockStateModel var8, long var9);
}

