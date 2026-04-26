/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.openal;

import org.jspecify.annotations.Nullable;
import org.lwjgl.openal.EXTDebugProcI;
import org.lwjgl.system.Callback;

public abstract class EXTDebugProc
extends Callback
implements EXTDebugProcI {
    public static EXTDebugProc create(long functionPointer) {
        EXTDebugProcI instance = (EXTDebugProcI)Callback.get(functionPointer);
        return instance instanceof EXTDebugProc ? (EXTDebugProc)instance : new Container(functionPointer, instance);
    }

    public static @Nullable EXTDebugProc createSafe(long functionPointer) {
        return functionPointer == 0L ? null : EXTDebugProc.create(functionPointer);
    }

    public static EXTDebugProc create(EXTDebugProcI instance) {
        return instance instanceof EXTDebugProc ? (EXTDebugProc)instance : new Container(instance.address(), instance);
    }

    protected EXTDebugProc() {
        super(DESCRIPTOR);
    }

    EXTDebugProc(long functionPointer) {
        super(functionPointer);
    }

    private static final class Container
    extends EXTDebugProc {
        private final EXTDebugProcI delegate;

        Container(long functionPointer, EXTDebugProcI delegate) {
            super(functionPointer);
            this.delegate = delegate;
        }

        @Override
        public void invoke(int source, int type, int id, int severity, int length, long message, long userParam) {
            this.delegate.invoke(source, type, id, severity, length, message, userParam);
        }
    }
}

