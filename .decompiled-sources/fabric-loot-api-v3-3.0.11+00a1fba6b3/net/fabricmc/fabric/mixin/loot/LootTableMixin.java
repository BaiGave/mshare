/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.loot;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.impl.loot.FabricLootTable;
import net.fabricmc.fabric.impl.loot.LootUtil;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={LootTable.class}, priority=3000)
class LootTableMixin
implements FabricLootTable {
    @Unique
    @Nullable Holder<LootTable> holder = null;

    LootTableMixin() {
    }

    @WrapMethod(method={"getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V"})
    private void fabric$modifyDrops(LootContext context, Consumer<ItemStack> lootConsumer, Operation<Void> original) {
        if (this.holder == null) {
            this.holder = LootUtil.getEntryOrDirect(context.getLevel(), (LootTable)((Object)this));
        }
        ObjectArrayList<ItemStack> list = new ObjectArrayList<ItemStack>();
        Object[] objectArray = new Object[2];
        objectArray[0] = context;
        objectArray[1] = list::add;
        original.call(objectArray);
        LootTableEvents.MODIFY_DROPS.invoker().modifyLootTableDrops(this.holder, context, list);
        list.forEach(lootConsumer);
    }

    @Override
    public void fabric$setHolder(Holder<LootTable> key) {
        this.holder = key;
    }
}

