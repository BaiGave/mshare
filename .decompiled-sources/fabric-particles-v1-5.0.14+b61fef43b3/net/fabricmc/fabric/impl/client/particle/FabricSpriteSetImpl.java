/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.particle;

import java.util.List;
import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleResources;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.util.RandomSource;

public record FabricSpriteSetImpl(ParticleResources.MutableSpriteSet delegate) implements FabricSpriteSet
{
    @Override
    public TextureAtlas getAtlas() {
        return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.PARTICLES);
    }

    @Override
    public List<TextureAtlasSprite> getSprites() {
        return this.delegate.sprites;
    }

    @Override
    public TextureAtlasSprite get(int i, int j) {
        return this.delegate.get(i, j);
    }

    @Override
    public TextureAtlasSprite get(RandomSource random) {
        return this.delegate.get(random);
    }

    @Override
    public TextureAtlasSprite first() {
        return this.delegate.first();
    }
}

