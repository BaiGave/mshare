/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.content.registry;

import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.behavior.GiveGiftToHero;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={GiveGiftToHero.class})
public interface GiveGiftToHeroAccessor {
    @Accessor(value="GIFTS")
    public static Map<ResourceKey<VillagerProfession>, ResourceKey<LootTable>> fabric_getGifts() {
        throw new AssertionError((Object)"Untransformed @Accessor");
    }
}

