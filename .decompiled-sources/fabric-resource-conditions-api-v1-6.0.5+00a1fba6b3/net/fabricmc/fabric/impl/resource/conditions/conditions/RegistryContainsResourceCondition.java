/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.resource.conditions.conditions;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.impl.resource.conditions.DefaultResourceConditionTypes;
import net.fabricmc.fabric.impl.resource.conditions.ResourceConditionsImpl;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

public record RegistryContainsResourceCondition(Identifier registry, List<Identifier> entries) implements ResourceCondition
{
    public static final MapCodec<RegistryContainsResourceCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Identifier.CODEC.fieldOf("registry")).orElse(Registries.ITEM.identifier()).forGetter(RegistryContainsResourceCondition::registry), ((MapCodec)Identifier.CODEC.listOf().fieldOf("values")).forGetter(RegistryContainsResourceCondition::entries)).apply((Applicative<RegistryContainsResourceCondition, ?>)instance, RegistryContainsResourceCondition::new));

    public RegistryContainsResourceCondition(Identifier registry, Identifier ... entries) {
        this(registry, List.of(entries));
    }

    @SafeVarargs
    public <T> RegistryContainsResourceCondition(ResourceKey<T> ... entries) {
        this(entries[0].registry(), Arrays.stream(entries).map(ResourceKey::identifier).toList());
    }

    @Override
    public ResourceConditionType<?> getType() {
        return DefaultResourceConditionTypes.REGISTRY_CONTAINS;
    }

    @Override
    public boolean test( @Nullable RegistryOps.RegistryInfoLookup registryInfo) {
        return ResourceConditionsImpl.registryContains(registryInfo, this.registry(), this.entries());
    }
}

