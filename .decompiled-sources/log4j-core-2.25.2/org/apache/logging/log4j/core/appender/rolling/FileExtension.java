/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.rolling;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.util.Objects;
import org.apache.logging.log4j.core.appender.rolling.action.Action;
import org.apache.logging.log4j.core.appender.rolling.action.CommonsCompressAction;
import org.apache.logging.log4j.core.appender.rolling.action.GzCompressAction;
import org.apache.logging.log4j.core.appender.rolling.action.ZipCompressAction;

public enum FileExtension {
    ZIP(".zip"){

        @Override
        public Action createCompressAction(String renameTo, String compressedName, boolean deleteSource, int compressionLevel) {
            return new ZipCompressAction(this.source(renameTo), this.target(compressedName), deleteSource, compressionLevel);
        }
    }
    ,
    GZ(".gz"){

        @Override
        public Action createCompressAction(String renameTo, String compressedName, boolean deleteSource, int compressionLevel) {
            return new GzCompressAction(this.source(renameTo), this.target(compressedName), deleteSource, compressionLevel);
        }
    }
    ,
    BZIP2(".bz2"){

        @Override
        public Action createCompressAction(String renameTo, String compressedName, boolean deleteSource, int compressionLevel) {
            return new CommonsCompressAction("bzip2", this.source(renameTo), this.target(compressedName), deleteSource);
        }
    }
    ,
    DEFLATE(".deflate"){

        @Override
        public Action createCompressAction(String renameTo, String compressedName, boolean deleteSource, int compressionLevel) {
            return new CommonsCompressAction("deflate", this.source(renameTo), this.target(compressedName), deleteSource);
        }
    }
    ,
    PACK200(".pack200"){

        @Override
        public Action createCompressAction(String renameTo, String compressedName, boolean deleteSource, int compressionLevel) {
            return new CommonsCompressAction("pack200", this.source(renameTo), this.target(compressedName), deleteSource);
        }
    }
    ,
    XZ(".xz"){

        @Override
        public Action createCompressAction(String renameTo, String compressedName, boolean deleteSource, int compressionLevel) {
            return new CommonsCompressAction("xz", this.source(renameTo), this.target(compressedName), deleteSource);
        }
    }
    ,
    ZSTD(".zst"){

        @Override
        public Action createCompressAction(String renameTo, String compressedName, boolean deleteSource, int compressionLevel) {
            return new CommonsCompressAction("zstd", this.source(renameTo), this.target(compressedName), deleteSource);
        }
    };

    private final String extension;

    public static FileExtension lookup(String fileExtension) {
        for (FileExtension ext : FileExtension.values()) {
            if (!ext.isExtensionFor(fileExtension)) continue;
            return ext;
        }
        return null;
    }

    public static FileExtension lookupForFile(String fileName) {
        for (FileExtension ext : FileExtension.values()) {
            if (!fileName.endsWith(ext.extension)) continue;
            return ext;
        }
        return null;
    }

    private FileExtension(String extension) {
        Objects.requireNonNull(extension, "extension");
        this.extension = extension;
    }

    public abstract Action createCompressAction(String var1, String var2, boolean var3, int var4);

    public String getExtension() {
        return this.extension;
    }

    boolean isExtensionFor(String s) {
        return s.endsWith(this.extension);
    }

    int length() {
        return this.extension.length();
    }

    @SuppressFBWarnings(value={"PATH_TRAVERSAL_IN"}, justification="The name of the accessed files is based on a configuration value.")
    File source(String fileName) {
        return new File(fileName);
    }

    @SuppressFBWarnings(value={"PATH_TRAVERSAL_IN"}, justification="The name of the accessed files is based on a configuration value.")
    File target(String fileName) {
        return new File(fileName);
    }
}

