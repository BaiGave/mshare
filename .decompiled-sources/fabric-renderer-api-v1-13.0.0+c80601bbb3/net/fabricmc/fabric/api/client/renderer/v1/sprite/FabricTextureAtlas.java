/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.renderer.v1.sprite;

import net.fabricmc.fabric.api.client.renderer.v1.sprite.SpriteFinder;

public interface FabricTextureAtlas {
    default public SpriteFinder spriteFinder() {
        throw new UnsupportedOperationException();
    }
}

