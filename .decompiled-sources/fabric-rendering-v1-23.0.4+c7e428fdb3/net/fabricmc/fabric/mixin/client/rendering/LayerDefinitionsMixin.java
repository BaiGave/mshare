/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.rendering;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.Map;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.impl.client.rendering.ModelLayerImpl;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={LayerDefinitions.class})
abstract class LayerDefinitionsMixin {
    LayerDefinitionsMixin() {
    }

    @Inject(method={"createRoots"}, at={@At(value="INVOKE", target="Lcom/google/common/collect/ImmutableMap$Builder;build()Lcom/google/common/collect/ImmutableMap;")})
    private static void registerExtraModelData(CallbackInfoReturnable<Map<ModelLayerLocation, LayerDefinition>> info, @Local(name={"result"}) ImmutableMap.Builder<ModelLayerLocation, LayerDefinition> result) {
        for (Map.Entry<ModelLayerLocation, ModelLayerRegistry.TexturedLayerDefinitionProvider> entry : ModelLayerImpl.PROVIDERS.entrySet()) {
            result.put(entry.getKey(), entry.getValue().createLayerDefinition());
        }
        for (Map.Entry<Record, Object> entry : ModelLayerImpl.ARMOR_PROVIDERS.entrySet()) {
            ((ArmorModelSet)entry.getKey()).putFrom(((ModelLayerRegistry.TexturedArmorModelSetProvider)entry.getValue()).createArmorModelSet(), result);
        }
    }
}

