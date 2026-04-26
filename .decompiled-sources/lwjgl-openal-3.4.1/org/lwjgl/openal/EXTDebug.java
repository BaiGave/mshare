/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.openal;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.EXTDebugProcI;
import org.lwjgl.system.Checks;
import org.lwjgl.system.JNI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;

public class EXTDebug {
    public static final int ALC_CONTEXT_FLAGS_EXT = 6607;
    public static final int ALC_CONTEXT_DEBUG_BIT_EXT = 1;
    public static final int AL_CONTEXT_FLAGS_EXT = 6607;
    public static final int AL_CONTEXT_DEBUG_BIT_EXT = 1;
    public static final int AL_DEBUG_OUTPUT_EXT = 6578;
    public static final int AL_DEBUG_CALLBACK_FUNCTION_EXT = 6579;
    public static final int AL_DEBUG_CALLBACK_USER_PARAM_EXT = 6580;
    public static final int AL_DEBUG_SOURCE_API_EXT = 6581;
    public static final int AL_DEBUG_SOURCE_AUDIO_SYSTEM_EXT = 6582;
    public static final int AL_DEBUG_SOURCE_THIRD_PARTY_EXT = 6583;
    public static final int AL_DEBUG_SOURCE_APPLICATION_EXT = 6584;
    public static final int AL_DEBUG_SOURCE_OTHER_EXT = 6585;
    public static final int AL_DEBUG_TYPE_ERROR_EXT = 6586;
    public static final int AL_DEBUG_TYPE_DEPRECATED_BEHAVIOR_EXT = 6587;
    public static final int AL_DEBUG_TYPE_UNDEFINED_BEHAVIOR_EXT = 6588;
    public static final int AL_DEBUG_TYPE_PORTABILITY_EXT = 6589;
    public static final int AL_DEBUG_TYPE_PERFORMANCE_EXT = 6590;
    public static final int AL_DEBUG_TYPE_MARKER_EXT = 6591;
    public static final int AL_DEBUG_TYPE_OTHER_EXT = 6594;
    public static final int AL_DEBUG_TYPE_PUSH_GROUP_EXT = 6592;
    public static final int AL_DEBUG_TYPE_POP_GROUP_EXT = 6593;
    public static final int AL_DEBUG_SEVERITY_HIGH_EXT = 6595;
    public static final int AL_DEBUG_SEVERITY_MEDIUM_EXT = 6596;
    public static final int AL_DEBUG_SEVERITY_LOW_EXT = 6597;
    public static final int AL_DEBUG_SEVERITY_NOTIFICATION_EXT = 6598;
    public static final int AL_DONT_CARE_EXT = 2;
    public static final int AL_DEBUG_LOGGED_MESSAGES_EXT = 6599;
    public static final int AL_DEBUG_NEXT_LOGGED_MESSAGE_LENGTH_EXT = 6600;
    public static final int AL_MAX_DEBUG_MESSAGE_LENGTH_EXT = 6601;
    public static final int AL_MAX_DEBUG_LOGGED_MESSAGES_EXT = 6602;
    public static final int AL_MAX_DEBUG_GROUP_STACK_DEPTH_EXT = 6603;
    public static final int AL_MAX_LABEL_LENGTH_EXT = 6604;
    public static final int AL_STACK_OVERFLOW_EXT = 6605;
    public static final int AL_STACK_UNDERFLOW_EXT = 6606;
    public static final int AL_BUFFER_EXT = 4105;
    public static final int AL_SOURCE_EXT = 6608;
    public static final int AL_FILTER_EXT = 6609;
    public static final int AL_EFFECT_EXT = 6610;
    public static final int AL_AUXILIARY_EFFECT_SLOT_EXT = 6611;

    protected EXTDebug() {
        throw new UnsupportedOperationException();
    }

