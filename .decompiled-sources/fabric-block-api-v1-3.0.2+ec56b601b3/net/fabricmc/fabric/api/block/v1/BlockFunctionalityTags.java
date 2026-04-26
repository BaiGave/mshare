/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.block.v1;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class BlockFunctionalityTags {
    public static final TagKey<Block> CAN_CLIMB_TRAPDOOR_ABOVE = BlockFunctionalityTags.create("can_climb_trapdoor_above");

    private BlockFunctionalityTags() {
    }

    private static TagKey<Block> create(String name) {
        return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("fabric", name));
    }
}

