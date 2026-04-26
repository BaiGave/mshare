/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.resource.conditions.conditions;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.impl.resource.conditions.DefaultResourceConditionTypes;
import org.jspecify.annotations.Nullable;

public record NotResourceCondition(ResourceCondition condition) implements ResourceCondition
{
    public static final MapCodec<NotResourceCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)ResourceCondition.CODEC.fieldOf("value")).forGetter(NotResourceCondition::condition)).apply((Applicative<NotResourceCondition, ?>)instance, NotResourceCondition::new));

    @Override
    public ResourceConditionType<?> getType() {
        return DefaultResourceConditionTypes.NOT;
    }

    @Override
    public boolean test( @Nullable RegistryOps.RegistryInfoLookup registryInfo) {
        return !this.condition().test(registryInfo);
    }
}

