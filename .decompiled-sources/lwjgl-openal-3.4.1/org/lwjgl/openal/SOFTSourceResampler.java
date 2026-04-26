/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.openal;

import org.jspecify.annotations.Nullable;
import org.lwjgl.openal.AL;
import org.lwjgl.system.Checks;
import org.lwjgl.system.JNI;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;

public class SOFTSourceResampler {
    public static final int AL_NUM_RESAMPLERS_SOFT = 4624;
    public static final int AL_DEFAULT_RESAMPLER_SOFT = 4625;
    public static final int AL_SOURCE_RESAMPLER_SOFT = 4626;
    public static final int AL_RESAMPLER_NAME_SOFT = 4627;

    protected SOFTSourceResampler() {
        throw new UnsupportedOperationException();
    }

    public static long nalGetStringiSOFT(int pname, int index) {
        long __functionAddress = AL.getICD().alGetStringiSOFT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
        }
        return JNI.invokeP(pname, index, __functionAddress);
    }

    @NativeType(value="ALchar const *")
    public static @Nullable String alGetStringiSOFT(@NativeType(value="ALenum") int pname, @NativeType(value="ALsizei") int index) {
        long __result = SOFTSourceResampler.nalGetStringiSOFT(pname, index);
        return MemoryUtil.memUTF8Safe(__result);
    }

    public static long nalGetStringiDirectSOFT(long context, int pname, int index) {
        long __functionAddress = AL.getICD().alGetStringiDirectSOFT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
            Checks.check(context);
        }
        return JNI.invokePP(context, pname, index, __functionAddress);
    }

    @NativeType(value="ALchar const *")
    public static @Nullable String alGetStringiDirectSOFT(@NativeType(value="ALCcontext *") long context, @NativeType(value="ALenum") int pname, @NativeType(value="ALsizei") int index) {
        long __result = SOFTSourceResampler.nalGetStringiDirectSOFT(context, pname, index);
        return MemoryUtil.memUTF8Safe(__result);
    }
}

