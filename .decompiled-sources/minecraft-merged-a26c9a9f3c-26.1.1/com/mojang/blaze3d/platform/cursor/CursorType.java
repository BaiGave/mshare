/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.platform.cursor;

import com.mojang.blaze3d.platform.Window;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.glfw.GLFW;

@Environment(value=EnvType.CLIENT)
public class CursorType {
    public static final CursorType DEFAULT = new CursorType("default", 0L);
    private final String name;
    private final long handle;

    private CursorType(String name, long handle) {
        this.name = name;
        this.handle = handle;
    }

    public void select(Window window) {
        GLFW.glfwSetCursor(window.handle(), this.handle);
    }

    public String toString() {
        return this.name;
    }

    public static CursorType createStandardCursor(int shape, String name, CursorType fallback) {
        long handle = GLFW.glfwCreateStandardCursor(shape);
        if (handle == 0L) {
            return fallback;
        }
        return new CursorType(name, handle);
    }
}

