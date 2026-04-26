/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.async;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.Sequence;
import org.apache.logging.log4j.core.async.RingBufferLogEvent;

class RingBufferLogEventHandler4
implements EventHandler<RingBufferLogEvent> {
    private static final int NOTIFY_PROGRESS_THRESHOLD = 50;
    private Sequence sequenceCallback;
    private int counter;
    private long threadId = -1L;

    RingBufferLogEventHandler4() {
    }

    public void setSequenceCallback(Sequence sequenceCallback) {
        this.sequenceCallback = sequenceCallback;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void onEvent(RingBufferLogEvent event, long sequence, boolean endOfBatch) throws Exception {
        try {
            if (event.isPopulated()) {
                event.execute(endOfBatch);
            }
        }
        finally {
            event.clear();
            this.notifyCallback(sequence);
        }
    }

    private void notifyCallback(long sequence) {
        if (++this.counter > 50) {
            this.sequenceCallback.set(sequence);
            this.counter = 0;
        }
    }

    public long getThreadId() {
        return this.threadId;
    }

    public void onStart() {
        this.threadId = Thread.currentThread().getId();
    }

    public void onShutdown() {
    }
}

