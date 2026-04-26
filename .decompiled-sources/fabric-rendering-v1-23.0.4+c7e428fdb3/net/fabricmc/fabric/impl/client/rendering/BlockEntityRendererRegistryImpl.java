/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.rendering;

import java.util.HashMap;
import java.util.function.BiConsumer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class BlockEntityRendererRegistryImpl {
    private static final HashMap<BlockEntityType<?>, BlockEntityRendererProvider<?, ?>> MAP = new HashMap();
    private static BiConsumer<BlockEntityType<?>, BlockEntityRendererProvider<?, ?>> handler = (type, function) -> MAP.put((BlockEntityType<?>)type, (BlockEntityRendererProvider<?, ?>)function);

    public static <E extends BlockEntity, S extends BlockEntityRenderState> void register(BlockEntityType<E> blockEntityType, BlockEntityRendererProvider<? super E, ? super S> blockEntityRendererProvider) {
        handler.accept(blockEntityType, blockEntityRendererProvider);
    }

    public static void setup(BiConsumer<BlockEntityType<?>, BlockEntityRendererProvider<?, ?>> vanillaHandler) {
        MAP.forEach(vanillaHandler);
        handler = vanillaHandler;
    }

    private BlockEntityRendererRegistryImpl() {
    }
}

