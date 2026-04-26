/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.datagen.loot;

import net.fabricmc.fabric.api.datagen.v1.loot.FabricEntityLootSubProvider;
import net.minecraft.data.loot.EntityLootSubProvider;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={EntityLootSubProvider.class})
public class EntityLootSubProviderMixin
implements FabricEntityLootSubProvider {
}

