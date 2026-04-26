/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.object.builder.client;

import net.fabricmc.fabric.impl.object.builder.client.SignTypeTextureHelper;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SpriteMapper;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Sheets.class})
abstract class SheetsMixin {
    SheetsMixin() {
    }

    @Inject(method={"<clinit>*"}, at={@At(value="RETURN")})
    private static void onReturnClinit(CallbackInfo ci) {
        SignTypeTextureHelper.shouldAddTextures = true;
    }

    @Redirect(method={"createSignSprite"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/renderer/SpriteMapper;defaultNamespaceApply(Ljava/lang/String;)Lnet/minecraft/client/resources/model/sprite/SpriteId;"))
    private static SpriteId redirectSignVanillaId(SpriteMapper instance, String name) {
        return instance.apply(Identifier.parse(name));
    }

    @Redirect(method={"createHangingSignSprite"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/renderer/SpriteMapper;defaultNamespaceApply(Ljava/lang/String;)Lnet/minecraft/client/resources/model/sprite/SpriteId;"))
    private static SpriteId redirectHangingVanillaId(SpriteMapper instance, String name) {
        return instance.apply(Identifier.parse(name));
    }
}

