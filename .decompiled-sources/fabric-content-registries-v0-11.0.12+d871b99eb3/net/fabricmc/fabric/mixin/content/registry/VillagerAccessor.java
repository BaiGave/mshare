/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.content.registry;

import java.util.Map;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={Villager.class})
public interface VillagerAccessor {
    @Mutable
    @Accessor(value="FOOD_POINTS")
    public static void fabric_setItemFoodValues(Map<Item, Integer> items) {
        throw new AssertionError((Object)"Untransformed @Accessor");
    }
}

