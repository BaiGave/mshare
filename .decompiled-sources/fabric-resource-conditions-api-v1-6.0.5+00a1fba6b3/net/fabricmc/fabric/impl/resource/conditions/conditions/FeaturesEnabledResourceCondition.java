/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.resource.conditions.conditions;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.List;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.impl.resource.conditions.DefaultResourceConditionTypes;
import net.fabricmc.fabric.impl.resource.conditions.ResourceConditionsImpl;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlags;
import org.jspecify.annotations.Nullable;

public record FeaturesEnabledResourceCondition(Collection<Identifier> features) implements ResourceCondition
{
    public static final MapCodec<FeaturesEnabledResourceCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Identifier.CODEC.listOf().fieldOf("features")).forGetter(condition -> List.copyOf(condition.features))).apply((Applicative<FeaturesEnabledResourceCondition, ?>)instance, FeaturesEnabledResourceCondition::new));

    public FeaturesEnabledResourceCondition(Identifier ... features) {
        this(List.of(features));
    }

    public FeaturesEnabledResourceCondition(FeatureFlag ... flags) {
        this(FeatureFlags.REGISTRY.toNames(FeatureFlags.REGISTRY.subset(flags)));
    }

    @Override
    public ResourceConditionType<?> getType() {
        return DefaultResourceConditionTypes.FEATURES_ENABLED;
    }

    @Override
    public boolean test( @Nullable RegistryOps.RegistryInfoLookup registryInfo) {
        return ResourceConditionsImpl.featuresEnabled(this.features());
    }
}

