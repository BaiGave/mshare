/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.rendering.v1;

import java.util.Objects;
import net.fabricmc.fabric.impl.client.rendering.ModelLayerImpl;
import net.fabricmc.fabric.mixin.client.rendering.ModelLayersAccessor;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.ArmorModelSet;

public final class ModelLayerRegistry {
    public static void registerModelLayer(ModelLayerLocation modelLayer, TexturedLayerDefinitionProvider provider) {
        Objects.requireNonNull(modelLayer, "ModelLayerLocation cannot be null");
        Objects.requireNonNull(provider, "TexturedLayerDefinitionProvider cannot be null");
        if (ModelLayerImpl.PROVIDERS.putIfAbsent(modelLayer, provider) != null) {
            throw new IllegalArgumentException(String.format("Cannot replace registration for model layer \"%s\"", modelLayer));
        }
        ModelLayersAccessor.getLayers().add(modelLayer);
    }

    public static void registerArmorModelLayers(ArmorModelSet<ModelLayerLocation> armorModelSet, TexturedArmorModelSetProvider provider) {
        Objects.requireNonNull(armorModelSet, "ArmorModelSet cannot be null");
        Objects.requireNonNull(provider, "TexturedArmorModelSetProvider cannot be null");
        if (ModelLayerImpl.ARMOR_PROVIDERS.putIfAbsent(armorModelSet, provider) != null) {
            throw new IllegalArgumentException(String.format("Cannot replace registration for armor model layer \"%s\"", armorModelSet));
        }
        armorModelSet.map(ModelLayersAccessor.getLayers()::add);
    }

    private ModelLayerRegistry() {
    }

    @FunctionalInterface
    public static interface TexturedArmorModelSetProvider {
        public ArmorModelSet<LayerDefinition> createArmorModelSet();
    }

    @FunctionalInterface
    public static interface TexturedLayerDefinitionProvider {
        public LayerDefinition createLayerDefinition();
    }
}

