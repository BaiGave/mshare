/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.datagen.v1.loot;

import com.google.common.base.Preconditions;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.impl.datagen.loot.ConditionEntityLootSubProvider;
import net.minecraft.data.loot.EntityLootSubProvider;

public interface FabricEntityLootSubProvider {
    default public EntityLootSubProvider withConditions(ResourceCondition ... conditions) {
        Preconditions.checkArgument(conditions.length > 0, "Must add at least one condition.");
        return new ConditionEntityLootSubProvider((EntityLootSubProvider)this, conditions);
    }
}

