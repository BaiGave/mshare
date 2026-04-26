/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.resource.client;

import net.fabricmc.fabric.impl.resource.client.PackTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ClientTooltipComponent.class})
public interface ClientTooltipComponentMixin {
    @Inject(method={"create(Lnet/minecraft/world/inventory/tooltip/TooltipComponent;)Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;"}, at={@At(value="HEAD")}, cancellable=true)
    private static void onCreate(TooltipComponent tooltipComponent, CallbackInfoReturnable<ClientTooltipComponent> cir) {
        if (tooltipComponent instanceof PackTooltipComponent) {
            PackTooltipComponent packTooltipComponent = (PackTooltipComponent)tooltipComponent;
            cir.setReturnValue(packTooltipComponent);
        }
    }
}

