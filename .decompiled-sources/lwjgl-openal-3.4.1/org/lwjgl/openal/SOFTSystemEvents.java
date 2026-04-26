/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.openal;

import java.nio.IntBuffer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.SOFTSystemEventProcI;
import org.lwjgl.system.Checks;
import org.lwjgl.system.JNI;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;

public class SOFTSystemEvents {
    public static final int ALC_EVENT_TYPE_DEFAULT_DEVICE_CHANGED_SOFT = 6614;
    public static final int ALC_EVENT_TYPE_DEVICE_ADDED_SOFT = 6615;
    public static final int ALC_EVENT_TYPE_DEVICE_REMOVED_SOFT = 6616;
    public static final int ALC_PLAYBACK_DEVICE_SOFT = 6612;
    public static final int ALC_CAPTURE_DEVICE_SOFT = 6613;
    public static final int ALC_EVENT_SUPPORTED_SOFT = 6617;
    public static final int ALC_EVENT_NOT_SUPPORTED_SOFT = 6618;

    protected SOFTSystemEvents() {
        throw new UnsupportedOperationException();
    }

    @NativeType(value="ALCenum")
    public static int alcEventIsSupportedSOFT(@NativeType(value="ALCenum") int eventType, @NativeType(value="ALCenum") int deviceType) {
        long __functionAddress = ALC.getICD().alcEventIsSupportedSOFT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
        }
        return JNI.invokeI(eventType, deviceType, __functionAddress);
    }

    public static boolean nalcEventControlSOFT(int count, long events, boolean enable) {
        long __functionAddress = ALC.getICD().alcEventControlSOFT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
        }
        return JNI.invokePZ(count, events, enable, __functionAddress);
    }

    @NativeType(value="ALCboolean")
    public static boolean alcEventControlSOFT(@NativeType(value="ALCenum const *") @Nullable IntBuffer events, @NativeType(value="ALCboolean") boolean enable) {
        return SOFTSystemEvents.nalcEventControlSOFT(Checks.remainingSafe(events), MemoryUtil.memAddressSafe(events), enable);
    }

    public static void nalcEventCallbackSOFT(long callback, long userParam) {
        long __functionAddress = ALC.getICD().alcEventCallbackSOFT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
        }
        JNI.invokePPV(callback, userParam, __functionAddress);
    }

    @NativeType(value="ALCvoid")
    public static void alcEventCallbackSOFT(@NativeType(value="ALCEVENTPROCTYPESOFT") @Nullable SOFTSystemEventProcI callback, @NativeType(value="ALCvoid *") long userParam) {
        SOFTSystemEvents.nalcEventCallbackSOFT(MemoryUtil.memAddressSafe(callback), userParam);
    }

    @NativeType(value="ALCboolean")
    public static boolean alcEventControlSOFT(@NativeType(value="ALCenum const *") int @Nullable [] events, @NativeType(value="ALCboolean") boolean enable) {
        long __functionAddress = ALC.getICD().alcEventControlSOFT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
        }
        return JNI.invokePZ(Checks.lengthSafe(events), events, enable, __functionAddress);
    }
}

