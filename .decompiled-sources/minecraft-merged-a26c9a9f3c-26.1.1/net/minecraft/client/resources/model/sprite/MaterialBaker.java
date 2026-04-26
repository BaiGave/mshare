/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.resources.model.sprite;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.renderer.v1.sprite.FabricMaterialBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;

@Environment(value=EnvType.CLIENT)
public interface MaterialBaker
extends FabricMaterialBaker {
    public Material.Baked get(Material var1, ModelDebugName var2);

    public Material.Baked reportMissingReference(String var1, ModelDebugName var2);

    default public Material.Baked resolveSlot(TextureSlots slots, String id, ModelDebugName name) {
        Material resolvedMaterial = slots.getMaterial(id);
        return resolvedMaterial != null ? this.get(resolvedMaterial, name) : this.reportMissingReference(id, name);
    }
}

