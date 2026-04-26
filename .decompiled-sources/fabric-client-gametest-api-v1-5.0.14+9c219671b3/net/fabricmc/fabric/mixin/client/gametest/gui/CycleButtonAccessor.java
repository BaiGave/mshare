/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.gametest.gui;

import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={CycleButton.class})
public interface CycleButtonAccessor {
    @Accessor
    public Component getName();
}

