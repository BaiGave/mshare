/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

public interface Attackable {
    public @Nullable LivingEntity getLastAttacker();
}

