/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.variant;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.variant.SpawnCondition;
import net.minecraft.world.entity.variant.SpawnContext;
import net.minecraft.world.level.levelgen.structure.Structure;

public record StructureCheck(HolderSet<Structure> requiredStructures) implements SpawnCondition
{
    public static final MapCodec<StructureCheck> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)RegistryCodecs.homogeneousList(Registries.STRUCTURE).fieldOf("structures")).forGetter(StructureCheck::requiredStructures)).apply((Applicative<StructureCheck, ?>)i, StructureCheck::new));

    @Override
    public boolean test(SpawnContext context) {
        return context.level().getLevel().structureManager().getStructureWithPieceAt(context.pos(), this.requiredStructures).isValid();
    }

    public MapCodec<StructureCheck> codec() {
        return MAP_CODEC;
    }
}

