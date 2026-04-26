/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.model.loading.v1;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.impl.client.model.loading.CustomUnbakedBlockStateModelRegistry;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.resources.Identifier;

public interface CustomUnbakedBlockStateModel
extends BlockStateModel.Unbaked {
    public static void register(Identifier id, MapCodec<? extends CustomUnbakedBlockStateModel> codec) {
        CustomUnbakedBlockStateModelRegistry.register(id, codec);
    }

    public MapCodec<? extends CustomUnbakedBlockStateModel> codec();
}

