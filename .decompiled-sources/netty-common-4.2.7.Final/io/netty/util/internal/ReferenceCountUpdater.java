/*
 * Decompiled with CFR 0.152.
 */
package io.netty.util.internal;

import io.netty.util.IllegalReferenceCountException;
import io.netty.util.ReferenceCounted;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PlatformDependent;

public abstract class ReferenceCountUpdater<T extends ReferenceCounted> {
    protected ReferenceCountUpdater() {
    }

    protected abstract void safeInitializeRawRefCnt(T var1, int var2);

    protected abstract int getAndAddRawRefCnt(T var1, int var2);

    protected abstract int getRawRefCnt(T var1);

    protected abstract int getAcquireRawRefCnt(T var1);

    protected abstract void setReleaseRawRefCnt(T var1, int var2);

    protected abstract boolean casRawRefCnt(T var1, int var2, int var3);

    public final int initialValue() {
        return 2;
    }

    public final void setInitialValue(T instance) {
        this.safeInitializeRawRefCnt(instance, this.initialValue());
    }

    private static int realRefCnt(int rawCnt) {
        return rawCnt != 2 && rawCnt != 4 && (rawCnt & 1) != 0 ? 0 : rawCnt >>> 1;
    }

    private static int toLiveRealRefCnt(int rawCnt, int decrement) {
        if (rawCnt == 2 || rawCnt == 4 || (rawCnt & 1) == 0) {
            return rawCnt >>> 1;
        }
        throw new IllegalReferenceCountException(0, -decrement);
    }

    public final int refCnt(T instance) {
        return ReferenceCountUpdater.realRefCnt(this.getAcquireRawRefCnt(instance));
    }

    public final boolean isLiveNonVolatile(T instance) {
        int rawCnt = this.getRawRefCnt(instance);
        return rawCnt == 2 || rawCnt == 4 || rawCnt == 6 || rawCnt == 8 || (rawCnt & 1) == 0;
    }

    public final void setRefCnt(T instance, int refCnt) {
        int rawRefCnt = refCnt > 0 ? refCnt << 1 : 1;
        this.setReleaseRawRefCnt(instance, rawRefCnt);
    }

    public final void resetRefCnt(T instance) {
        this.setReleaseRawRefCnt(instance, this.initialValue());
    }

    public final T retain(T instance) {
        return this.retain0(instance, 1, 2);
    }

    public final T retain(T instance, int increment) {
        int rawIncrement = ObjectUtil.checkPositive(increment, "increment") << 1;
        return this.retain0(instance, increment, rawIncrement);
    }

    private T retain0(T instance, int increment, int rawIncrement) {
        int oldRef = this.getAndAddRawRefCnt(instance, rawIncrement);
        if (oldRef != 2 && oldRef != 4 && (oldRef & 1) != 0) {
            throw new IllegalReferenceCountException(0, increment);
        }
        if (oldRef <= 0 && oldRef + rawIncrement >= 0 || oldRef >= 0 && oldRef + rawIncrement < oldRef) {
            this.getAndAddRawRefCnt(instance, -rawIncrement);
            throw new IllegalReferenceCountException(ReferenceCountUpdater.realRefCnt(oldRef), increment);
        }
        return instance;
    }

    public final boolean release(T instance) {
        int rawCnt = this.getRawRefCnt(instance);
        return rawCnt == 2 ? this.tryFinalRelease0(instance, 2) || this.retryRelease0(instance, 1) : this.nonFinalRelease0(instance, 1, rawCnt, ReferenceCountUpdater.toLiveRealRefCnt(rawCnt, 1));
    }

    public final boolean release(T instance, int decrement) {
        int rawCnt = this.getRawRefCnt(instance);
        int realCnt = ReferenceCountUpdater.toLiveRealRefCnt(rawCnt, ObjectUtil.checkPositive(decrement, "decrement"));
        return decrement == realCnt ? this.tryFinalRelease0(instance, rawCnt) || this.retryRelease0(instance, decrement) : this.nonFinalRelease0(instance, decrement, rawCnt, realCnt);
    }

    private boolean tryFinalRelease0(T instance, int expectRawCnt) {
        return this.casRawRefCnt(instance, expectRawCnt, 1);
    }

    private boolean nonFinalRelease0(T instance, int decrement, int rawCnt, int realCnt) {
        if (decrement < realCnt && this.casRawRefCnt(instance, rawCnt, rawCnt - (decrement << 1))) {
            return false;
        }
        return this.retryRelease0(instance, decrement);
    }

    private boolean retryRelease0(T instance, int decrement) {
        while (true) {
            int rawCnt;
            int realCnt;
            if (decrement == (realCnt = ReferenceCountUpdater.toLiveRealRefCnt(rawCnt = this.getRawRefCnt(instance), decrement))) {
                if (this.tryFinalRelease0(instance, rawCnt)) {
                    return true;
                }
            } else if (decrement < realCnt) {
                if (this.casRawRefCnt(instance, rawCnt, rawCnt - (decrement << 1))) {
                    return false;
                }
            } else {
                throw new IllegalReferenceCountException(realCnt, -decrement);
            }
            Thread.yield();
        }
    }

    public static <T extends ReferenceCounted> UpdaterType updaterTypeOf(Class<T> clz, String fieldName) {
        long fieldOffset = ReferenceCountUpdater.getUnsafeOffset(clz, fieldName);
        if (fieldOffset >= 0L) {
            return UpdaterType.Unsafe;
        }
        if (PlatformDependent.hasVarHandle()) {
            return UpdaterType.VarHandle;
        }
        return UpdaterType.Atomic;
    }

    public static long getUnsafeOffset(Class<? extends ReferenceCounted> clz, String fieldName) {
        try {
            if (PlatformDependent.hasUnsafe()) {
                return PlatformDependent.objectFieldOffset(clz.getDeclaredField(fieldName));
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return -1L;
    }

    public static enum UpdaterType {
        Unsafe,
        VarHandle,
        Atomic;

    }
}

