/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.compressors.gzip;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import org.apache.commons.compress.compressors.FileNameUtil;

public class GzipUtils {
    static final int FCOMMENT = 16;
    static final int FEXTRA = 4;
    static final int FHCRC = 2;
    private static final FileNameUtil fileNameUtil;
    static final int FNAME = 8;
    static final int FRESERVED = 224;
    static final Charset GZIP_ENCODING;
    static final int ID1 = 31;
    static final int ID2 = 139;
    static final byte XFL_MAX_COMPRESSION = 2;
    static final byte XFL_MAX_SPEED = 4;
    static final byte XFL_UNKNOWN = 0;

    @Deprecated
    public static String getCompressedFilename(String fileName) {
        return fileNameUtil.getCompressedFileName(fileName);
    }

    public static String getCompressedFileName(String fileName) {
        return fileNameUtil.getCompressedFileName(fileName);
    }

    @Deprecated
    public static String getUncompressedFilename(String fileName) {
        return fileNameUtil.getUncompressedFileName(fileName);
    }

    public static String getUncompressedFileName(String fileName) {
        return fileNameUtil.getUncompressedFileName(fileName);
    }

    @Deprecated
    public static boolean isCompressedFilename(String fileName) {
        return fileNameUtil.isCompressedFileName(fileName);
    }

    public static boolean isCompressedFileName(String fileName) {
        return fileNameUtil.isCompressedFileName(fileName);
    }

    private GzipUtils() {
    }

    static {
        GZIP_ENCODING = StandardCharsets.ISO_8859_1;
        LinkedHashMap<String, String> uncompressSuffix = new LinkedHashMap<String, String>();
        uncompressSuffix.put(".tgz", ".tar");
        uncompressSuffix.put(".taz", ".tar");
        uncompressSuffix.put(".svgz", ".svg");
        uncompressSuffix.put(".cpgz", ".cpio");
        uncompressSuffix.put(".wmz", ".wmf");
        uncompressSuffix.put(".emz", ".emf");
        uncompressSuffix.put(".gz", "");
        uncompressSuffix.put(".z", "");
        uncompressSuffix.put("-gz", "");
        uncompressSuffix.put("-z", "");
        uncompressSuffix.put("_z", "");
        fileNameUtil = new FileNameUtil(uncompressSuffix, ".gz");
    }
}

