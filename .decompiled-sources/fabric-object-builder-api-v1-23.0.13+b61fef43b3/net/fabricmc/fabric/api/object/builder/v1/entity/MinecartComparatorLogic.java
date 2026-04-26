/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.object.builder.v1.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.block.state.BlockState;

@FunctionalInterface
public interface MinecartComparatorLogic<T extends AbstractMinecart> {
    public int getComparatorValue(T var1, BlockState var2, BlockPos var3);
}

