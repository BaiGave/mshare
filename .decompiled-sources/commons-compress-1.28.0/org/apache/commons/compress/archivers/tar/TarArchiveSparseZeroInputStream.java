/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.archivers.tar;

import java.io.InputStream;

final class TarArchiveSparseZeroInputStream
extends InputStream {
    TarArchiveSparseZeroInputStream() {
    }

    @Override
    public int read() {
        return 0;
    }

    @Override
    public long skip(long n) {
        return n;
    }
}

