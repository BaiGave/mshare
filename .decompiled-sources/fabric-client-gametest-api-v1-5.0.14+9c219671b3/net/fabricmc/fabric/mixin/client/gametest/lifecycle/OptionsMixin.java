/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.gametest.lifecycle;

import net.fabricmc.fabric.impl.client.gametest.context.ClientGameTestContextImpl;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Options.class})
public class OptionsMixin {
    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    private void onCreateGameOptions(CallbackInfo ci) {
        ClientGameTestContextImpl.initGameOptions((Options)((Object)this));
    }
}

