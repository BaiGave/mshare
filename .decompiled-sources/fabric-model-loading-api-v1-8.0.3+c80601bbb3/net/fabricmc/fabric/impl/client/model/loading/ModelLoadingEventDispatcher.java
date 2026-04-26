/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.model.loading;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import net.fabricmc.fabric.api.client.model.loading.v1.BlockStateResolver;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedExtraModel;
import net.fabricmc.fabric.impl.client.model.loading.ModelLoadingPluginContextImpl;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.BlockStateModelLoader;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelLoadingEventDispatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelLoadingEventDispatcher.class);
    public static final ThreadLocal<ModelLoadingEventDispatcher> CURRENT = new ThreadLocal();
    private final ModelLoadingPluginContextImpl pluginContext;
    private final BlockStateResolverContext blockStateResolverContext = new BlockStateResolverContext();
    private final OnLoadModifierContext onLoadModifierContext = new OnLoadModifierContext();
    private final OnLoadBlockModifierContext onLoadBlockModifierContext = new OnLoadBlockModifierContext();

    public ModelLoadingEventDispatcher(List<ModelLoadingPlugin> plugins) {
        this.pluginContext = new ModelLoadingPluginContextImpl();
        for (ModelLoadingPlugin plugin : plugins) {
            try {
                plugin.initialize(this.pluginContext);
            }
            catch (Exception exception) {
                LOGGER.error("Failed to initialize model loading plugin", exception);
            }
        }
    }

    public Map<ExtraModelKey<?>, UnbakedExtraModel<?>> getExtraModels() {
        return this.pluginContext.extraModels;
    }

    public Map<Identifier, UnbakedModel> modifyModelsOnLoad(Map<Identifier, UnbakedModel> models) {
        if (!(models instanceof HashMap)) {
            models = new HashMap<Identifier, UnbakedModel>(models);
        }
        models.replaceAll(this::modifyModelOnLoad);
        return models;
    }

    private UnbakedModel modifyModelOnLoad(Identifier id, UnbakedModel model) {
        this.onLoadModifierContext.prepare(id);
        return this.pluginContext.modifyModelOnLoad().invoker().modifyModelOnLoad(model, this.onLoadModifierContext);
    }

    public BlockStateModelLoader.LoadedModels modifyBlockModelsOnLoad(BlockStateModelLoader.LoadedModels models) {
        Map<BlockState, BlockStateModel.UnbakedRoot> map = models.models();
        if (!(map instanceof HashMap)) {
            map = new HashMap<BlockState, BlockStateModel.UnbakedRoot>(map);
            models = new BlockStateModelLoader.LoadedModels(map);
        }
        this.putResolvedBlockStates(map);
        map.replaceAll(this::modifyBlockModelOnLoad);
        return models;
    }

    private void putResolvedBlockStates(Map<BlockState, BlockStateModel.UnbakedRoot> map) {
        this.pluginContext.blockStateResolvers.forEach((block, resolver) -> this.resolveBlockStates((BlockStateResolver)resolver, (Block)block, map::put));
    }

    private void resolveBlockStates(BlockStateResolver resolver, Block block, BiConsumer<BlockState, BlockStateModel.UnbakedRoot> output) {
        BlockStateResolverContext context = this.blockStateResolverContext;
        context.prepare(block);
        Reference2ReferenceMap<BlockState, BlockStateModel.UnbakedRoot> resolvedModels = context.models;
        ImmutableList<BlockState> allStates = block.getStateDefinition().getPossibleStates();
        boolean thrown = false;
        try {
            resolver.resolveBlockStates(context);
        }
        catch (Exception e) {
            LOGGER.error("Failed to resolve block state models for block {}. Using missing model for all states.", (Object)block, (Object)e);
            thrown = true;
        }
        if (!thrown) {
            if (resolvedModels.size() == allStates.size()) {
                resolvedModels.forEach(output);
            } else {
                for (BlockState state : allStates) {
                    @Nullable BlockStateModel.UnbakedRoot model = (BlockStateModel.UnbakedRoot)resolvedModels.get(state);
                    if (model == null) {
                        LOGGER.error("Block state resolver did not provide a model for state {} in block {}. Using missing model.", (Object)state, (Object)block);
                        continue;
                    }
                    output.accept(state, model);
                }
            }
        }
        resolvedModels.clear();
    }

    private BlockStateModel.UnbakedRoot modifyBlockModelOnLoad(BlockState state, BlockStateModel.UnbakedRoot model) {
        this.onLoadBlockModifierContext.prepare(state);
        return this.pluginContext.modifyBlockModelOnLoad().invoker().modifyModelOnLoad(model, this.onLoadBlockModifierContext);
    }

    public BlockStateModel modifyBlockModel(BlockStateModel.UnbakedRoot unbakedModel, BlockState state, ModelBaker baker, Operation<BlockStateModel> bakeOperation) {
        BakeBlockModifierContext modifierContext = new BakeBlockModifierContext(state, baker);
        unbakedModel = this.pluginContext.modifyBlockModelBeforeBake().invoker().modifyModelBeforeBake(unbakedModel, modifierContext);
        BlockStateModel model = bakeOperation.call(unbakedModel, state, baker);
        modifierContext.prepareAfterBake(unbakedModel);
        return this.pluginContext.modifyBlockModelAfterBake().invoker().modifyModelAfterBake(model, modifierContext);
    }

    public ItemModel modifyItemModel(ItemModel.Unbaked unbakedModel, Identifier itemId, ItemModel.BakingContext bakeContext, Matrix4fc transformation, Operation<ItemModel> bakeOperation) {
        BakeItemModifierContext modifierContext = new BakeItemModifierContext(itemId, bakeContext, transformation);
        unbakedModel = this.pluginContext.modifyItemModelBeforeBake().invoker().modifyModelBeforeBake(unbakedModel, modifierContext);
        ItemModel model = bakeOperation.call(unbakedModel, bakeContext, transformation);
        modifierContext.prepareAfterBake(unbakedModel);
        return this.pluginContext.modifyItemModelAfterBake().invoker().modifyModelAfterBake(model, modifierContext);
    }

    private static class BlockStateResolverContext
    implements BlockStateResolver.Context {
        private Block block;
        private final Reference2ReferenceMap<BlockState, BlockStateModel.UnbakedRoot> models = new Reference2ReferenceOpenHashMap<BlockState, BlockStateModel.UnbakedRoot>();

        private BlockStateResolverContext() {
        }

        private void prepare(Block block) {
            this.block = block;
            this.models.clear();
        }

        @Override
        public Block block() {
            return this.block;
        }

        @Override
        public void setModel(BlockState state, BlockStateModel.UnbakedRoot model) {
            Objects.requireNonNull(state, "state cannot be null");
            Objects.requireNonNull(model, "model cannot be null");
            if (!state.is(this.block)) {
                throw new IllegalArgumentException("Attempted to set model for state " + String.valueOf(state) + " on block " + String.valueOf(this.block));
            }
            if (this.models.putIfAbsent(state, model) != null) {
                throw new IllegalStateException("Duplicate model for state " + String.valueOf(state) + " on block " + String.valueOf(this.block));
            }
        }
    }

    private static class OnLoadModifierContext
    implements ModelModifier.OnLoad.Context {
        private Identifier id;

        private OnLoadModifierContext() {
        }

        private void prepare(Identifier id) {
            this.id = id;
        }

        @Override
        public Identifier id() {
            return this.id;
        }
    }

    private static class OnLoadBlockModifierContext
    implements ModelModifier.OnLoadBlock.Context {
        private BlockState state;

        private OnLoadBlockModifierContext() {
        }

        private void prepare(BlockState state) {
            this.state = state;
        }

        @Override
        public BlockState state() {
            return this.state;
        }
    }

    private static class BakeBlockModifierContext
    implements ModelModifier.BeforeBakeBlock.Context,
    ModelModifier.AfterBakeBlock.Context {
        private final BlockState state;
        private final ModelBaker baker;
        private BlockStateModel.UnbakedRoot sourceModel;

        private BakeBlockModifierContext(BlockState state, ModelBaker baker) {
            this.state = state;
            this.baker = baker;
        }

        private void prepareAfterBake(BlockStateModel.UnbakedRoot sourceModel) {
            this.sourceModel = sourceModel;
        }

        @Override
        public BlockState state() {
            return this.state;
        }

        @Override
        public ModelBaker baker() {
            return this.baker;
        }

        @Override
        public BlockStateModel.UnbakedRoot sourceModel() {
            return this.sourceModel;
        }
    }

    private static class BakeItemModifierContext
    implements ModelModifier.BeforeBakeItem.Context,
    ModelModifier.AfterBakeItem.Context {
        private final Identifier itemId;
        private final ItemModel.BakingContext bakeContext;
        private final Matrix4fc transformation;
        private ItemModel.Unbaked sourceModel;

        private BakeItemModifierContext(Identifier itemId, ItemModel.BakingContext bakeContext, Matrix4fc transformation) {
            this.itemId = itemId;
            this.bakeContext = bakeContext;
            this.transformation = transformation;
        }

        private void prepareAfterBake(ItemModel.Unbaked sourceModel) {
            this.sourceModel = sourceModel;
        }

        @Override
        public Identifier itemId() {
            return this.itemId;
        }

        @Override
        public ItemModel.BakingContext bakingContext() {
            return this.bakeContext;
        }

        @Override
        public Matrix4fc transformation() {
            return this.transformation;
        }

        @Override
        public ItemModel.Unbaked sourceModel() {
            return this.sourceModel;
        }
    }
}

