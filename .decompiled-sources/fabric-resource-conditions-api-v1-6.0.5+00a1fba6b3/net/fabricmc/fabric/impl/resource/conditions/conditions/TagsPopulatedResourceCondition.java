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
import net.minecraft.tags.TagKey;
import org.jspecify.annotations.Nullable;

public record TagsPopulatedResourceCondition(Identifier registry, List<Identifier> tags) implements ResourceCondition
{
    public static final MapCodec<TagsPopulatedResourceCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Identifier.CODEC.fieldOf("registry")).orElse(Registries.ITEM.identifier()).forGetter(TagsPopulatedResourceCondition::registry), ((MapCodec)Identifier.CODEC.listOf().fieldOf("values")).forGetter(TagsPopulatedResourceCondition::tags)).apply((Applicative<TagsPopulatedResourceCondition, ?>)instance, TagsPopulatedResourceCondition::new));

    @SafeVarargs
    public <T> TagsPopulatedResourceCondition(Identifier registry, TagKey<T> ... tags) {
        this(registry, Arrays.stream(tags).map(TagKey::location).toList());
    }

    @SafeVarargs
    public <T> TagsPopulatedResourceCondition(TagKey<T> ... tags) {
        this(tags[0].registry().identifier(), Arrays.stream(tags).map(TagKey::location).toList());
    }

    @Override
    public ResourceConditionType<?> getType() {
        return DefaultResourceConditionTypes.TAGS_POPULATED;
    }

    @Override
    public boolean test( @Nullable RegistryOps.RegistryInfoLookup registryInfo) {
        return ResourceConditionsImpl.tagsPopulated(registryInfo, this.registry(), this.tags());
    }
}

