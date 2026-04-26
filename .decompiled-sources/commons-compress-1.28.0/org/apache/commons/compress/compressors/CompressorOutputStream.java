/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.compressors;

import java.io.OutputStream;
import org.apache.commons.compress.CompressFilterOutputStream;

public abstract class CompressorOutputStream<T extends OutputStream>
extends CompressFilterOutputStream<T> {
    public CompressorOutputStream() {
    }

    public CompressorOutputStream(T out) {
        super(out);
    }
}

