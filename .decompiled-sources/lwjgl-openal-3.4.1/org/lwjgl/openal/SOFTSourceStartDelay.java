/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.openal;

import java.nio.IntBuffer;
import org.lwjgl.openal.AL;
import org.lwjgl.system.Checks;
import org.lwjgl.system.JNI;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;

public class SOFTSourceStartDelay {
    protected SOFTSourceStartDelay() {
        throw new UnsupportedOperationException();
    }

    @NativeType(value="ALvoid")
    public static void alSourcePlayAtTimeSOFT(@NativeType(value="ALuint") int source, @NativeType(value="ALint64SOFT") long start_time) {
        long __functionAddress = AL.getICD().alSourcePlayAtTimeSOFT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
        }
        JNI.invokeJV(source, start_time, __functionAddress);
    }

    @NativeType(value="ALvoid")
    public static void alSourcePlayAtTimeDirectSOFT(@NativeType(value="ALCcontext *") long context, @NativeType(value="ALuint") int source, @NativeType(value="ALint64SOFT") long start_time) {
        long __functionAddress = AL.getICD().alSourcePlayAtTimeDirectSOFT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
            Checks.check(context);
        }
        JNI.invokePJV(context, source, start_time, __functionAddress);
    }

    public static void nalSourcePlayAtTimevSOFT(int n, long sources, long start_time) {
        long __functionAddress = AL.getICD().alSourcePlayAtTimevSOFT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
        }
        JNI.invokePJV(n, sources, start_time, __functionAddress);
    }

    @NativeType(value="ALvoid")
    public static void alSourcePlayAtTimevSOFT(@NativeType(value="ALuint const *") IntBuffer sources, @NativeType(value="ALint64SOFT") long start_time) {
        SOFTSourceStartDelay.nalSourcePlayAtTimevSOFT(sources.remaining(), MemoryUtil.memAddress(sources), start_time);
    }

    public static void nalSourcePlayAtTimevDirectSOFT(long context, int n, long sources, long start_time) {
        long __functionAddress = AL.getICD().alSourcePlayAtTimevDirectSOFT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
            Checks.check(context);
        }
        JNI.invokePPJV(context, n, sources, start_time, __functionAddress);
    }

    @NativeType(value="ALvoid")
    public static void alSourcePlayAtTimevDirectSOFT(@NativeType(value="ALCcontext *") long context, @NativeType(value="ALuint const *") IntBuffer sources, @NativeType(value="ALint64SOFT") long start_time) {
        SOFTSourceStartDelay.nalSourcePlayAtTimevDirectSOFT(context, sources.remaining(), MemoryUtil.memAddress(sources), start_time);
    }

    @NativeType(value="ALvoid")
    public static void alSourcePlayAtTimevSOFT(@NativeType(value="ALuint const *") int[] sources, @NativeType(value="ALint64SOFT") long start_time) {
        long __functionAddress = AL.getICD().alSourcePlayAtTimevSOFT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
        }
        JNI.invokePJV(sources.length, sources, start_time, __functionAddress);
    }

    @NativeType(value="ALvoid")
    public static void alSourcePlayAtTimevDirectSOFT(@NativeType(value="ALCcontext *") long context, @NativeType(value="ALuint const *") int[] sources, @NativeType(value="ALint64SOFT") long start_time) {
        long __functionAddress = AL.getICD().alSourcePlayAtTimevDirectSOFT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
            Checks.check(context);
        }
        JNI.invokePPJV(context, sources.length, sources, start_time, __functionAddress);
    }
}

