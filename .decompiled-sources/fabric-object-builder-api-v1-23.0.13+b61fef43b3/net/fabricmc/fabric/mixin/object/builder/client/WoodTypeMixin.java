/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.object.builder.client;

import net.fabricmc.fabric.impl.object.builder.client.SignTypeTextureHelper;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={WoodType.class})
abstract class WoodTypeMixin {
    WoodTypeMixin() {
    }

    @Inject(method={"register"}, at={@At(value="RETURN")})
    private static void onReturnRegister(WoodType type, CallbackInfoReturnable<WoodType> cir) {
        if (SignTypeTextureHelper.shouldAddTextures) {
            Identifier identifier = Identifier.parse(type.name());
            Sheets.SIGN_SPRITES.put(type, Sheets.SIGN_MAPPER.apply(identifier));
            Sheets.HANGING_SIGN_SPRITES.put(type, Sheets.HANGING_SIGN_MAPPER.apply(identifier));
        }
    }
}

