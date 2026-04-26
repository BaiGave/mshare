/*
 * Decompiled with CFR 0.152.
 */
package io.netty.buffer;

import io.netty.buffer.AbstractByteBuf;
import io.netty.buffer.ByteBuf;
import io.netty.util.internal.AtomicReferenceCountUpdater;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.ReferenceCountUpdater;
import io.netty.util.internal.UnsafeReferenceCountUpdater;
import io.netty.util.internal.VarHandleReferenceCountUpdater;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

public abstract class AbstractReferenceCountedByteBuf
extends AbstractByteBuf {
    private static final long REFCNT_FIELD_OFFSET;
    private static final AtomicIntegerFieldUpdater<AbstractReferenceCountedByteBuf> AIF_UPDATER;
    private static final Object REFCNT_FIELD_VH;
    private static final ReferenceCountUpdater<AbstractReferenceCountedByteBuf> updater;
    private volatile int refCnt;

    protected AbstractReferenceCountedByteBuf(int maxCapacity) {
        super(maxCapacity);
        updater.setInitialValue(this);
    }

    @Override
    boolean isAccessible() {
        return updater.isLiveNonVolatile(this);
    }

    @Override
    public int refCnt() {
        return updater.refCnt(this);
    }

    protected final void setRefCnt(int refCnt) {
        updater.setRefCnt(this, refCnt);
    }

    protected final void resetRefCnt() {
        updater.resetRefCnt(this);
    }

    @Override
    public ByteBuf retain() {
        return updater.retain(this);
    }

    @Override
    public ByteBuf retain(int increment) {
        return updater.retain(this, increment);
    }

    @Override
    public ByteBuf touch() {
        return this;
    }

    @Override
    public ByteBuf touch(Object hint) {
        return this;
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
        ReferenceCountUpdater.UpdaterType updaterType = ReferenceCountUpdater.updaterTypeOf(AbstractReferenceCountedByteBuf.class, "refCnt");
        switch (updaterType) {
            case Atomic: {
                AIF_UPDATER = AtomicIntegerFieldUpdater.newUpdater(AbstractReferenceCountedByteBuf.class, "refCnt");
                REFCNT_FIELD_OFFSET = -1L;
                REFCNT_FIELD_VH = null;
                updater = new AtomicReferenceCountUpdater<AbstractReferenceCountedByteBuf>(){

                    @Override
                    protected AtomicIntegerFieldUpdater<AbstractReferenceCountedByteBuf> updater() {
                        return AIF_UPDATER;
                    }
                };
                break;
            }
            case Unsafe: {
                AIF_UPDATER = null;
                REFCNT_FIELD_OFFSET = ReferenceCountUpdater.getUnsafeOffset(AbstractReferenceCountedByteBuf.class, "refCnt");
                REFCNT_FIELD_VH = null;
                updater = new UnsafeReferenceCountUpdater<AbstractReferenceCountedByteBuf>(){

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
                REFCNT_FIELD_VH = PlatformDependent.findVarHandleOfIntField(MethodHandles.lookup(), AbstractReferenceCountedByteBuf.class, "refCnt");
                updater = new VarHandleReferenceCountUpdater<AbstractReferenceCountedByteBuf>(){

                    @Override
                    protected VarHandle varHandle() {
                        return (VarHandle)REFCNT_FIELD_VH;
                    }
                };
                break;
            }
            default: {
                throw new Error("Unexpected updater type for AbstractReferenceCountedByteBuf: " + (Object)((Object)updaterType));
            }
        }
    }
}

