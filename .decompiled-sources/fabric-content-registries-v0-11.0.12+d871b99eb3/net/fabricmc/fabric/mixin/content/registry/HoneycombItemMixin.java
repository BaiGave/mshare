/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.content.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.world.item.HoneycombItem;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={HoneycombItem.class})
public class HoneycombItemMixin {
    @Inject(method={"lambda$static$0"}, at={@At(value="RETURN")}, cancellable=true)
    @Dynamic(value="lambda$static$0: Synthetic lambda body for Suppliers.memoize in initialization of WAXABLES")
    private static void createWaxables(CallbackInfoReturnable<BiMap> cir) {
        cir.setReturnValue(HashBiMap.create(cir.getReturnValue()));
    }
}

