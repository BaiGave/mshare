/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.damagesource;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.FallLocation;
import org.jspecify.annotations.Nullable;

public record CombatEntry(DamageSource source, float damage, @Nullable FallLocation fallLocation, float fallDistance) {
}

