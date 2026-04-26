/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.packs.resources;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.util.IdentifierPattern;

public class ResourceFilterSection {
    private static final Codec<ResourceFilterSection> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)Codec.list(IdentifierPattern.CODEC).fieldOf("block")).forGetter(o -> o.blockList)).apply((Applicative<ResourceFilterSection, ?>)i, ResourceFilterSection::new));
    public static final MetadataSectionType<ResourceFilterSection> TYPE = new MetadataSectionType<ResourceFilterSection>("filter", CODEC);
    private final List<IdentifierPattern> blockList;

    public ResourceFilterSection(List<IdentifierPattern> blockList) {
        this.blockList = List.copyOf(blockList);
    }

    public boolean isNamespaceFiltered(String namespace) {
        return this.blockList.stream().anyMatch(p -> p.namespacePredicate().test(namespace));
    }

    public boolean isPathFiltered(String path) {
        return this.blockList.stream().anyMatch(p -> p.pathPredicate().test(path));
    }
}

