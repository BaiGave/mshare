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
@NativeType(value="ALCEVENTPROCTYPESOFT")
public interface SOFTSystemEventProcI
extends CallbackI {
    public static final Callback.Descriptor DESCRIPTOR = new Callback.Descriptor(MethodHandles.lookup(), APIUtil.apiCreateCIF(LibFFI.ffi_type_void, LibFFI.ffi_type_sint32, LibFFI.ffi_type_sint32, LibFFI.ffi_type_pointer, LibFFI.ffi_type_sint32, LibFFI.ffi_type_pointer, LibFFI.ffi_type_pointer));

    @Override
    default public Callback.Descriptor getDescriptor() {
        return DESCRIPTOR;
    }

    @Override
    default public void callback(long ret, long args) {
        this.invoke(MemoryUtil.memGetInt(MemoryUtil.memGetAddress(args)), MemoryUtil.memGetInt(MemoryUtil.memGetAddress(args + (long)POINTER_SIZE)), MemoryUtil.memGetAddress(MemoryUtil.memGetAddress(args + (long)(2 * POINTER_SIZE))), MemoryUtil.memGetInt(MemoryUtil.memGetAddress(args + (long)(3 * POINTER_SIZE))), MemoryUtil.memGetAddress(MemoryUtil.memGetAddress(args + (long)(4 * POINTER_SIZE))), MemoryUtil.memGetAddress(MemoryUtil.memGetAddress(args + (long)(5 * POINTER_SIZE))));
    }

    public void invoke(@NativeType(value="ALCenum") int var1, @NativeType(value="ALCenum") int var2, @NativeType(value="ALCdevice *") long var3, @NativeType(value="ALCsizei") int var5, @NativeType(value="ALCchar const *") long var6, @NativeType(value="ALCvoid *") long var8);
}

