/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.renderer.v1.sprite;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadView;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface SpriteFinder {
    public TextureAtlasSprite find(QuadView var1);

    public TextureAtlasSprite find(float var1, float var2);
}

