/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.openal;

import java.lang.invoke.MethodHandles;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.Callback;
import org.lwjgl.system.CallbackI;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.libffi.LibFFI;

@FunctionalInterface
@NativeType(value="ALBUFFERCALLBACKTYPESOFT")
public interface SOFTCallbackBufferTypeI
extends CallbackI {
    public static final Callback.Descriptor DESCRIPTOR = new Callback.Descriptor(MethodHandles.lookup(), APIUtil.apiCreateCIF(LibFFI.ffi_type_sint32, LibFFI.ffi_type_pointer, LibFFI.ffi_type_pointer, LibFFI.ffi_type_sint32));

    @Override
    default public Callback.Descriptor getDescriptor() {
        return DESCRIPTOR;
    }

    @Override
    default public void callback(long ret, long args) {
        int __result = this.invoke(MemoryUtil.memGetAddress(MemoryUtil.memGetAddress(args)), MemoryUtil.memGetAddress(MemoryUtil.memGetAddress(args + (long)POINTER_SIZE)), MemoryUtil.memGetInt(MemoryUtil.memGetAddress(args + (long)(2 * POINTER_SIZE))));
        APIUtil.apiClosureRet(ret, __result);
    }

    @NativeType(value="ALsizei")
    public int invoke(@NativeType(value="ALvoid *") long var1, @NativeType(value="ALvoid *") long var3, @NativeType(value="ALsizei") int var5);
}

