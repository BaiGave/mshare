/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.rendering.v1;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.impl.client.rendering.SpriteSourceRegistryImpl;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.resources.Identifier;

public final class SpriteSourceRegistry {
    private SpriteSourceRegistry() {
    }

    public static void register(Identifier id, MapCodec<? extends SpriteSource> codec) {
        SpriteSourceRegistryImpl.register(id, codec);
    }
}

