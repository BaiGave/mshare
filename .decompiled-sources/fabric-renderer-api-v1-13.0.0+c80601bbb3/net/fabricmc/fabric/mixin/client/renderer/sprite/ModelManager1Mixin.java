/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.renderer.sprite;

import net.fabricmc.fabric.api.client.renderer.v1.sprite.FabricMaterialBaker;
import net.fabricmc.fabric.api.client.renderer.v1.sprite.SpriteFinder;
import net.fabricmc.fabric.impl.client.renderer.MissingSpriteFinderImpl;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(targets={"net.minecraft.client.resources.model.ModelManager$1"})
abstract class ModelManager1Mixin
implements FabricMaterialBaker {
    @Shadow
    @Final
    private Material.Baked blockMissing;
    @Shadow
    @Final
    SpriteLoader.Preparations val$blockAtlas;
    @Shadow
    @Final
    SpriteLoader.Preparations val$itemAtlas;
    @Unique
    private volatile @Nullable MissingSpriteFinderImpl missingSpriteFinder;

    ModelManager1Mixin() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public SpriteFinder spriteFinder(Identifier atlasId) {
        if (atlasId.equals(AtlasIds.BLOCKS)) {
            return this.val$blockAtlas.spriteFinder();
        }
        if (atlasId.equals(AtlasIds.ITEMS)) {
            return this.val$itemAtlas.spriteFinder();
        }
        MissingSpriteFinderImpl result = this.missingSpriteFinder;
        if (result == null) {
            ModelManager1Mixin modelManager1Mixin = this;
            synchronized (modelManager1Mixin) {
                result = this.missingSpriteFinder;
                if (result == null) {
                    this.missingSpriteFinder = result = new MissingSpriteFinderImpl(this.blockMissing.sprite());
                }
            }
        }
        return result;
    }
}

