/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.font.providers;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.font.providers.GlyphProviderDefinition;
import net.minecraft.client.gui.font.providers.GlyphProviderType;
import net.minecraft.resources.Identifier;

@Environment(value=EnvType.CLIENT)
public record ProviderReferenceDefinition(Identifier id) implements GlyphProviderDefinition
{
    public static final MapCodec<ProviderReferenceDefinition> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)Identifier.CODEC.fieldOf("id")).forGetter(ProviderReferenceDefinition::id)).apply((Applicative<ProviderReferenceDefinition, ?>)i, ProviderReferenceDefinition::new));

    @Override
    public GlyphProviderType type() {
        return GlyphProviderType.REFERENCE;
    }

    @Override
    public Either<GlyphProviderDefinition.Loader, GlyphProviderDefinition.Reference> unpack() {
        return Either.right(new GlyphProviderDefinition.Reference(this.id));
    }
}

