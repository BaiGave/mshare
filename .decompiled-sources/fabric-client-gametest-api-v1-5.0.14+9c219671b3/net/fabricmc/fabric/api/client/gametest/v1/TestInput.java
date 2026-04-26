/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.gametest.v1;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.Function;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface TestInput {
    public void holdKey(KeyMapping var1);

    public void holdKey(Function<Options, KeyMapping> var1);

    public void holdKey(InputConstants.Key var1);

    public void holdKey(int var1);

    public void holdMouse(int var1);

    public void holdControl();

    public void holdShift();

    public void holdAlt();

    public void releaseKey(KeyMapping var1);

    public void releaseKey(Function<Options, KeyMapping> var1);

    public void releaseKey(InputConstants.Key var1);

    public void releaseKey(int var1);

    public void releaseMouse(int var1);

    public void releaseControl();

    public void releaseShift();

    public void releaseAlt();

    public void pressKey(KeyMapping var1);

    public void pressKey(Function<Options, KeyMapping> var1);

    public void pressKey(InputConstants.Key var1);

    public void pressKey(int var1);

    public void pressMouse(int var1);

    public void holdKeyFor(KeyMapping var1, int var2);

    public void holdKeyFor(Function<Options, KeyMapping> var1, int var2);

    public void holdKeyFor(InputConstants.Key var1, int var2);

    public void holdKeyFor(int var1, int var2);

    public void holdMouseFor(int var1, int var2);

    public void typeChar(int var1);

    public void typeChars(String var1);

    public void scroll(double var1);

    public void scroll(double var1, double var3);

    public void setCursorPos(double var1, double var3);

    public void moveCursor(double var1, double var3);

    public void resizeWindow(int var1, int var2);
}

