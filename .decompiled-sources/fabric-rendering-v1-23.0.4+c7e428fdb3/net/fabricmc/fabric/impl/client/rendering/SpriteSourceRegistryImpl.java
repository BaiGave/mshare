/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.rendering;

import com.mojang.serialization.MapCodec;
import java.util.Objects;
import net.fabricmc.fabric.mixin.client.rendering.SpriteSourcesAccessor;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.resources.Identifier;

public final class SpriteSourceRegistryImpl {
    private SpriteSourceRegistryImpl() {
    }

    public static void register(Identifier id, MapCodec<? extends SpriteSource> codec) {
        Objects.requireNonNull(id, "id must not be null!");
        Objects.requireNonNull(codec, "codec must not be null!");
        SpriteSourcesAccessor.getSpriteSourceCodecs().put(id, codec);
    }
}

