/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.resource.conditions.conditions;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.impl.resource.conditions.DefaultResourceConditionTypes;
import net.fabricmc.fabric.impl.resource.conditions.ResourceConditionsImpl;
import org.jspecify.annotations.Nullable;

public record OrResourceCondition(List<ResourceCondition> conditions) implements ResourceCondition
{
    public static final MapCodec<OrResourceCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)ResourceCondition.CODEC.listOf().fieldOf("values")).forGetter(OrResourceCondition::conditions)).apply((Applicative<OrResourceCondition, ?>)instance, OrResourceCondition::new));

    @Override
    public ResourceConditionType<?> getType() {
        return DefaultResourceConditionTypes.OR;
    }

    @Override
    public boolean test( @Nullable RegistryOps.RegistryInfoLookup registryInfo) {
        return ResourceConditionsImpl.conditionsMet(this.conditions(), registryInfo, false);
    }
}

