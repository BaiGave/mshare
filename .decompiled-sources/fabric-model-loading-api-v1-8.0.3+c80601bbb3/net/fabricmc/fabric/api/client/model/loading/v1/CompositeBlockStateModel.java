/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.model.loading.v1;

import java.util.List;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.fabricmc.fabric.impl.client.model.loading.CompositeBlockStateModelImpl;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Unmodifiable;

@ApiStatus.NonExtendable
public interface CompositeBlockStateModel
extends BlockStateModel {
    public static CompositeBlockStateModel of(List<BlockStateModel> models) {
        return CompositeBlockStateModelImpl.of(models);
    }

    public @Unmodifiable List<BlockStateModel> models();

    @ApiStatus.NonExtendable
    public static interface Unbaked
    extends CustomUnbakedBlockStateModel {
        public static Unbaked of(List<BlockStateModel.Unbaked> models) {
            return CompositeBlockStateModelImpl.Unbaked.of(models);
        }

        public @Unmodifiable List<BlockStateModel.Unbaked> models();
    }
}

