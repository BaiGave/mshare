/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.openal;

import java.nio.ByteBuffer;
import org.lwjgl.openal.ALC;
import org.lwjgl.system.Checks;
import org.lwjgl.system.JNI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;

public class EXTDirectContext {
    protected EXTDirectContext() {
        throw new UnsupportedOperationException();
    }

    public static long nalcGetProcAddress2(long device, long funcName) {
        long __functionAddress = ALC.getICD().alcGetProcAddress2;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
        }
        return JNI.invokePPP(device, funcName, __functionAddress);
    }

    @NativeType(value="ALCvoid *")
    public static long alcGetProcAddress2(@NativeType(value="ALCdevice *") long device, @NativeType(value="ALchar const *") ByteBuffer funcName) {
        if (Checks.CHECKS) {
            Checks.checkNT1(funcName);
        }
        return EXTDirectContext.nalcGetProcAddress2(device, MemoryUtil.memAddress(funcName));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="ALCvoid *")
    public static long alcGetProcAddress2(@NativeType(value="ALCdevice *") long device, @NativeType(value="ALchar const *") CharSequence funcName) {
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            stack.nASCII(funcName, true);
            long funcNameEncoded = stack.getPointerAddress();
            long l = EXTDirectContext.nalcGetProcAddress2(device, funcNameEncoded);
            return l;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }
}

