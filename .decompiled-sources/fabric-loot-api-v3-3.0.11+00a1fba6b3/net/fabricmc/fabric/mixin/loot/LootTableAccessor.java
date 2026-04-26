/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.loot;

import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={LootTable.class})
public interface LootTableAccessor {
    @Accessor(value="pools")
    public List<LootPool> fabric_getPools();

    @Accessor(value="functions")
    public List<LootItemFunction> fabric_getFunctions();

    @Accessor(value="randomSequence")
    public Optional<Identifier> fabric_getRandomSequence();
}

