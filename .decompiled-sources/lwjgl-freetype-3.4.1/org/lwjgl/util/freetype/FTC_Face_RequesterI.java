/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.util.freetype;

import java.lang.invoke.MethodHandles;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.Callback;
import org.lwjgl.system.CallbackI;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.libffi.LibFFI;

@FunctionalInterface
@NativeType(value="FTC_Face_Requester")
public interface FTC_Face_RequesterI
extends CallbackI {
    public static final Callback.Descriptor DESCRIPTOR = new Callback.Descriptor(MethodHandles.lookup(), APIUtil.apiCreateCIF(LibFFI.ffi_type_sint32, LibFFI.ffi_type_pointer, LibFFI.ffi_type_pointer, LibFFI.ffi_type_pointer, LibFFI.ffi_type_pointer));

    @Override
    default public Callback.Descriptor getDescriptor() {
        return DESCRIPTOR;
    }

    @Override
    default public void callback(long ret, long args) {
        int __result = this.invoke(MemoryUtil.memGetAddress(MemoryUtil.memGetAddress(args)), MemoryUtil.memGetAddress(MemoryUtil.memGetAddress(args + (long)POINTER_SIZE)), MemoryUtil.memGetAddress(MemoryUtil.memGetAddress(args + (long)(2 * POINTER_SIZE))), MemoryUtil.memGetAddress(MemoryUtil.memGetAddress(args + (long)(3 * POINTER_SIZE))));
        APIUtil.apiClosureRet(ret, __result);
    }

    @NativeType(value="FT_Error")
    public int invoke(@NativeType(value="FTC_FaceID") long var1, @NativeType(value="FT_Library") long var3, @NativeType(value="FT_Pointer") long var5, @NativeType(value="FT_Face *") long var7);
}

