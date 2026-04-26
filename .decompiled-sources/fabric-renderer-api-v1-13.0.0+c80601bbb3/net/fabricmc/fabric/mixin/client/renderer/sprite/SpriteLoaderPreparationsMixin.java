/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.renderer.sprite;

import java.util.Map;
import net.fabricmc.fabric.api.client.renderer.v1.sprite.FabricPreparations;
import net.fabricmc.fabric.api.client.renderer.v1.sprite.SpriteFinder;
import net.fabricmc.fabric.impl.client.renderer.SpriteFinderImpl;
import net.fabricmc.fabric.impl.client.renderer.SpriteLoaderPreparationsExtension;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={SpriteLoader.Preparations.class})
abstract class SpriteLoaderPreparationsMixin
implements FabricPreparations,
SpriteLoaderPreparationsExtension {
    @Shadow
    @Final
    private TextureAtlasSprite missing;
    @Shadow
    @Final
    private Map<Identifier, TextureAtlasSprite> regions;
    @Unique
    private volatile @Nullable SpriteFinder spriteFinder;

    SpriteLoaderPreparationsMixin() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public SpriteFinder spriteFinder() {
        SpriteFinder result = this.spriteFinder;
        if (result == null) {
            SpriteLoaderPreparationsMixin spriteLoaderPreparationsMixin = this;
            synchronized (spriteLoaderPreparationsMixin) {
                result = this.spriteFinder;
                if (result == null) {
                    this.spriteFinder = result = new SpriteFinderImpl(this.regions, this.missing);
                }
            }
        }
        return result;
    }

    @Override
    public @Nullable SpriteFinder fabric_spriteFinderNullable() {
        return this.spriteFinder;
    }
}

