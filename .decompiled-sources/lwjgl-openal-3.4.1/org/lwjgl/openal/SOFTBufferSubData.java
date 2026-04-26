/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.openal;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import org.lwjgl.openal.AL;
import org.lwjgl.system.Checks;
import org.lwjgl.system.JNI;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;

public class SOFTBufferSubData {
    public static final int AL_BYTE_RW_OFFSETS_SOFT = 4145;
    public static final int AL_SAMPLE_RW_OFFSETS_SOFT = 4146;

    protected SOFTBufferSubData() {
        throw new UnsupportedOperationException();
    }

    public static void nalBufferSubDataSOFT(int buffer, int format, long data, int offset, int length) {
        long __functionAddress = AL.getICD().alBufferSubDataSOFT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
        }
        JNI.invokePV(buffer, format, data, offset, length, __functionAddress);
    }

    public static void alBufferSubDataSOFT(@NativeType(value="ALuint") int buffer, @NativeType(value="ALenum") int format, @NativeType(value="ALvoid const *") ByteBuffer data, @NativeType(value="ALsizei") int offset) {
        SOFTBufferSubData.nalBufferSubDataSOFT(buffer, format, MemoryUtil.memAddress(data), offset, data.remaining());
    }

    public static void alBufferSubDataSOFT(@NativeType(value="ALuint") int buffer, @NativeType(value="ALenum") int format, @NativeType(value="ALvoid const *") ShortBuffer data, @NativeType(value="ALsizei") int offset) {
        SOFTBufferSubData.nalBufferSubDataSOFT(buffer, format, MemoryUtil.memAddress(data), offset, data.remaining() << 1);
    }

    public static void alBufferSubDataSOFT(@NativeType(value="ALuint") int buffer, @NativeType(value="ALenum") int format, @NativeType(value="ALvoid const *") IntBuffer data, @NativeType(value="ALsizei") int offset) {
        SOFTBufferSubData.nalBufferSubDataSOFT(buffer, format, MemoryUtil.memAddress(data), offset, data.remaining() << 2);
    }

    public static void alBufferSubDataSOFT(@NativeType(value="ALuint") int buffer, @NativeType(value="ALenum") int format, @NativeType(value="ALvoid const *") FloatBuffer data, @NativeType(value="ALsizei") int offset) {
        SOFTBufferSubData.nalBufferSubDataSOFT(buffer, format, MemoryUtil.memAddress(data), offset, data.remaining() << 2);
    }

    public static void nalBufferSubDataDirectSOFT(long context, int buffer, int format, long data, int offset, int length) {
        long __functionAddress = AL.getICD().alBufferSubDataDirectSOFT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
            Checks.check(context);
        }
        JNI.invokePPV(context, buffer, format, data, offset, length, __functionAddress);
    }

    public static void alBufferSubDataDirectSOFT(@NativeType(value="ALCcontext *") long context, @NativeType(value="ALuint") int buffer, @NativeType(value="ALenum") int format, @NativeType(value="ALvoid const *") ByteBuffer data, @NativeType(value="ALsizei") int offset) {
        SOFTBufferSubData.nalBufferSubDataDirectSOFT(context, buffer, format, MemoryUtil.memAddress(data), offset, data.remaining());
    }

    public static void alBufferSubDataDirectSOFT(@NativeType(value="ALCcontext *") long context, @NativeType(value="ALuint") int buffer, @NativeType(value="ALenum") int format, @NativeType(value="ALvoid const *") ShortBuffer data, @NativeType(value="ALsizei") int offset) {
        SOFTBufferSubData.nalBufferSubDataDirectSOFT(context, buffer, format, MemoryUtil.memAddress(data), offset, data.remaining() << 1);
    }

    public static void alBufferSubDataDirectSOFT(@NativeType(value="ALCcontext *") long context, @NativeType(value="ALuint") int buffer, @NativeType(value="ALenum") int format, @NativeType(value="ALvoid const *") IntBuffer data, @NativeType(value="ALsizei") int offset) {
        SOFTBufferSubData.nalBufferSubDataDirectSOFT(context, buffer, format, MemoryUtil.memAddress(data), offset, data.remaining() << 2);
    }

    public static void alBufferSubDataDirectSOFT(@NativeType(value="ALCcontext *") long context, @NativeType(value="ALuint") int buffer, @NativeType(value="ALenum") int format, @NativeType(value="ALvoid const *") FloatBuffer data, @NativeType(value="ALsizei") int offset) {
        SOFTBufferSubData.nalBufferSubDataDirectSOFT(context, buffer, format, MemoryUtil.memAddress(data), offset, data.remaining() << 2);
    }

    public static void alBufferSubDataSOFT(@NativeType(value="ALuint") int buffer, @NativeType(value="ALenum") int format, @NativeType(value="ALvoid const *") short[] data, @NativeType(value="ALsizei") int offset) {
        long __functionAddress = AL.getICD().alBufferSubDataSOFT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
        }
        JNI.invokePV(buffer, format, data, offset, data.length << 1, __functionAddress);
    }

    public static void alBufferSubDataSOFT(@NativeType(value="ALuint") int buffer, @NativeType(value="ALenum") int format, @NativeType(value="ALvoid const *") int[] data, @NativeType(value="ALsizei") int offset) {
        long __functionAddress = AL.getICD().alBufferSubDataSOFT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
        }
        JNI.invokePV(buffer, format, data, offset, data.length << 2, __functionAddress);
    }

    public static void alBufferSubDataSOFT(@NativeType(value="ALuint") int buffer, @NativeType(value="ALenum") int format, @NativeType(value="ALvoid const *") float[] data, @NativeType(value="ALsizei") int offset) {
        long __functionAddress = AL.getICD().alBufferSubDataSOFT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
        }
        JNI.invokePV(buffer, format, data, offset, data.length << 2, __functionAddress);
    }

    public static void alBufferSubDataDirectSOFT(@NativeType(value="ALCcontext *") long context, @NativeType(value="ALuint") int buffer, @NativeType(value="ALenum") int format, @NativeType(value="ALvoid const *") short[] data, @NativeType(value="ALsizei") int offset) {
        long __functionAddress = AL.getICD().alBufferSubDataDirectSOFT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
            Checks.check(context);
        }
        JNI.invokePPV(context, buffer, format, data, offset, data.length << 1, __functionAddress);
    }

    public static void alBufferSubDataDirectSOFT(@NativeType(value="ALCcontext *") long context, @NativeType(value="ALuint") int buffer, @NativeType(value="ALenum") int format, @NativeType(value="ALvoid const *") int[] data, @NativeType(value="ALsizei") int offset) {
        long __functionAddress = AL.getICD().alBufferSubDataDirectSOFT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
            Checks.check(context);
        }
        JNI.invokePPV(context, buffer, format, data, offset, data.length << 2, __functionAddress);
    }

    public static void alBufferSubDataDirectSOFT(@NativeType(value="ALCcontext *") long context, @NativeType(value="ALuint") int buffer, @NativeType(value="ALenum") int format, @NativeType(value="ALvoid const *") float[] data, @NativeType(value="ALsizei") int offset) {
        long __functionAddress = AL.getICD().alBufferSubDataDirectSOFT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
            Checks.check(context);
        }
        JNI.invokePPV(context, buffer, format, data, offset, data.length << 2, __functionAddress);
    }
}

