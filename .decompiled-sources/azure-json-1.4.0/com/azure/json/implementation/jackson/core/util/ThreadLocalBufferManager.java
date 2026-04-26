/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json.implementation.jackson.core.util;

import com.azure.json.implementation.jackson.core.util.BufferRecycler;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class ThreadLocalBufferManager {
    private final Map<SoftReference<BufferRecycler>, Boolean> _trackedRecyclers = new ConcurrentHashMap<SoftReference<BufferRecycler>, Boolean>();
    private final ReferenceQueue<BufferRecycler> _refQueue = new ReferenceQueue();

    ThreadLocalBufferManager() {
    }

    public static ThreadLocalBufferManager instance() {
        return ThreadLocalBufferManagerHolder.manager;
    }

    public SoftReference<BufferRecycler> wrapAndTrack(BufferRecycler br) {
        SoftReference<BufferRecycler> newRef = new SoftReference<BufferRecycler>(br, this._refQueue);
        this._trackedRecyclers.put(newRef, true);
        this.removeSoftRefsClearedByGc();
        return newRef;
    }

    private void removeSoftRefsClearedByGc() {
        SoftReference clearedSoftRef;
        while ((clearedSoftRef = (SoftReference)this._refQueue.poll()) != null) {
            this._trackedRecyclers.remove(clearedSoftRef);
        }
    }

    private static final class ThreadLocalBufferManagerHolder {
        static final ThreadLocalBufferManager manager = new ThreadLocalBufferManager();

        private ThreadLocalBufferManagerHolder() {
        }
    }
}

