/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json.implementation.jackson.core;

import java.io.Serializable;

public class StreamReadConstraints
implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int DEFAULT_MAX_DEPTH = 1000;
    public static final long DEFAULT_MAX_DOC_LEN = -1L;
    public static final int DEFAULT_MAX_NUM_LEN = 1000;
    public static final int DEFAULT_MAX_STRING_LEN = 20000000;
    public static final int DEFAULT_MAX_NAME_LEN = 50000;
    protected final int _maxNestingDepth;
    protected final long _maxDocLen;
    protected final int _maxNumLen;
    protected final int _maxStringLen;
    protected final int _maxNameLen;

    @Deprecated
    protected StreamReadConstraints(int maxNestingDepth, long maxDocLen, int maxNumLen, int maxStringLen) {
        this(maxNestingDepth, -1L, maxNumLen, maxStringLen, 50000);
    }

    protected StreamReadConstraints(int maxNestingDepth, long maxDocLen, int maxNumLen, int maxStringLen, int maxNameLen) {
        this._maxNestingDepth = maxNestingDepth;
        this._maxDocLen = maxDocLen;
        this._maxNumLen = maxNumLen;
        this._maxStringLen = maxStringLen;
        this._maxNameLen = maxNameLen;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final long maxDocLen;
        private final int maxNestingDepth;
        private final int maxNumLen;
        private final int maxStringLen;
        private final int maxNameLen;

        Builder() {
            this(1000, -1L, 1000, 20000000, 50000);
        }

        Builder(int maxNestingDepth, long maxDocLen, int maxNumLen, int maxStringLen, int maxNameLen) {
            this.maxNestingDepth = maxNestingDepth;
            this.maxDocLen = maxDocLen;
            this.maxNumLen = maxNumLen;
            this.maxStringLen = maxStringLen;
            this.maxNameLen = maxNameLen;
        }

        public StreamReadConstraints build() {
            return new StreamReadConstraints(this.maxNestingDepth, this.maxDocLen, this.maxNumLen, this.maxStringLen, this.maxNameLen);
        }
    }
}

