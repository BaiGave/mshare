/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.gametest.screenshot;

public interface NativeImageHooks {
    public byte[] fabric_copyPixelsLuminance();

    public int[] fabric_copyPixelsRgb();

    public boolean fabric_isFullyOpaque();
}

