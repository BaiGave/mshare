/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.content.registry;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.api.registry.FuelValueEvents;
import net.fabricmc.fabric.impl.content.registry.FuelRegistryEventsContextImpl;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.FuelValues;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={FuelValues.class})
public abstract class FuelValuesMixin {
    @WrapOperation(method={"vanillaBurnTimes(Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/world/flag/FeatureFlagSet;I)Lnet/minecraft/world/level/block/entity/FuelValues;"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/level/block/entity/FuelValues$Builder;remove(Lnet/minecraft/tags/TagKey;)Lnet/minecraft/world/level/block/entity/FuelValues$Builder;")}, allow=1)
    private static FuelValues.Builder build(FuelValues.Builder builder, TagKey<Item> tag, Operation<FuelValues.Builder> operation, @Local(argsOnly=true) HolderLookup.Provider registries, @Local(argsOnly=true) FeatureFlagSet features, @Local(argsOnly=true) int baseSmeltTime) {
        FuelRegistryEventsContextImpl context = new FuelRegistryEventsContextImpl(registries, features, baseSmeltTime);
        FuelValueEvents.BUILD.invoker().build(builder, context);
        operation.call(builder, tag);
        FuelValueEvents.EXCLUSIONS.invoker().buildExclusions(builder, context);
        return builder;
    }
}

