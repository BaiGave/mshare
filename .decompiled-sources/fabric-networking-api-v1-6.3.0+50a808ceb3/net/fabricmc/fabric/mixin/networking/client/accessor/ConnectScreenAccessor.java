/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.networking.client.accessor;

import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ConnectScreen.class})
public interface ConnectScreenAccessor {
    @Accessor
    public Connection getConnection();
}

