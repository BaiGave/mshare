/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.resource.conditions.conditions;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.impl.resource.conditions.DefaultResourceConditionTypes;
import net.fabricmc.fabric.impl.resource.conditions.ResourceConditionsImpl;
import org.jspecify.annotations.Nullable;

public record AnyModsLoadedResourceCondition(List<String> modIds) implements ResourceCondition
{
    public static final MapCodec<AnyModsLoadedResourceCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.STRING.listOf().fieldOf("values")).forGetter(AnyModsLoadedResourceCondition::modIds)).apply((Applicative<AnyModsLoadedResourceCondition, ?>)instance, AnyModsLoadedResourceCondition::new));

    @Override
    public ResourceConditionType<?> getType() {
        return DefaultResourceConditionTypes.ANY_MODS_LOADED;
    }

    @Override
    public boolean test( @Nullable RegistryOps.RegistryInfoLookup registryInfo) {
        return ResourceConditionsImpl.modsLoaded(this.modIds(), false);
    }
}

