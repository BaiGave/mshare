/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.openal;

import org.lwjgl.openal.AL;
import org.lwjgl.system.Checks;
import org.lwjgl.system.JNI;
import org.lwjgl.system.NativeType;

public class SOFTDeferredUpdates {
    public static final int AL_DEFERRED_UPDATES_SOFT = 49154;

    protected SOFTDeferredUpdates() {
        throw new UnsupportedOperationException();
    }

    @NativeType(value="ALvoid")
    public static void alDeferUpdatesSOFT() {
        long __functionAddress = AL.getICD().alDeferUpdatesSOFT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
        }
        JNI.invokeV(__functionAddress);
    }

    @NativeType(value="ALvoid")
    public static void alDeferUpdatesDirectSOFT(@NativeType(value="ALCcontext *") long context) {
        long __functionAddress = AL.getICD().alDeferUpdatesDirectSOFT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
            Checks.check(context);
        }
        JNI.invokePV(context, __functionAddress);
    }

    @NativeType(value="ALvoid")
    public static void alProcessUpdatesSOFT() {
        long __functionAddress = AL.getICD().alProcessUpdatesSOFT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
        }
        JNI.invokeV(__functionAddress);
    }

    @NativeType(value="ALvoid")
    public static void alProcessUpdatesDirectSOFT(@NativeType(value="ALCcontext *") long context) {
        long __functionAddress = AL.getICD().alProcessUpdatesDirectSOFT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
            Checks.check(context);
        }
        JNI.invokePV(context, __functionAddress);
    }
}