    public static void nalDebugMessageCallbackEXT(long callback, long userParam) {
        long __functionAddress = AL.getICD().alDebugMessageCallbackEXT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
        }
        JNI.invokePPV(callback, userParam, __functionAddress);
    }

    @NativeType(value="ALvoid")
    public static void alDebugMessageCallbackEXT(@NativeType(value="ALDEBUGPROCEXT") @Nullable EXTDebugProcI callback, @NativeType(value="ALvoid *") long userParam) {
        EXTDebug.nalDebugMessageCallbackEXT(MemoryUtil.memAddressSafe(callback), userParam);
    }

    public static void nalDebugMessageCallbackDirectEXT(long context, long callback, long userParam) {
        long __functionAddress = AL.getICD().alDebugMessageCallbackDirectEXT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
            Checks.check(context);
        }
        JNI.invokePPPV(context, callback, userParam, __functionAddress);
    }

    @NativeType(value="ALvoid")
    public static void alDebugMessageCallbackDirectEXT(@NativeType(value="ALCcontext *") long context, @NativeType(value="ALDEBUGPROCEXT") @Nullable EXTDebugProcI callback, @NativeType(value="ALvoid *") long userParam) {
        EXTDebug.nalDebugMessageCallbackDirectEXT(context, MemoryUtil.memAddressSafe(callback), userParam);
    }

    public static void nalDebugMessageInsertEXT(int source, int type, int id, int severity, int length, long message) {
        long __functionAddress = AL.getICD().alDebugMessageInsertEXT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
        }
        JNI.invokePV(source, type, id, severity, length, message, __functionAddress);
    }

    @NativeType(value="ALvoid")
    public static void alDebugMessageInsertEXT(@NativeType(value="ALenum") int source, @NativeType(value="ALenum") int type, @NativeType(value="ALuint") int id, @NativeType(value="ALenum") int severity, @NativeType(value="ALchar const *") ByteBuffer message) {
        EXTDebug.nalDebugMessageInsertEXT(source, type, id, severity, message.remaining(), MemoryUtil.memAddress(message));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="ALvoid")
    public static void alDebugMessageInsertEXT(@NativeType(value="ALenum") int source, @NativeType(value="ALenum") int type, @NativeType(value="ALuint") int id, @NativeType(value="ALenum") int severity, @NativeType(value="ALchar const *") CharSequence message) {
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            int messageEncodedLength = stack.nUTF8(message, false);
            long messageEncoded = stack.getPointerAddress();
            EXTDebug.nalDebugMessageInsertEXT(source, type, id, severity, messageEncodedLength, messageEncoded);
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static void nalDebugMessageInsertDirectEXT(long context, int source, int type, int id, int severity, int length, long message) {
        long __functionAddress = AL.getICD().alDebugMessageInsertDirectEXT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
            Checks.check(context);
        }
        JNI.invokePPV(context, source, type, id, severity, length, message, __functionAddress);
    }

    @NativeType(value="ALvoid")
    public static void alDebugMessageInsertDirectEXT(@NativeType(value="ALCcontext *") long context, @NativeType(value="ALenum") int source, @NativeType(value="ALenum") int type, @NativeType(value="ALuint") int id, @NativeType(value="ALenum") int severity, @NativeType(value="ALchar const *") ByteBuffer message) {
        EXTDebug.nalDebugMessageInsertDirectEXT(context, source, type, id, severity, message.remaining(), MemoryUtil.memAddress(message));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="ALvoid")
    public static void alDebugMessageInsertDirectEXT(@NativeType(value="ALCcontext *") long context, @NativeType(value="ALenum") int source, @NativeType(value="ALenum") int type, @NativeType(value="ALuint") int id, @NativeType(value="ALenum") int severity, @NativeType(value="ALchar const *") CharSequence message) {
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            int messageEncodedLength = stack.nUTF8(message, false);
            long messageEncoded = stack.getPointerAddress();
            EXTDebug.nalDebugMessageInsertDirectEXT(context, source, type, id, severity, messageEncodedLength, messageEncoded);
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static void nalDebugMessageControlEXT(int source, int type, int severity, int count, long ids, boolean enable) {
        long __functionAddress = AL.getICD().alDebugMessageControlEXT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
        }
        JNI.invokePV(source, type, severity, count, ids, enable, __functionAddress);
    }

    @NativeType(value="ALvoid")
    public static void alDebugMessageControlEXT(@NativeType(value="ALenum") int source, @NativeType(value="ALenum") int type, @NativeType(value="ALenum") int severity, @NativeType(value="ALuint const *") @Nullable IntBuffer ids, @NativeType(value="ALboolean") boolean enable) {
        EXTDebug.nalDebugMessageControlEXT(source, type, severity, Checks.remainingSafe(ids), MemoryUtil.memAddressSafe(ids), enable);
    }

    public static void nalDebugMessageControlDirectEXT(long context, int source, int type, int severity, int count, long ids, boolean enable) {
        long __functionAddress = AL.getICD().alDebugMessageControlDirectEXT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
            Checks.check(context);
        }
        JNI.invokePPV(context, source, type, severity, count, ids, enable, __functionAddress);
    }

    @NativeType(value="ALvoid")
    public static void alDebugMessageControlDirectEXT(@NativeType(value="ALCcontext *") long context, @NativeType(value="ALenum") int source, @NativeType(value="ALenum") int type, @NativeType(value="ALenum") int severity, @NativeType(value="ALuint const *") @Nullable IntBuffer ids, @NativeType(value="ALboolean") boolean enable) {
        EXTDebug.nalDebugMessageControlDirectEXT(context, source, type, severity, Checks.remainingSafe(ids), MemoryUtil.memAddressSafe(ids), enable);
    }

    public static void nalPushDebugGroupEXT(int source, int id, int length, long message) {
        long __functionAddress = AL.getICD().alPushDebugGroupEXT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
        }
        JNI.invokePV(source, id, length, message, __functionAddress);
    }

    @NativeType(value="ALvoid")
    public static void alPushDebugGroupEXT(@NativeType(value="ALenum") int source, @NativeType(value="ALuint") int id, @NativeType(value="ALchar const *") ByteBuffer message) {
        EXTDebug.nalPushDebugGroupEXT(source, id, message.remaining(), MemoryUtil.memAddress(message));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="ALvoid")
    public static void alPushDebugGroupEXT(@NativeType(value="ALenum") int source, @NativeType(value="ALuint") int id, @NativeType(value="ALchar const *") CharSequence message) {
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            int messageEncodedLength = stack.nUTF8(message, false);
            long messageEncoded = stack.getPointerAddress();
            EXTDebug.nalPushDebugGroupEXT(source, id, messageEncodedLength, messageEncoded);
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static void nalPushDebugGroupDirectEXT(long context, int source, int id, int length, long message) {
        long __functionAddress = AL.getICD().alPushDebugGroupDirectEXT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
            Checks.check(context);
        }
        JNI.invokePPV(context, source, id, length, message, __functionAddress);
    }

    @NativeType(value="ALvoid")
    public static void alPushDebugGroupDirectEXT(@NativeType(value="ALCcontext *") long context, @NativeType(value="ALenum") int source, @NativeType(value="ALuint") int id, @NativeType(value="ALchar const *") ByteBuffer message) {
        EXTDebug.nalPushDebugGroupDirectEXT(context, source, id, message.remaining(), MemoryUtil.memAddress(message));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="ALvoid")
    public static void alPushDebugGroupDirectEXT(@NativeType(value="ALCcontext *") long context, @NativeType(value="ALenum") int source, @NativeType(value="ALuint") int id, @NativeType(value="ALchar const *") CharSequence message) {
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            int messageEncodedLength = stack.nUTF8(message, false);
            long messageEncoded = stack.getPointerAddress();
            EXTDebug.nalPushDebugGroupDirectEXT(context, source, id, messageEncodedLength, messageEncoded);
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    @NativeType(value="ALvoid")
    public static void alPopDebugGroupEXT() {
        long __functionAddress = AL.getICD().alPopDebugGroupEXT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
        }
        JNI.invokeV(__functionAddress);
    }

    @NativeType(value="ALvoid")
    public static void alPopDebugGroupDirectEXT(@NativeType(value="ALCcontext *") long context) {
        long __functionAddress = AL.getICD().alPopDebugGroupDirectEXT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
            Checks.check(context);
        }
        JNI.invokePV(context, __functionAddress);
    }

    public static int nalGetDebugMessageLogEXT(int count, int logBufSize, long sources, long types, long ids, long severities, long lengths, long logBuf) {
        long __functionAddress = AL.getICD().alGetDebugMessageLogEXT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
        }
        return JNI.invokePPPPPPI(count, logBufSize, sources, types, ids, severities, lengths, logBuf, __functionAddress);
    }

    @NativeType(value="ALuint")
    public static int alGetDebugMessageLogEXT(@NativeType(value="ALenum *") @Nullable IntBuffer sources, @NativeType(value="ALenum *") @Nullable IntBuffer types, @NativeType(value="ALuint *") @Nullable IntBuffer ids, @NativeType(value="ALenum *") @Nullable IntBuffer severities, @NativeType(value="ALsizei *") @Nullable IntBuffer lengths, @NativeType(value="ALchar *") @Nullable ByteBuffer logBuf) {
        if (Checks.CHECKS) {
            Checks.checkSafe((Buffer)types, Checks.remainingSafe(sources));
            Checks.checkSafe((Buffer)ids, Checks.remainingSafe(sources));
            Checks.checkSafe((Buffer)severities, Checks.remainingSafe(sources));
            Checks.checkSafe((Buffer)lengths, Checks.remainingSafe(sources));
        }
        return EXTDebug.nalGetDebugMessageLogEXT(Checks.remainingSafe(sources), Checks.remainingSafe(logBuf), MemoryUtil.memAddressSafe(sources), MemoryUtil.memAddressSafe(types), MemoryUtil.memAddressSafe(ids), MemoryUtil.memAddressSafe(severities), MemoryUtil.memAddressSafe(lengths), MemoryUtil.memAddressSafe(logBuf));
    }

    public static int nalGetDebugMessageLogDirectEXT(long context, int count, int logBufSize, long sources, long types, long ids, long severities, long lengths, long logBuf) {
        long __functionAddress = AL.getICD().alGetDebugMessageLogDirectEXT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
            Checks.check(context);
        }
        return JNI.invokePPPPPPPI(context, count, logBufSize, sources, types, ids, severities, lengths, logBuf, __functionAddress);
    }

    @NativeType(value="ALuint")
    public static int alGetDebugMessageLogDirectEXT(@NativeType(value="ALCcontext *") long context, @NativeType(value="ALenum *") @Nullable IntBuffer sources, @NativeType(value="ALenum *") @Nullable IntBuffer types, @NativeType(value="ALuint *") @Nullable IntBuffer ids, @NativeType(value="ALenum *") @Nullable IntBuffer severities, @NativeType(value="ALsizei *") @Nullable IntBuffer lengths, @NativeType(value="ALchar *") @Nullable ByteBuffer logBuf) {
        if (Checks.CHECKS) {
            Checks.checkSafe((Buffer)types, Checks.remainingSafe(sources));
            Checks.checkSafe((Buffer)ids, Checks.remainingSafe(sources));
            Checks.checkSafe((Buffer)severities, Checks.remainingSafe(sources));
            Checks.checkSafe((Buffer)lengths, Checks.remainingSafe(sources));
        }
        return EXTDebug.nalGetDebugMessageLogDirectEXT(context, Checks.remainingSafe(sources), Checks.remainingSafe(logBuf), MemoryUtil.memAddressSafe(sources), MemoryUtil.memAddressSafe(types), MemoryUtil.memAddressSafe(ids), MemoryUtil.memAddressSafe(severities), MemoryUtil.memAddressSafe(lengths), MemoryUtil.memAddressSafe(logBuf));
    }

    public static void nalObjectLabelEXT(int identifier, int name, int length, long label) {
        long __functionAddress = AL.getICD().alObjectLabelEXT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
        }
        JNI.invokePV(identifier, name, length, label, __functionAddress);
    }

    @NativeType(value="ALvoid")
    public static void alObjectLabelEXT(@NativeType(value="ALenum") int identifier, @NativeType(value="ALuint") int name, @NativeType(value="ALchar const *") ByteBuffer label) {
        EXTDebug.nalObjectLabelEXT(identifier, name, label.remaining(), MemoryUtil.memAddress(label));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="ALvoid")
    public static void alObjectLabelEXT(@NativeType(value="ALenum") int identifier, @NativeType(value="ALuint") int name, @NativeType(value="ALchar const *") CharSequence label) {
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            int labelEncodedLength = stack.nUTF8(label, false);
            long labelEncoded = stack.getPointerAddress();
            EXTDebug.nalObjectLabelEXT(identifier, name, labelEncodedLength, labelEncoded);
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static void nalObjectLabelDirectEXT(long context, int identifier, int name, int length, long label) {
        long __functionAddress = AL.getICD().alObjectLabelDirectEXT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
            Checks.check(context);
        }
        JNI.invokePPV(context, identifier, name, length, label, __functionAddress);
    }

    @NativeType(value="ALvoid")
    public static void alObjectLabelDirectEXT(@NativeType(value="ALCcontext *") long context, @NativeType(value="ALenum") int identifier, @NativeType(value="ALuint") int name, @NativeType(value="ALchar const *") ByteBuffer label) {
        EXTDebug.nalObjectLabelDirectEXT(context, identifier, name, label.remaining(), MemoryUtil.memAddress(label));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="ALvoid")
    public static void alObjectLabelDirectEXT(@NativeType(value="ALCcontext *") long context, @NativeType(value="ALenum") int identifier, @NativeType(value="ALuint") int name, @NativeType(value="ALchar const *") CharSequence label) {
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            int labelEncodedLength = stack.nUTF8(label, false);
            long labelEncoded = stack.getPointerAddress();
            EXTDebug.nalObjectLabelDirectEXT(context, identifier, name, labelEncodedLength, labelEncoded);
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static void nalGetObjectLabelEXT(int identifier, int name, int bufSize, long length, long label) {
        long __functionAddress = AL.getICD().alGetObjectLabelEXT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
        }
        JNI.invokePPV(identifier, name, bufSize, length, label, __functionAddress);
    }

    @NativeType(value="ALvoid")
    public static void alGetObjectLabelEXT(@NativeType(value="ALenum") int identifier, @NativeType(value="ALuint") int name, @NativeType(value="ALsizei *") IntBuffer length, @NativeType(value="ALchar *") @Nullable ByteBuffer label) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)length, 1);
        }
        EXTDebug.nalGetObjectLabelEXT(identifier, name, Checks.remainingSafe(label), MemoryUtil.memAddress(length), MemoryUtil.memAddressSafe(label));
    }

    public static void nalGetObjectLabelDirectEXT(long context, int identifier, int name, int bufSize, long length, long label) {
        long __functionAddress = AL.getICD().alGetObjectLabelDirectEXT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
            Checks.check(context);
        }
        JNI.invokePPPV(context, identifier, name, bufSize, length, label, __functionAddress);
    }

    @NativeType(value="ALvoid")
    public static void alGetObjectLabelDirectEXT(@NativeType(value="ALCcontext *") long context, @NativeType(value="ALenum") int identifier, @NativeType(value="ALuint") int name, @NativeType(value="ALsizei *") IntBuffer length, @NativeType(value="ALchar *") @Nullable ByteBuffer label) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)length, 1);
        }
        EXTDebug.nalGetObjectLabelDirectEXT(context, identifier, name, Checks.remainingSafe(label), MemoryUtil.memAddress(length), MemoryUtil.memAddressSafe(label));
    }

    @NativeType(value="ALvoid *")
    public static long alGetPointerEXT(@NativeType(value="ALenum") int pname) {
        long __functionAddress = AL.getICD().alGetPointerEXT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
        }
        return JNI.invokeP(pname, __functionAddress);
    }

    @NativeType(value="ALvoid *")
    public static long alGetPointerDirectEXT(@NativeType(value="ALCcontext *") long context, @NativeType(value="ALenum") int pname) {
        long __functionAddress = AL.getICD().alGetPointerDirectEXT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
            Checks.check(context);
        }
        return JNI.invokePP(context, pname, __functionAddress);
    }

    public static void nalGetPointervEXT(int pname, long values) {
        long __functionAddress = AL.getICD().alGetPointervEXT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
        }
        JNI.invokePV(pname, values, __functionAddress);
    }

    @NativeType(value="ALvoid")
    public static void alGetPointervEXT(@NativeType(value="ALenum") int pname, @NativeType(value="ALvoid **") PointerBuffer values) {
        if (Checks.CHECKS) {
            Checks.check(values, 1);
        }
        EXTDebug.nalGetPointervEXT(pname, MemoryUtil.memAddress(values));
    }

    public static void nalGetPointervDirectEXT(long context, int pname, long values) {
        long __functionAddress = AL.getICD().alGetPointervDirectEXT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
            Checks.check(context);
        }
        JNI.invokePPV(context, pname, values, __functionAddress);
    }

    @NativeType(value="ALvoid")
    public static void alGetPointervDirectEXT(@NativeType(value="ALCcontext *") long context, @NativeType(value="ALenum") int pname, @NativeType(value="ALvoid **") PointerBuffer values) {
        if (Checks.CHECKS) {
            Checks.check(values, 1);
        }
        EXTDebug.nalGetPointervDirectEXT(context, pname, MemoryUtil.memAddress(values));
    }

    @NativeType(value="ALvoid")
    public static void alDebugMessageControlEXT(@NativeType(value="ALenum") int source, @NativeType(value="ALenum") int type, @NativeType(value="ALenum") int severity, @NativeType(value="ALuint const *") int @Nullable [] ids, @NativeType(value="ALboolean") boolean enable) {
        long __functionAddress = AL.getICD().alDebugMessageControlEXT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
        }
        JNI.invokePV(source, type, severity, Checks.lengthSafe(ids), ids, enable, __functionAddress);
    }

    @NativeType(value="ALvoid")
    public static void alDebugMessageControlDirectEXT(@NativeType(value="ALCcontext *") long context, @NativeType(value="ALenum") int source, @NativeType(value="ALenum") int type, @NativeType(value="ALenum") int severity, @NativeType(value="ALuint const *") int @Nullable [] ids, @NativeType(value="ALboolean") boolean enable) {
        long __functionAddress = AL.getICD().alDebugMessageControlDirectEXT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
            Checks.check(context);
        }
        JNI.invokePPV(context, source, type, severity, Checks.lengthSafe(ids), ids, enable, __functionAddress);
    }

    @NativeType(value="ALuint")
    public static int alGetDebugMessageLogEXT(@NativeType(value="ALenum *") int @Nullable [] sources, @NativeType(value="ALenum *") int @Nullable [] types, @NativeType(value="ALuint *") int @Nullable [] ids, @NativeType(value="ALenum *") int @Nullable [] severities, @NativeType(value="ALsizei *") int @Nullable [] lengths, @NativeType(value="ALchar *") @Nullable ByteBuffer logBuf) {
        long __functionAddress = AL.getICD().alGetDebugMessageLogEXT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
            Checks.checkSafe(types, Checks.lengthSafe(sources));
            Checks.checkSafe(ids, Checks.lengthSafe(sources));
            Checks.checkSafe(severities, Checks.lengthSafe(sources));
            Checks.checkSafe(lengths, Checks.lengthSafe(sources));
        }
        return JNI.invokePPPPPPI(Checks.lengthSafe(sources), Checks.remainingSafe(logBuf), sources, types, ids, severities, lengths, MemoryUtil.memAddressSafe(logBuf), __functionAddress);
    }

    @NativeType(value="ALuint")
    public static int alGetDebugMessageLogDirectEXT(@NativeType(value="ALCcontext *") long context, @NativeType(value="ALenum *") int @Nullable [] sources, @NativeType(value="ALenum *") int @Nullable [] types, @NativeType(value="ALuint *") int @Nullable [] ids, @NativeType(value="ALenum *") int @Nullable [] severities, @NativeType(value="ALsizei *") int @Nullable [] lengths, @NativeType(value="ALchar *") @Nullable ByteBuffer logBuf) {
        long __functionAddress = AL.getICD().alGetDebugMessageLogDirectEXT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
            Checks.check(context);
            Checks.checkSafe(types, Checks.lengthSafe(sources));
            Checks.checkSafe(ids, Checks.lengthSafe(sources));
            Checks.checkSafe(severities, Checks.lengthSafe(sources));
            Checks.checkSafe(lengths, Checks.lengthSafe(sources));
        }
        return JNI.invokePPPPPPPI(context, Checks.lengthSafe(sources), Checks.remainingSafe(logBuf), sources, types, ids, severities, lengths, MemoryUtil.memAddressSafe(logBuf), __functionAddress);
    }

    @NativeType(value="ALvoid")
    public static void alGetObjectLabelEXT(@NativeType(value="ALenum") int identifier, @NativeType(value="ALuint") int name, @NativeType(value="ALsizei *") int[] length, @NativeType(value="ALchar *") @Nullable ByteBuffer label) {
        long __functionAddress = AL.getICD().alGetObjectLabelEXT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
            Checks.check(length, 1);
        }
        JNI.invokePPV(identifier, name, Checks.remainingSafe(label), length, MemoryUtil.memAddressSafe(label), __functionAddress);
    }

    @NativeType(value="ALvoid")
    public static void alGetObjectLabelDirectEXT(@NativeType(value="ALCcontext *") long context, @NativeType(value="ALenum") int identifier, @NativeType(value="ALuint") int name, @NativeType(value="ALsizei *") int[] length, @NativeType(value="ALchar *") @Nullable ByteBuffer label) {
        long __functionAddress = AL.getICD().alGetObjectLabelDirectEXT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
            Checks.check(context);
            Checks.check(length, 1);
        }
        JNI.invokePPPV(context, identifier, name, Checks.remainingSafe(label), length, MemoryUtil.memAddressSafe(label), __functionAddress);
    }
}

