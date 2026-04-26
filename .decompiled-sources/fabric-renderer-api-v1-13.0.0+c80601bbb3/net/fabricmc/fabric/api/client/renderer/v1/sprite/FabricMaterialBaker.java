/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.renderer.v1.sprite;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadAtlas;
import net.fabricmc.fabric.api.client.renderer.v1.sprite.SpriteFinder;
import net.fabricmc.fabric.api.client.renderer.v1.sprite.SpriteFinderGetter;
import net.minecraft.resources.Identifier;

public interface FabricMaterialBaker
extends SpriteFinderGetter {
    default public SpriteFinder spriteFinder(Identifier atlasId) {
        throw new UnsupportedOperationException();
    }

    @Override
    default public SpriteFinder spriteFinder(QuadAtlas quadAtlas) {
        return this.spriteFinder(quadAtlas.getId());
    }
}

