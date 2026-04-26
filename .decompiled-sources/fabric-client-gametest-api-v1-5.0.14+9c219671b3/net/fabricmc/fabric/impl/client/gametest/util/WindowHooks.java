/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.gametest.util;

public interface WindowHooks {
    public int fabric_getRealWidth();

    public int fabric_getRealHeight();

    public int fabric_getRealFramebufferWidth();

    public int fabric_getRealFramebufferHeight();

    public void fabric_resetSize();

    public void fabric_resize(int var1, int var2);

    public void fabric_focus();
}

