/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.rendering;

import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.ArmorModelSet;

public final class ModelLayerImpl {
    public static final Map<ModelLayerLocation, ModelLayerRegistry.TexturedLayerDefinitionProvider> PROVIDERS = new HashMap<ModelLayerLocation, ModelLayerRegistry.TexturedLayerDefinitionProvider>();
    public static final Map<ArmorModelSet<ModelLayerLocation>, ModelLayerRegistry.TexturedArmorModelSetProvider> ARMOR_PROVIDERS = new HashMap<ArmorModelSet<ModelLayerLocation>, ModelLayerRegistry.TexturedArmorModelSetProvider>();

    private ModelLayerImpl() {
    }
}

