/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.stb;

import java.lang.invoke.MethodHandles;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.Callback;
import org.lwjgl.system.CallbackI;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.libffi.LibFFI;

@FunctionalInterface
@NativeType(value="unsigned char * (*) (unsigned char *, int, int *, int)")
public interface STBIZlibCompressI
extends CallbackI {
    public static final Callback.Descriptor DESCRIPTOR = new Callback.Descriptor(MethodHandles.lookup(), APIUtil.apiCreateCIF(LibFFI.ffi_type_pointer, LibFFI.ffi_type_pointer, LibFFI.ffi_type_sint32, LibFFI.ffi_type_pointer, LibFFI.ffi_type_sint32));

    @Override
    default public Callback.Descriptor getDescriptor() {
        return DESCRIPTOR;
    }

    @Override
    default public void callback(long ret, long args) {
        long __result = this.invoke(MemoryUtil.memGetAddress(MemoryUtil.memGetAddress(args)), MemoryUtil.memGetInt(MemoryUtil.memGetAddress(args + (long)POINTER_SIZE)), MemoryUtil.memGetAddress(MemoryUtil.memGetAddress(args + (long)(2 * POINTER_SIZE))), MemoryUtil.memGetInt(MemoryUtil.memGetAddress(args + (long)(3 * POINTER_SIZE))));
        APIUtil.apiClosureRetP(ret, __result);
    }

    @NativeType(value="unsigned char *")
    public long invoke(@NativeType(value="unsigned char *") long var1, int var3, @NativeType(value="int *") long var4, int var6);
}

