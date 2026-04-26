/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface EntityProcessor {
    public static final EntityProcessor NOP = input -> input;

    public @Nullable Entity process(Entity var1);
}

