/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.object.builder.v1.block.entity;

import net.minecraft.world.level.block.Block;

public interface FabricBlockEntityType {
    default public void addValidBlock(Block block) {
        throw new AssertionError((Object)"Implemented in Mixin");
    }
}

