/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress;

import org.apache.commons.compress.CompressException;

public class MemoryLimitException
extends CompressException {
    private static final long serialVersionUID = 1L;
    private final long memoryNeededKiB;
    private final int memoryLimitKiB;

    private static String buildMessage(long memoryNeededInKb, int memoryLimitInKb) {
        return String.format("%,d KiB of memory would be needed; limit was %,d KiB. If the file is not corrupt, consider increasing the memory limit.", memoryNeededInKb, memoryLimitInKb);
    }

    public MemoryLimitException(long memoryNeededKiB, int memoryLimitKiB) {
        super(MemoryLimitException.buildMessage(memoryNeededKiB, memoryLimitKiB));
        this.memoryNeededKiB = memoryNeededKiB;
        this.memoryLimitKiB = memoryLimitKiB;
    }

    @Deprecated
    public MemoryLimitException(long memoryNeededKiB, int memoryLimitKiB, Exception cause) {
        super(MemoryLimitException.buildMessage(memoryNeededKiB, memoryLimitKiB), cause);
        this.memoryNeededKiB = memoryNeededKiB;
        this.memoryLimitKiB = memoryLimitKiB;
    }

    public MemoryLimitException(long memoryNeededKiB, int memoryLimitKiB, Throwable cause) {
        super(MemoryLimitException.buildMessage(memoryNeededKiB, memoryLimitKiB), cause);
        this.memoryNeededKiB = memoryNeededKiB;
        this.memoryLimitKiB = memoryLimitKiB;
    }

    public int getMemoryLimitInKb() {
        return this.memoryLimitKiB;
    }

    public long getMemoryNeededInKb() {
        return this.memoryNeededKiB;
    }
}

