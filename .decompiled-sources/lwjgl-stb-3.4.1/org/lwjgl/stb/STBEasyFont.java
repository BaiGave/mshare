/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.stb;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.stb.LibSTB;
import org.lwjgl.system.Checks;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;

public class STBEasyFont {
    protected STBEasyFont() {
        throw new UnsupportedOperationException();
    }

    public static native int nstb_easy_font_width(long var0);

    public static int stb_easy_font_width(@NativeType(value="char *") ByteBuffer text) {
        if (Checks.CHECKS) {
            Checks.checkNT1(text);
        }
        return STBEasyFont.nstb_easy_font_width(MemoryUtil.memAddress(text));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static int stb_easy_font_width(@NativeType(value="char *") CharSequence text) {
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            stack.nASCII(text, true);
            long textEncoded = stack.getPointerAddress();
            int n = STBEasyFont.nstb_easy_font_width(textEncoded);
            return n;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static native int nstb_easy_font_height(long var0);

    public static int stb_easy_font_height(@NativeType(value="char *") ByteBuffer text) {
        if (Checks.CHECKS) {
            Checks.checkNT1(text);
        }
        return STBEasyFont.nstb_easy_font_height(MemoryUtil.memAddress(text));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static int stb_easy_font_height(@NativeType(value="char *") CharSequence text) {
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            stack.nASCII(text, true);
            long textEncoded = stack.getPointerAddress();
            int n = STBEasyFont.nstb_easy_font_height(textEncoded);
            return n;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static native int nstb_easy_font_print(float var0, float var1, long var2, long var4, long var6, int var8);

    public static int stb_easy_font_print(float x, float y, @NativeType(value="char *") ByteBuffer text, @NativeType(value="unsigned char *") @Nullable ByteBuffer color, @NativeType(value="void *") ByteBuffer vertex_buffer) {
        if (Checks.CHECKS) {
            Checks.checkNT1(text);
            Checks.checkSafe((Buffer)color, 4);
        }
        return STBEasyFont.nstb_easy_font_print(x, y, MemoryUtil.memAddress(text), MemoryUtil.memAddressSafe(color), MemoryUtil.memAddress(vertex_buffer), vertex_buffer.remaining());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static int stb_easy_font_print(float x, float y, @NativeType(value="char *") CharSequence text, @NativeType(value="unsigned char *") @Nullable ByteBuffer color, @NativeType(value="void *") ByteBuffer vertex_buffer) {
        if (Checks.CHECKS) {
            Checks.checkSafe((Buffer)color, 4);
        }
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            stack.nASCII(text, true);
            long textEncoded = stack.getPointerAddress();
            int n = STBEasyFont.nstb_easy_font_print(x, y, textEncoded, MemoryUtil.memAddressSafe(color), MemoryUtil.memAddress(vertex_buffer), vertex_buffer.remaining());
            return n;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static native void stb_easy_font_spacing(float var0);

    static {
        LibSTB.initialize();
    }
}

