/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.compressors.zstandard;

import com.github.luben.zstd.Zstd;

public class ZstdConstants {
    public static final int ZSTD_CHAINLOG_MAX = Zstd.chainLogMax();
    public static final int ZSTD_CHAINLOG_MIN = Zstd.chainLogMin();
    public static final int ZSTD_CLEVEL_DEFAULT = Zstd.defaultCompressionLevel();
    public static final int ZSTD_CLEVEL_MAX = Zstd.maxCompressionLevel();
    public static final int ZSTD_CLEVEL_MIN = Zstd.minCompressionLevel();
    public static final int ZSTD_HASHLOG_MAX = Zstd.hashLogMax();
    public static final int ZSTD_HASHLOG_MIN = Zstd.hashLogMin();
    public static final int ZSTD_MINMATCH_MAX = 7;
    public static final int ZSTD_MINMATCH_MIN = 3;
    public static final int ZSTD_SEARCHLOG_MAX = Zstd.searchLogMax();
    public static final int ZSTD_SEARCHLOG_MIN = Zstd.searchLogMin();
    public static final int ZSTD_WINDOWLOG_LIMIT_DEFAULT = 27;
    public static final int ZSTD_WINDOWLOG_MAX = Zstd.windowLogMax();
    public static final int ZSTD_WINDOWLOG_MIN = Zstd.windowLogMin();
}

