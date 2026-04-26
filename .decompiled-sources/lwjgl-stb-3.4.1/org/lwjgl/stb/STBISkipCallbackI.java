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
@NativeType(value="void (*) (void *, int)")
public interface STBISkipCallbackI
extends CallbackI {
    public static final Callback.Descriptor DESCRIPTOR = new Callback.Descriptor(MethodHandles.lookup(), APIUtil.apiCreateCIF(LibFFI.ffi_type_void, LibFFI.ffi_type_pointer, LibFFI.ffi_type_sint32));

    @Override
    default public Callback.Descriptor getDescriptor() {
        return DESCRIPTOR;
    }

    @Override
    default public void callback(long ret, long args) {
        this.invoke(MemoryUtil.memGetAddress(MemoryUtil.memGetAddress(args)), MemoryUtil.memGetInt(MemoryUtil.memGetAddress(args + (long)POINTER_SIZE)));
    }

    public void invoke(@NativeType(value="void *") long var1, int var3);
}

