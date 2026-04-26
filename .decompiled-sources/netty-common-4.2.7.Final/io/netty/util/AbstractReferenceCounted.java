/*
 * Decompiled with CFR 0.152.
 */
package io.netty.util;

import io.netty.util.ReferenceCounted;
import io.netty.util.internal.AtomicReferenceCountUpdater;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.ReferenceCountUpdater;
import io.netty.util.internal.UnsafeReferenceCountUpdater;
import io.netty.util.internal.VarHandleReferenceCountUpdater;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

public abstract class AbstractReferenceCounted
implements ReferenceCounted {
    private static final long REFCNT_FIELD_OFFSET;
    private static final AtomicIntegerFieldUpdater<AbstractReferenceCounted> AIF_UPDATER;
    private static final Object REFCNT_FIELD_VH;
    private static final ReferenceCountUpdater<AbstractReferenceCounted> updater;
    private volatile int refCnt = updater.initialValue();

    @Override
    public int refCnt() {
        return updater.refCnt(this);
    }

    protected final void setRefCnt(int refCnt) {
        updater.setRefCnt(this, refCnt);
    }

    @Override
    public ReferenceCounted retain() {
        return updater.retain(this);
    }

    @Override
    public ReferenceCounted retain(int increment) {
        return updater.retain(this, increment);
    }

    @Override
    public ReferenceCounted touch() {
        return this.touch(null);
    }

    @Override
    public boolean release() {
        return this.handleRelease(updater.release(this));
    }

    @Override
    public boolean release(int decrement) {
        return this.handleRelease(updater.release(this, decrement));
    }

    private boolean handleRelease(boolean result) {
        if (result) {
            this.deallocate();
        }
        return result;
    }

    protected abstract void deallocate();

    static {
        ReferenceCountUpdater.UpdaterType updaterType = ReferenceCountUpdater.updaterTypeOf(AbstractReferenceCounted.class, "refCnt");
        switch (updaterType) {
            case Atomic: {
                AIF_UPDATER = AtomicIntegerFieldUpdater.newUpdater(AbstractReferenceCounted.class, "refCnt");
                REFCNT_FIELD_OFFSET = -1L;
                REFCNT_FIELD_VH = null;
                updater = new AtomicReferenceCountUpdater<AbstractReferenceCounted>(){

                    @Override
                    protected AtomicIntegerFieldUpdater<AbstractReferenceCounted> updater() {
                        return AIF_UPDATER;
                    }
                };
                break;
            }
            case Unsafe: {
                AIF_UPDATER = null;
                REFCNT_FIELD_OFFSET = ReferenceCountUpdater.getUnsafeOffset(AbstractReferenceCounted.class, "refCnt");
                REFCNT_FIELD_VH = null;
                updater = new UnsafeReferenceCountUpdater<AbstractReferenceCounted>(){

                    @Override
                    protected long refCntFieldOffset() {
                        return REFCNT_FIELD_OFFSET;
                    }
                };
                break;
            }
            case VarHandle: {
                AIF_UPDATER = null;
                REFCNT_FIELD_OFFSET = -1L;
                REFCNT_FIELD_VH = PlatformDependent.findVarHandleOfIntField(MethodHandles.lookup(), AbstractReferenceCounted.class, "refCnt");
                updater = new VarHandleReferenceCountUpdater<AbstractReferenceCounted>(){

                    @Override
                    protected VarHandle varHandle() {
                        return (VarHandle)REFCNT_FIELD_VH;
                    }
                };
                break;
            }
            default: {
                throw new Error("Unexpected updater type for AbstractReferenceCounted: " + (Object)((Object)updaterType));
            }
        }
    }
}

