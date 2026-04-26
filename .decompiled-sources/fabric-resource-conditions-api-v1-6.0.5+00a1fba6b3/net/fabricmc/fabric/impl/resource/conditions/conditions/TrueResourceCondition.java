/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.resource.conditions.conditions;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.impl.resource.conditions.DefaultResourceConditionTypes;
import org.jspecify.annotations.Nullable;

public class TrueResourceCondition
implements ResourceCondition {
    public static final MapCodec<TrueResourceCondition> CODEC = MapCodec.unit(TrueResourceCondition::new);

    @Override
    public ResourceConditionType<?> getType() {
        return DefaultResourceConditionTypes.TRUE;
    }

    @Override
    public boolean test( @Nullable RegistryOps.RegistryInfoLookup registryInfo) {
        return true;
    }
}

