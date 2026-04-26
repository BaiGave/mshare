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
@NativeType(value="FT_Incremental_GetGlyphDataFunc")
public interface FT_Incremental_GetGlyphDataFuncI
extends CallbackI {
    public static final Callback.Descriptor DESCRIPTOR = new Callback.Descriptor(MethodHandles.lookup(), APIUtil.apiCreateCIF(LibFFI.ffi_type_sint32, LibFFI.ffi_type_pointer, LibFFI.ffi_type_uint32, LibFFI.ffi_type_pointer));

    @Override
    default public Callback.Descriptor getDescriptor() {
        return DESCRIPTOR;
    }

    @Override
    default public void callback(long ret, long args) {
        int __result = this.invoke(MemoryUtil.memGetAddress(MemoryUtil.memGetAddress(args)), MemoryUtil.memGetInt(MemoryUtil.memGetAddress(args + (long)POINTER_SIZE)), MemoryUtil.memGetAddress(MemoryUtil.memGetAddress(args + (long)(2 * POINTER_SIZE))));
        APIUtil.apiClosureRet(ret, __result);
    }

    @NativeType(value="FT_Error")
    public int invoke(@NativeType(value="FT_Incremental") long var1, @NativeType(value="FT_UInt") int var3, @NativeType(value="FT_Data *") long var4);
}

