/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.resource.conditions.v1;

import com.mojang.serialization.Codec;
import java.util.List;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import org.jspecify.annotations.Nullable;

public interface ResourceCondition {
    public static final Codec<ResourceCondition> CODEC = ResourceConditionType.TYPE_CODEC.dispatch("condition", ResourceCondition::getType, ResourceConditionType::codec);
    public static final Codec<List<ResourceCondition>> LIST_CODEC = CODEC.listOf();
    public static final Codec<ResourceCondition> CONDITION_CODEC = Codec.withAlternative(CODEC, LIST_CODEC, conditions -> ResourceConditions.and(conditions.toArray(new ResourceCondition[0])));

    public ResourceConditionType<?> getType();

    public boolean test( @Nullable RegistryOps.RegistryInfoLookup var1);
}

