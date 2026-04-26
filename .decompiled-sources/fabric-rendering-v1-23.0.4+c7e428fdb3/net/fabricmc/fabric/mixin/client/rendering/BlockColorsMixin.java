/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.rendering;

import net.fabricmc.fabric.impl.client.rendering.BlockColorRegistryImpl;
import net.minecraft.client.color.block.BlockColors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={BlockColors.class})
abstract class BlockColorsMixin {
    BlockColorsMixin() {
    }

    @Inject(method={"createDefault"}, at={@At(value="RETURN")})
    private static void create(CallbackInfoReturnable<BlockColors> ci) {
        BlockColorRegistryImpl.initialize(ci.getReturnValue());
    }
}

