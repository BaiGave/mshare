/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.rendering;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={SpriteSources.class})
public interface SpriteSourcesAccessor {
    @Accessor(value="ID_MAPPER")
    public static ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends SpriteSource>> getSpriteSourceCodecs() {
        throw new AssertionError();
    }
}

