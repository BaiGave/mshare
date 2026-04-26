/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.content.registry;

import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.world.level.block.state.BlockState;

public interface FireBlockHooks {
    public FlammableBlockRegistry.Entry fabric_getVanillaEntry(BlockState var1);
}

