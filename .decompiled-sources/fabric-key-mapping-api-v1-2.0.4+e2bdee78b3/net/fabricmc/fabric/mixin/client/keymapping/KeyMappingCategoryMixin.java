/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.keymapping;

import java.util.List;
import net.fabricmc.fabric.impl.client.keymapping.CategoryComparator;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={KeyMapping.Category.class})
abstract class KeyMappingCategoryMixin {
    @Shadow
    @Final
    private static List<KeyMapping.Category> SORT_ORDER;

    KeyMappingCategoryMixin() {
    }

    @Inject(method={"register(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/KeyMapping$Category;"}, at={@At(value="RETURN")})
    private static void onReturnRegister(Identifier id, CallbackInfoReturnable<KeyMapping.Category> cir) {
        SORT_ORDER.sort(CategoryComparator.INSTANCE);
    }
}

