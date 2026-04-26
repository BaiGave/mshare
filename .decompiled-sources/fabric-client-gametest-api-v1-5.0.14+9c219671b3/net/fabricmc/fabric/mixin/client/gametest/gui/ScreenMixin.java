/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.gametest.gui;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={Screen.class})
public class ScreenMixin {
    @ModifyReturnValue(method={"panoramaShouldSpin"}, at={@At(value="RETURN")})
    private boolean disableRotatingPanoramaForClientGameTests(boolean original) {
        return false;
    }
}

