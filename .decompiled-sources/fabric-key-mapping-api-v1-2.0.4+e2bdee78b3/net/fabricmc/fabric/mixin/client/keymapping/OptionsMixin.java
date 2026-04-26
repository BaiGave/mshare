/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.keymapping;

import net.fabricmc.fabric.impl.client.keymapping.KeyMappingRegistryImpl;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Options.class})
public class OptionsMixin {
    @Mutable
    @Shadow
    @Final
    public KeyMapping[] keyMappings;

    @Inject(at={@At(value="HEAD")}, method={"load()V"})
    public void loadHook(CallbackInfo info) {
        this.keyMappings = KeyMappingRegistryImpl.process(this.keyMappings);
    }
}

