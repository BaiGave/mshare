/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.rendering.v1;

import net.fabricmc.fabric.impl.client.rendering.BlockEntityRendererRegistryImpl;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

@Deprecated
public final class BlockEntityRendererRegistry {
    public static <E extends BlockEntity, S extends BlockEntityRenderState> void register(BlockEntityType<E> blockEntityType, BlockEntityRendererProvider<? super E, ? super S> blockEntityRendererProvider) {
        BlockEntityRendererRegistryImpl.register(blockEntityType, blockEntityRendererProvider);
    }

    private BlockEntityRendererRegistry() {
    }
}

