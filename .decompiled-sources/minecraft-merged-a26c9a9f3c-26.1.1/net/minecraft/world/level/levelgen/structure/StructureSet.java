/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.structure;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

public record StructureSet(List<StructureSelectionEntry> structures, StructurePlacement placement) {
    public static final Codec<StructureSet> DIRECT_CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)StructureSelectionEntry.CODEC.listOf().fieldOf("structures")).forGetter(StructureSet::structures), ((MapCodec)StructurePlacement.CODEC.fieldOf("placement")).forGetter(StructureSet::placement)).apply((Applicative<StructureSet, ?>)i, StructureSet::new));
    public static final Codec<Holder<StructureSet>> CODEC = RegistryFileCodec.create(Registries.STRUCTURE_SET, DIRECT_CODEC);

    public StructureSet(Holder<Structure> singleEntry, StructurePlacement placement) {
        this(List.of(new StructureSelectionEntry(singleEntry, 1)), placement);
    }

    public static StructureSelectionEntry entry(Holder<Structure> structure, int weight) {
        return new StructureSelectionEntry(structure, weight);
    }

    public static StructureSelectionEntry entry(Holder<Structure> structure) {
        return new StructureSelectionEntry(structure, 1);
    }

    public record StructureSelectionEntry(Holder<Structure> structure, int weight) {
        public static final Codec<StructureSelectionEntry> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)Structure.CODEC.fieldOf("structure")).forGetter(StructureSelectionEntry::structure), ((MapCodec)ExtraCodecs.POSITIVE_INT.fieldOf("weight")).forGetter(StructureSelectionEntry::weight)).apply((Applicative<StructureSelectionEntry, ?>)i, StructureSelectionEntry::new));
    }
}

