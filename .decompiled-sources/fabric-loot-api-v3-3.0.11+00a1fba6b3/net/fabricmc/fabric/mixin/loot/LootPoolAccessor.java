/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.loot;

import java.util.List;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={LootPool.class})
public interface LootPoolAccessor {
    @Accessor(value="rolls")
    public NumberProvider fabric_getRolls();

    @Accessor(value="bonusRolls")
    public NumberProvider fabric_getBonusRolls();

    @Accessor(value="entries")
    public List<LootPoolEntryContainer> fabric_getEntries();

    @Accessor(value="conditions")
    public List<LootItemCondition> fabric_getConditions();

    @Accessor(value="functions")
    public List<LootItemFunction> fabric_getFunctions();
}

