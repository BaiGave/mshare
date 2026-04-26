/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.model.loading.v1;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;

@FunctionalInterface
public interface BlockStateResolver {
    public void resolveBlockStates(Context var1);

    @ApiStatus.NonExtendable
    public static interface Context {
        public Block block();

        public void setModel(BlockState var1, BlockStateModel.UnbakedRoot var2);
    }
}

