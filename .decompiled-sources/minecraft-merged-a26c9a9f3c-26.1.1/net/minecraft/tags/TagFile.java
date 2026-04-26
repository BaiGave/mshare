/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.tags;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.tags.TagEntry;

public record TagFile(List<TagEntry> entries, boolean replace) {
    public static final Codec<TagFile> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)TagEntry.CODEC.listOf().fieldOf("values")).forGetter(TagFile::entries), Codec.BOOL.optionalFieldOf("replace", false).forGetter(TagFile::replace)).apply((Applicative<TagFile, ?>)i, TagFile::new));
}

