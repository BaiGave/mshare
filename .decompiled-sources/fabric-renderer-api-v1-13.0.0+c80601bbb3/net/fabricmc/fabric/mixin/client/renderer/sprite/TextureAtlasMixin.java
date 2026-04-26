/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.renderer.sprite;

import java.util.Map;
import net.fabricmc.fabric.api.client.renderer.v1.sprite.FabricTextureAtlas;
import net.fabricmc.fabric.api.client.renderer.v1.sprite.SpriteFinder;
import net.fabricmc.fabric.impl.client.renderer.SpriteFinderImpl;
import net.fabricmc.fabric.impl.client.renderer.SpriteLoaderPreparationsExtension;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={TextureAtlas.class})
abstract class TextureAtlasMixin
implements FabricTextureAtlas {
    @Shadow
    private Map<Identifier, TextureAtlasSprite> texturesByName;
    @Shadow
    private @Nullable TextureAtlasSprite missingSprite;
    @Unique
    private volatile @Nullable SpriteFinder spriteFinder;

    TextureAtlasMixin() {
    }

    @Inject(at={@At(value="RETURN")}, method={"upload"})
    private void uploadHook(SpriteLoader.Preparations stitchResult, CallbackInfo ci) {
        this.spriteFinder = ((SpriteLoaderPreparationsExtension)((Object)stitchResult)).fabric_spriteFinderNullable();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public SpriteFinder spriteFinder() {
        SpriteFinder result = this.spriteFinder;
        if (result == null) {
            TextureAtlasMixin textureAtlasMixin = this;
            synchronized (textureAtlasMixin) {
                result = this.spriteFinder;
                if (result == null) {
                    if (this.missingSprite == null) {
                        throw new IllegalStateException("Tried to create sprite finder, but atlas is not initialized");
                    }
                    this.spriteFinder = result = new SpriteFinderImpl(this.texturesByName, this.missingSprite);
                }
            }
        }
        return result;
    }
}

