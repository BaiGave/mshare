/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.content.registry;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.impl.content.registry.VillagerInteractionRegistriesImpl;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={Villager.class})
public class VillagerMixin {
    @WrapOperation(method={"wantsToPickUp"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/tags/TagKey;)Z")})
    private boolean useGatherableItemsSet(ItemStack stack, TagKey<Item> tag, Operation<Boolean> original) {
        return VillagerInteractionRegistriesImpl.getGatherableItemRegistry().contains(stack.getItem()) || original.call(stack, tag) != false;
    }
}

