/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.model.loading.v1;

import java.util.List;
import net.fabricmc.fabric.api.client.model.loading.v1.BlockStateResolver;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedExtraModel;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.impl.client.model.loading.ModelLoadingPluginManager;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnmodifiableView;

@FunctionalInterface
public interface ModelLoadingPlugin {
    public static void register(ModelLoadingPlugin plugin) {
        ModelLoadingPluginManager.registerPlugin(plugin);
    }

    public static @UnmodifiableView List<ModelLoadingPlugin> getAll() {
        return ModelLoadingPluginManager.PLUGINS_VIEW;
    }

    public void initialize(Context var1);

    @ApiStatus.NonExtendable
    public static interface Context {
        public void registerBlockStateResolver(Block var1, BlockStateResolver var2);

        public <T> void addModel(ExtraModelKey<T> var1, UnbakedExtraModel<T> var2);

        public Event<ModelModifier.OnLoad> modifyModelOnLoad();

        public Event<ModelModifier.OnLoadBlock> modifyBlockModelOnLoad();

        public Event<ModelModifier.BeforeBakeBlock> modifyBlockModelBeforeBake();

        public Event<ModelModifier.AfterBakeBlock> modifyBlockModelAfterBake();

        public Event<ModelModifier.BeforeBakeItem> modifyItemModelBeforeBake();

        public Event<ModelModifier.AfterBakeItem> modifyItemModelAfterBake();
    }
}

