/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.content.registry;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.behavior.GiveGiftToHero;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={GiveGiftToHero.class})
public class GiveGiftToHeroMixin {
    @Shadow
    @Final
    @Mutable
    private static Map<VillagerProfession, ResourceKey<LootTable>> GIFTS;

    @Inject(method={"<clinit>"}, at={@At(value="TAIL")})
    private static void makeMutable(CallbackInfo ci) {
        GIFTS = new HashMap<VillagerProfession, ResourceKey<LootTable>>(GIFTS);
    }
}

