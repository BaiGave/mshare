/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.spectator;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.spectator.SpectatorMenu;

@Environment(value=EnvType.CLIENT)
public interface SpectatorMenuListener {
    public void onSpectatorMenuClosed(SpectatorMenu var1);
}

