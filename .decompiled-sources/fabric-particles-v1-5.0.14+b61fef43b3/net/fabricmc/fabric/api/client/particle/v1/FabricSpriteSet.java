/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.particle.v1;

import java.util.List;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public interface FabricSpriteSet
extends SpriteSet {
    public TextureAtlas getAtlas();

    public List<TextureAtlasSprite> getSprites();
}

