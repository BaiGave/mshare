/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.model.loading;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.fabricmc.fabric.api.client.model.loading.v1.BlockStateResolver;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedExtraModel;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelLoadingPluginContextImpl
implements ModelLoadingPlugin.Context {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelLoadingPluginContextImpl.class);
    final Map<Block, BlockStateResolver> blockStateResolvers = new IdentityHashMap<Block, BlockStateResolver>();
    final Map<ExtraModelKey<?>, UnbakedExtraModel<?>> extraModels = new HashMap();
    private static final Identifier[] MODEL_MODIFIER_PHASES = new Identifier[]{ModelModifier.OVERRIDE_PHASE, ModelModifier.DEFAULT_PHASE, ModelModifier.WRAP_PHASE, ModelModifier.WRAP_LAST_PHASE};
    private final Event<ModelModifier.OnLoad> onLoadModifiers = EventFactory.createWithPhases(ModelModifier.OnLoad.class, modifiers -> (model, context) -> {
        for (ModelModifier.OnLoad modifier : modifiers) {
            try {
                model = modifier.modifyModelOnLoad(model, context);
            }
            catch (Exception exception) {
                LOGGER.error("Failed to modify unbaked model on load", exception);
            }
        }
        return model;
    }, MODEL_MODIFIER_PHASES);
    private final Event<ModelModifier.OnLoadBlock> onLoadBlockModifiers = EventFactory.createWithPhases(ModelModifier.OnLoadBlock.class, modifiers -> (model, context) -> {
        for (ModelModifier.OnLoadBlock modifier : modifiers) {
            try {
                model = modifier.modifyModelOnLoad(model, context);
            }
            catch (Exception exception) {
                LOGGER.error("Failed to modify unbaked block model on load", exception);
            }
        }
        return model;
    }, MODEL_MODIFIER_PHASES);
    private final Event<ModelModifier.BeforeBakeBlock> beforeBakeBlockModifiers = EventFactory.createWithPhases(ModelModifier.BeforeBakeBlock.class, modifiers -> (model, context) -> {
        for (ModelModifier.BeforeBakeBlock modifier : modifiers) {
            try {
                model = modifier.modifyModelBeforeBake(model, context);
            }
            catch (Exception exception) {
                LOGGER.error("Failed to modify unbaked block model before bake", exception);
            }
        }
        return model;
    }, MODEL_MODIFIER_PHASES);
    private final Event<ModelModifier.AfterBakeBlock> afterBakeBlockModifiers = EventFactory.createWithPhases(ModelModifier.AfterBakeBlock.class, modifiers -> (model, context) -> {
        for (ModelModifier.AfterBakeBlock modifier : modifiers) {
            try {
                model = modifier.modifyModelAfterBake(model, context);
            }
            catch (Exception exception) {
                LOGGER.error("Failed to modify baked block model after bake", exception);
            }
        }
        return model;
    }, MODEL_MODIFIER_PHASES);
    private final Event<ModelModifier.BeforeBakeItem> beforeBakeItemModifiers = EventFactory.createWithPhases(ModelModifier.BeforeBakeItem.class, modifiers -> (model, context) -> {
        for (ModelModifier.BeforeBakeItem modifier : modifiers) {
            try {
                model = modifier.modifyModelBeforeBake(model, context);
            }
            catch (Exception exception) {
                LOGGER.error("Failed to modify unbaked item model before bake", exception);
            }
        }
        return model;
    }, MODEL_MODIFIER_PHASES);
    private final Event<ModelModifier.AfterBakeItem> afterBakeItemModifiers = EventFactory.createWithPhases(ModelModifier.AfterBakeItem.class, modifiers -> (model, context) -> {
        for (ModelModifier.AfterBakeItem modifier : modifiers) {
            try {
                model = modifier.modifyModelAfterBake(model, context);
            }
            catch (Exception exception) {
                LOGGER.error("Failed to modify baked item model after bake", exception);
            }
        }
        return model;
    }, MODEL_MODIFIER_PHASES);

    @Override
    public void registerBlockStateResolver(Block block, BlockStateResolver resolver) {
        Objects.requireNonNull(block, "block cannot be null");
        Objects.requireNonNull(resolver, "resolver cannot be null");
        Optional<ResourceKey<Block>> optionalKey = BuiltInRegistries.BLOCK.getResourceKey(block);
        if (optionalKey.isEmpty()) {
            throw new IllegalArgumentException("Received unregistered block");
        }
        if (this.blockStateResolvers.put(block, resolver) != null) {
            throw new IllegalArgumentException("Duplicate block state resolver for " + String.valueOf(block));
        }
    }

    @Override
    public <T> void addModel(ExtraModelKey<T> key, UnbakedExtraModel<T> model) {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(model, "model cannot be null");
        if (this.extraModels.putIfAbsent(key, model) != null) {
            throw new IllegalArgumentException("Already have a model for this key");
        }
    }

    @Override
    public Event<ModelModifier.OnLoad> modifyModelOnLoad() {
        return this.onLoadModifiers;
    }

    @Override
    public Event<ModelModifier.OnLoadBlock> modifyBlockModelOnLoad() {
        return this.onLoadBlockModifiers;
    }

    @Override
    public Event<ModelModifier.BeforeBakeBlock> modifyBlockModelBeforeBake() {
        return this.beforeBakeBlockModifiers;
    }

    @Override
    public Event<ModelModifier.AfterBakeBlock> modifyBlockModelAfterBake() {
        return this.afterBakeBlockModifiers;
    }

    @Override
    public Event<ModelModifier.BeforeBakeItem> modifyItemModelBeforeBake() {
        return this.beforeBakeItemModifiers;
    }

    @Override
    public Event<ModelModifier.AfterBakeItem> modifyItemModelAfterBake() {
        return this.afterBakeItemModifiers;
    }
}

