/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.model.loading.v1;

import net.fabricmc.fabric.api.event.Event;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4fc;

public final class ModelModifier {
    public static final Identifier OVERRIDE_PHASE = Identifier.fromNamespaceAndPath("fabric", "override");
    public static final Identifier DEFAULT_PHASE = Event.DEFAULT_PHASE;
    public static final Identifier WRAP_PHASE = Identifier.fromNamespaceAndPath("fabric", "wrap");
    public static final Identifier WRAP_LAST_PHASE = Identifier.fromNamespaceAndPath("fabric", "wrap_last");

    private ModelModifier() {
    }

    @FunctionalInterface
    public static interface AfterBakeItem {
        public ItemModel modifyModelAfterBake(ItemModel var1, Context var2);

        @ApiStatus.NonExtendable
        public static interface Context {
            public Identifier itemId();

            public ItemModel.Unbaked sourceModel();

            public ItemModel.BakingContext bakingContext();

            public Matrix4fc transformation();
        }
    }

    @FunctionalInterface
    public static interface BeforeBakeItem {
        public ItemModel.Unbaked modifyModelBeforeBake(ItemModel.Unbaked var1, Context var2);

        @ApiStatus.NonExtendable
        public static interface Context {
            public Identifier itemId();

            public ItemModel.BakingContext bakingContext();

            public Matrix4fc transformation();
        }
    }

    @FunctionalInterface
    public static interface AfterBakeBlock {
        public BlockStateModel modifyModelAfterBake(BlockStateModel var1, Context var2);

        @ApiStatus.NonExtendable
        public static interface Context {
            public BlockState state();

            public BlockStateModel.UnbakedRoot sourceModel();

            public ModelBaker baker();
        }
    }

    @FunctionalInterface
    public static interface BeforeBakeBlock {
        public BlockStateModel.UnbakedRoot modifyModelBeforeBake(BlockStateModel.UnbakedRoot var1, Context var2);

        @ApiStatus.NonExtendable
        public static interface Context {
            public BlockState state();

            public ModelBaker baker();
        }
    }

    @FunctionalInterface
    public static interface OnLoadBlock {
        public BlockStateModel.UnbakedRoot modifyModelOnLoad(BlockStateModel.UnbakedRoot var1, Context var2);

        @ApiStatus.NonExtendable
        public static interface Context {
            public BlockState state();
        }
    }

    @FunctionalInterface
    public static interface OnLoad {
        public UnbakedModel modifyModelOnLoad(UnbakedModel var1, Context var2);

        @ApiStatus.NonExtendable
        public static interface Context {
            public Identifier id();
        }
    }
}

