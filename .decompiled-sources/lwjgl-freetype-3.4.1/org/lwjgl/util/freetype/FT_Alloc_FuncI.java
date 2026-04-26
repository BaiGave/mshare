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
@NativeType(value="FT_Alloc_Func")
public interface FT_Alloc_FuncI
extends CallbackI {
    public static final Callback.Descriptor DESCRIPTOR = new Callback.Descriptor(MethodHandles.lookup(), APIUtil.apiCreateCIF(LibFFI.ffi_type_pointer, LibFFI.ffi_type_pointer, LibFFI.ffi_type_slong));

    @Override
    default public Callback.Descriptor getDescriptor() {
        return DESCRIPTOR;
    }

    @Override
    default public void callback(long ret, long args) {
        long __result = this.invoke(MemoryUtil.memGetAddress(MemoryUtil.memGetAddress(args)), MemoryUtil.memGetCLong(MemoryUtil.memGetAddress(args + (long)POINTER_SIZE)));
        APIUtil.apiClosureRetP(ret, __result);
    }

    @NativeType(value="void *")
    public long invoke(@NativeType(value="FT_Memory") long var1, long var3);
}

