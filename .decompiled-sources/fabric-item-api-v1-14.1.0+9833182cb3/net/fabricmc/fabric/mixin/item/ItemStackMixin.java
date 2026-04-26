/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.item.v1.CustomDamageHandler;
import net.fabricmc.fabric.api.item.v1.FabricItemStack;
import net.fabricmc.fabric.impl.item.ItemComponentTooltipProviderRegistryImpl;
import net.fabricmc.fabric.impl.item.ItemExtensions;
import net.fabricmc.fabric.impl.item.VanillaTooltipProviderOrder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ItemStack.class})
public abstract class ItemStackMixin
implements FabricItemStack {
    @Shadow
    public abstract Item getItem();

    @Shadow
    public abstract void shrink(int var1);

    @WrapOperation(method={"hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V")})
    private void hookDamage(ItemStack instance, int amount, ServerLevel serverLevel, ServerPlayer serverPlayer, Consumer<Item> consumer, Operation<Void> original, @Local(argsOnly=true) LivingEntity entity, @Local(argsOnly=true) EquipmentSlot slot) {
        CustomDamageHandler handler = ((ItemExtensions)((Object)this.getItem())).fabric_getCustomDamageHandler();
        if (handler != null && !entity.hasInfiniteMaterials()) {
            MutableBoolean mut = new MutableBoolean(false);
            amount = handler.hurtAndBreak((ItemStack)((Object)this), amount, entity, slot, () -> {
                mut.setTrue();
                this.shrink(1);
                consumer.accept(this.getItem());
            });
            if (mut.booleanValue()) {
                return;
            }
        }
        original.call(instance, amount, serverLevel, serverPlayer, consumer);
    }

    @ModifyArg(method={"addDetailsToTooltip"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/item/ItemStack;addToTooltip(Lnet/minecraft/core/component/DataComponentType;Lnet/minecraft/world/item/Item$TooltipContext;Lnet/minecraft/world/item/component/TooltipDisplay;Ljava/util/function/Consumer;Lnet/minecraft/world/item/TooltipFlag;)V"))
    private DataComponentType<?> preAppendComponentTooltip(DataComponentType<?> componentType, @Local(argsOnly=true) Item.TooltipContext context, @Local(argsOnly=true) TooltipDisplay displayComponent, @Local(argsOnly=true) TooltipFlag type, @Local(argsOnly=true) Consumer<Component> componentConsumer, @Share(value="index") LocalIntRef index) {
        this.preAppendTooltip(componentType, context, displayComponent, componentConsumer, type, index);
        return componentType;
    }

    @ModifyArg(method={"addDetailsToTooltip"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/item/component/TooltipDisplay;shows(Lnet/minecraft/core/component/DataComponentType;)Z"))
    private DataComponentType<?> preShouldDisplay(DataComponentType<?> componentType, @Local(argsOnly=true) Item.TooltipContext context, @Local(argsOnly=true) TooltipDisplay displayComponent, @Local(argsOnly=true) TooltipFlag type, @Local(argsOnly=true) Consumer<Component> componentConsumer, @Share(value="index") LocalIntRef index) {
        this.preAppendTooltip(componentType, context, displayComponent, componentConsumer, type, index);
        return componentType;
    }

    @Inject(method={"addDetailsToTooltip"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/item/ItemStack;addAttributeTooltips(Ljava/util/function/Consumer;Lnet/minecraft/world/item/component/TooltipDisplay;Lnet/minecraft/world/entity/player/Player;)V")})
    private void preAttributeModifiers(Item.TooltipContext context, TooltipDisplay displayComponent, @Nullable Player player, TooltipFlag type, Consumer<Component> componentConsumer, CallbackInfo ci, @Share(value="index") LocalIntRef index) {
        this.preAppendTooltip(DataComponents.ATTRIBUTE_MODIFIERS, context, displayComponent, componentConsumer, type, index);
    }

    @Inject(method={"addDetailsToTooltip"}, at={@At(value="INVOKE", target="Lnet/minecraft/core/DefaultedRegistry;getKey(Ljava/lang/Object;)Lnet/minecraft/resources/Identifier;")})
    private void postTooltipsAdvanced(Item.TooltipContext context, TooltipDisplay displayComponent, @Nullable Player player, TooltipFlag type, Consumer<Component> componentConsumer, CallbackInfo ci, @Share(value="index") LocalIntRef index) {
        this.preAppendTooltip(null, context, displayComponent, componentConsumer, type, index);
    }

    @ModifyExpressionValue(method={"addDetailsToTooltip"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/item/TooltipFlag;isAdvanced()Z")})
    private boolean postTooltipsNonAdvanced(boolean isAdvanced, Item.TooltipContext context, TooltipDisplay displayComponent, @Nullable Player player, TooltipFlag type, Consumer<Component> componentConsumer, @Share(value="index") LocalIntRef index) {
        if (!isAdvanced) {
            this.preAppendTooltip(null, context, displayComponent, componentConsumer, type, index);
        }
        return isAdvanced;
    }

    @Unique
    private void preAppendTooltip(@Nullable DataComponentType<?> componentType, Item.TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> componentConsumer, TooltipFlag tooltipFlag, LocalIntRef index) {
        DataComponentType<?> componentInOrder;
        if (!ItemComponentTooltipProviderRegistryImpl.hasModdedEntries()) {
            return;
        }
        if (index.get() == 0) {
            ItemComponentTooltipProviderRegistryImpl.onFirst((ItemStack)((Object)this), context, displayComponent, componentConsumer, tooltipFlag);
        }
        List<DataComponentType<?>> vanillaOrder = VanillaTooltipProviderOrder.getVanillaOrder();
        if (index.get() > vanillaOrder.size()) {
            return;
        }
        do {
            HashSet cycleDetector;
            if (index.get() > 0) {
                DataComponentType<?> prevComponentInOrder = vanillaOrder.get(index.get() - 1);
                cycleDetector = new HashSet();
                cycleDetector.add(prevComponentInOrder);
                ItemComponentTooltipProviderRegistryImpl.onAfter((ItemStack)((Object)this), prevComponentInOrder, context, displayComponent, componentConsumer, tooltipFlag, cycleDetector);
            }
            if (index.get() == vanillaOrder.size()) {
                index.set(index.get() + 1);
                break;
            }
            componentInOrder = vanillaOrder.get(index.get());
            cycleDetector = new HashSet();
            cycleDetector.add(componentInOrder);
            ItemComponentTooltipProviderRegistryImpl.onBefore((ItemStack)((Object)this), componentInOrder, context, displayComponent, componentConsumer, tooltipFlag, cycleDetector);
            index.set(index.get() + 1);
        } while (componentInOrder != componentType);
        if (componentType == null) {
            ItemComponentTooltipProviderRegistryImpl.onLast((ItemStack)((Object)this), context, displayComponent, componentConsumer, tooltipFlag);
        }
    }
}

