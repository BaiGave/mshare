/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.datagen.loot;

import net.fabricmc.fabric.api.datagen.v1.loot.FabricBlockLootSubProvider;
import net.minecraft.data.loot.BlockLootSubProvider;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={BlockLootSubProvider.class})
public class BlockLootSubProviderMixin
implements FabricBlockLootSubProvider {
}

