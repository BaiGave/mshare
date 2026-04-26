/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.structure.pools.alias;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBinding;

@FunctionalInterface
public interface PoolAliasLookup {
    public static final PoolAliasLookup EMPTY = key -> key;

    public ResourceKey<StructureTemplatePool> lookup(ResourceKey<StructureTemplatePool> var1);

    public static PoolAliasLookup create(List<PoolAliasBinding> poolAliasBindings, BlockPos pos, long seed) {
        if (poolAliasBindings.isEmpty()) {
            return EMPTY;
        }
        RandomSource random = RandomSource.create(seed).forkPositional().at(pos);
        ImmutableMap.Builder builder = ImmutableMap.builder();
        poolAliasBindings.forEach(binding -> binding.forEachResolved(random, builder::put));
        ImmutableMap aliasMappings = builder.build();
        return resourceKey -> Objects.requireNonNull(aliasMappings.getOrDefault(resourceKey, resourceKey), () -> "alias " + String.valueOf(resourceKey.identifier()) + " was mapped to null value");
    }
}

