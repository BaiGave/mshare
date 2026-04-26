/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.renderer.sprite;

import net.fabricmc.fabric.api.client.renderer.v1.sprite.FabricMaterialBaker;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={MaterialBaker.class})
interface MaterialBakerMixin
extends FabricMaterialBaker {
}

