/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.minisat.core;

import java.io.Serializable;

public class CircularBuffer
implements Serializable {
    private final int[] values;
    private int index = 0;
    private long sum = 0L;
    private boolean full = false;

    public CircularBuffer(int capacity) {
        this.values = new int[capacity];
    }

    public void push(int value) {
        if (!this.full) {
            this.values[this.index++] = value;
            this.sum += (long)value;
            if (this.index == this.values.length) {
                this.full = true;
                this.index = -1;
            }
            return;
        }
        ++this.index;
        if (this.index == this.values.length) {
            this.index = 0;
        }
        this.sum -= (long)this.values[this.index];
        this.values[this.index] = value;
        this.sum += (long)value;
    }

    public long average() {
        if (this.full) {
            return this.sum / (long)this.values.length;
        }
        if (this.index == 0) {
            return 0L;
        }
        return this.sum / (long)this.index;
    }

    public void clear() {
        this.index = 0;
        this.full = false;
        this.sum = 0L;
    }

    public boolean isFull() {
        return this.full;
    }
}

