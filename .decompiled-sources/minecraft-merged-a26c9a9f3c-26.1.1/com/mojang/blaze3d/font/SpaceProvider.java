/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.font;

import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.UnbakedGlyph;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.gui.font.providers.GlyphProviderDefinition;
import net.minecraft.client.gui.font.providers.GlyphProviderType;
import net.minecraft.util.ExtraCodecs;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SpaceProvider
implements GlyphProvider {
    private final Int2ObjectMap<EmptyGlyph> glyphs;

    public SpaceProvider(Map<Integer, Float> advances) {
        this.glyphs = new Int2ObjectOpenHashMap<EmptyGlyph>(advances.size());
        advances.forEach((codepoint, advance) -> this.glyphs.put((int)codepoint, new EmptyGlyph(advance.floatValue())));
    }

    @Override
    public @Nullable UnbakedGlyph getGlyph(int codepoint) {
        return (UnbakedGlyph)this.glyphs.get(codepoint);
    }

    @Override
    public IntSet getSupportedGlyphs() {
        return IntSets.unmodifiable(this.glyphs.keySet());
    }

    @Environment(value=EnvType.CLIENT)
    public record Definition(Map<Integer, Float> advances) implements GlyphProviderDefinition
    {
        public static final MapCodec<Definition> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)Codec.unboundedMap(ExtraCodecs.CODEPOINT, Codec.FLOAT).fieldOf("advances")).forGetter(Definition::advances)).apply((Applicative<Definition, ?>)i, Definition::new));

        @Override
        public GlyphProviderType type() {
            return GlyphProviderType.SPACE;
        }

        @Override
        public Either<GlyphProviderDefinition.Loader, GlyphProviderDefinition.Reference> unpack() {
            GlyphProviderDefinition.Loader loader = resourceManager -> new SpaceProvider(this.advances);
            return Either.left(loader);
        }
    }
}

