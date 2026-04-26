/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.renderer;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadView;
import net.fabricmc.fabric.api.client.renderer.v1.sprite.SpriteFinder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public record MissingSpriteFinderImpl(TextureAtlasSprite missingSprite) implements SpriteFinder
{
    @Override
    public TextureAtlasSprite find(QuadView quad) {
        return this.missingSprite;
    }

    @Override
    public TextureAtlasSprite find(float u, float v) {
        return this.missingSprite;
    }
}

