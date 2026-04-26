/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.resource.conditions;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.data.DataProvider;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={DataProvider.class})
public interface DataProviderMixin {
    @Inject(method={"lambda$static$0"}, at={@At(value="HEAD")})
    @Dynamic(value="lambda method passed to Util.make")
    private static void fabric_injectResourceConditionsSortOrder(Object2IntOpenHashMap<String> map, CallbackInfo ci) {
        map.put("fabric:load_conditions", -100);
    }
}

