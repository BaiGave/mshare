/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.rendering;

import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ClientTooltipComponent.class})
public interface ClientTooltipComponentMixin {
    @Inject(method={"create(Lnet/minecraft/world/inventory/tooltip/TooltipComponent;)Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;"}, at={@At(value="HEAD")}, cancellable=true)
    private static void convertCustomTooltipComponent(TooltipComponent data, CallbackInfoReturnable<ClientTooltipComponent> cir) {
        ClientTooltipComponent component = ClientTooltipComponentCallback.EVENT.invoker().getClientComponent(data);
        if (component != null) {
            cir.setReturnValue(component);
        }
    }
}

