/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.state.level;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record BlockOutlineRenderState(BlockPos pos, boolean isTranslucent, boolean highContrast, VoxelShape shape, @Nullable VoxelShape collisionShape, @Nullable VoxelShape occlusionShape, @Nullable VoxelShape interactionShape) implements FabricRenderState
{
    public BlockOutlineRenderState(BlockPos pos, boolean isTranslucent, boolean highContrast, VoxelShape shape) {
        this(pos, isTranslucent, highContrast, shape, null, null, null);
    }
}

