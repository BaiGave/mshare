/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.renderer.v1.mesh;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public enum QuadAtlas {
    BLOCK(TextureAtlas.LOCATION_BLOCKS, AtlasIds.BLOCKS),
    ITEM(TextureAtlas.LOCATION_ITEMS, AtlasIds.ITEMS);

    private final Identifier textureLocation;
    private final Identifier id;

    private QuadAtlas(Identifier textureLocation, Identifier id) {
        this.textureLocation = textureLocation;
        this.id = id;
    }

    public static @Nullable QuadAtlas ofLocation(Identifier atlasTextureLocation) {
        if (atlasTextureLocation.equals(TextureAtlas.LOCATION_BLOCKS)) {
            return BLOCK;
        }
        if (atlasTextureLocation.equals(TextureAtlas.LOCATION_ITEMS)) {
            return ITEM;
        }
        return null;
    }

    public static @Nullable QuadAtlas ofId(Identifier atlasId) {
        if (atlasId.equals(AtlasIds.BLOCKS)) {
            return BLOCK;
        }
        if (atlasId.equals(AtlasIds.ITEMS)) {
            return ITEM;
        }
        return null;
    }

    public Identifier getTextureLocation() {
        return this.textureLocation;
    }

    public Identifier getId() {
        return this.id;
    }
}

