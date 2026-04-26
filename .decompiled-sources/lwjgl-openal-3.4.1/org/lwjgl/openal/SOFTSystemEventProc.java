/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.openal;

import org.jspecify.annotations.Nullable;
import org.lwjgl.openal.SOFTSystemEventProcI;
import org.lwjgl.system.Callback;

public abstract class SOFTSystemEventProc
extends Callback
implements SOFTSystemEventProcI {
    public static SOFTSystemEventProc create(long functionPointer) {
        SOFTSystemEventProcI instance = (SOFTSystemEventProcI)Callback.get(functionPointer);
        return instance instanceof SOFTSystemEventProc ? (SOFTSystemEventProc)instance : new Container(functionPointer, instance);
    }

    public static @Nullable SOFTSystemEventProc createSafe(long functionPointer) {
        return functionPointer == 0L ? null : SOFTSystemEventProc.create(functionPointer);
    }

    public static SOFTSystemEventProc create(SOFTSystemEventProcI instance) {
        return instance instanceof SOFTSystemEventProc ? (SOFTSystemEventProc)instance : new Container(instance.address(), instance);
    }

    protected SOFTSystemEventProc() {
        super(DESCRIPTOR);
    }

    SOFTSystemEventProc(long functionPointer) {
        super(functionPointer);
    }

    private static final class Container
    extends SOFTSystemEventProc {
        private final SOFTSystemEventProcI delegate;

        Container(long functionPointer, SOFTSystemEventProcI delegate) {
            super(functionPointer);
            this.delegate = delegate;
        }

        @Override
        public void invoke(int eventType, int deviceType, long device, int length, long message, long userParam) {
            this.delegate.invoke(eventType, deviceType, device, length, message, userParam);
        }
    }
}

