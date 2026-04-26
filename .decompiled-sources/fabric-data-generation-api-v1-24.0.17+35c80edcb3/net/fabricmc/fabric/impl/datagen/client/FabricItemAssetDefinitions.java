/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.datagen.client;

import java.util.Set;
import net.fabricmc.fabric.impl.datagen.client.FabricModelProviderDefinitions;
import net.minecraft.world.level.block.Block;

public interface FabricItemAssetDefinitions
extends FabricModelProviderDefinitions {
    public void fabric_setProcessedBlocks(Set<Block> var1);
}

