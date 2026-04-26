/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.compressors.gzip;

import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Objects;
import org.apache.commons.compress.compressors.gzip.ExtraField;
import org.apache.commons.compress.compressors.gzip.GzipUtils;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class GzipParameters {
    private static final int BUFFER_SIZE = 512;
    private static final int OS_ACORN_RISCOS = 13;
    private static final int OS_AMIGA = 1;
    private static final int OS_ATARI_TOS = 5;
    private static final int OS_CPM = 9;
    private static final int OS_FAT = 0;
    private static final int OS_HPFS = 6;
    private static final int OS_MACINTOSH = 7;
    private static final int OS_NTFS = 11;
    private static final int OS_QDOS = 12;
    private static final int OS_TOPS_20 = 10;
    private static final int OS_UNIX = 3;
    private static final int OS_UNKNOWN = 255;
    private static final int OS_VM_CMS = 4;
    private static final int OS_VMS = 2;
    private static final int OS_Z_SYSTEM = 8;
    private int bufferSize = 512;
    private String comment;
    private int compressionLevel = -1;
    private int deflateStrategy = 0;
    private ExtraField extraField;
    private String fileName;
    private Charset fileNameCharset = GzipUtils.GZIP_ENCODING;
    private boolean headerCrc;
    private Instant modificationInstant = Instant.EPOCH;
    private OS operatingSystem = OS.UNKNOWN;
    private long trailerCrc;
    private long trailerISize;

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GzipParameters)) {
            return false;
        }
        GzipParameters other = (GzipParameters)obj;
        return this.bufferSize == other.bufferSize && Objects.equals(this.comment, other.comment) && this.compressionLevel == other.compressionLevel && this.deflateStrategy == other.deflateStrategy && Objects.equals(this.extraField, other.extraField) && Objects.equals(this.fileName, other.fileName) && Objects.equals(this.fileNameCharset, other.fileNameCharset) && this.headerCrc == other.headerCrc && Objects.equals(this.modificationInstant, other.modificationInstant) && this.operatingSystem == other.operatingSystem && this.trailerCrc == other.trailerCrc && this.trailerISize == other.trailerISize;
    }

    public int getBufferSize() {
        return this.bufferSize;
    }

    public String getComment() {
        return this.comment;
    }

    public int getCompressionLevel() {
        return this.compressionLevel;
    }

    public int getDeflateStrategy() {
        return this.deflateStrategy;
    }

    public ExtraField getExtraField() {
        return this.extraField;
    }

    @Deprecated
    public String getFilename() {
        return this.fileName;
    }

    public String getFileName() {
        return this.fileName;
    }

    public Charset getFileNameCharset() {
        return this.fileNameCharset;
    }

    public boolean getHeaderCRC() {
        return this.headerCrc;
    }

    public Instant getModificationInstant() {
        return this.modificationInstant;
    }

    public long getModificationTime() {
        return this.modificationInstant.getEpochSecond();
    }

    public int getOperatingSystem() {
        return this.operatingSystem.type;
    }

    public OS getOS() {
        return this.operatingSystem;
    }

    public long getTrailerCrc() {
        return this.trailerCrc;
    }

    public long getTrailerISize() {
        return this.trailerISize;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.bufferSize, this.comment, this.compressionLevel, this.deflateStrategy, this.extraField, this.fileName, this.fileNameCharset, this.headerCrc, this.modificationInstant, this.operatingSystem, this.trailerCrc, this.trailerISize});
    }

    private String requireNonNulByte(String text) {
        if (StringUtils.isNotEmpty(text) && ArrayUtils.contains(text.getBytes(this.fileNameCharset), (byte)0)) {
            throw new IllegalArgumentException("String encoded in Charset '" + this.fileNameCharset + "' contains the nul byte 0 which is not supported in gzip.");
        }
        return text;
    }

    public void setBufferSize(int bufferSize) {
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("invalid buffer size: " + bufferSize);
        }
        this.bufferSize = bufferSize;
    }

    public void setComment(String comment) {
        this.comment = this.requireNonNulByte(comment);
    }

    public void setCompressionLevel(int compressionLevel) {
        if (compressionLevel < -1 || compressionLevel > 9) {
            throw new IllegalArgumentException("Invalid gzip compression level: " + compressionLevel);
        }
        this.compressionLevel = compressionLevel;
    }

    public void setDeflateStrategy(int deflateStrategy) {
        this.deflateStrategy = deflateStrategy;
    }

    public void setExtraField(ExtraField extra) {
        this.extraField = extra;
    }

    @Deprecated
    public void setFilename(String fileName) {
        this.setFileName(fileName);
    }

    public void setFileName(String fileName) {
        this.fileName = this.requireNonNulByte(fileName);
    }

    public void setFileNameCharset(Charset charset) {
        this.fileNameCharset = Charsets.toCharset(charset, GzipUtils.GZIP_ENCODING);
    }

    public void setHeaderCRC(boolean headerCRC) {
        this.headerCrc = headerCRC;
    }

    public void setModificationInstant(Instant modificationTime) {
        this.modificationInstant = modificationTime != null ? modificationTime : Instant.EPOCH;
    }

    public void setModificationTime(long modificationTimeSeconds) {
        this.modificationInstant = Instant.ofEpochSecond(modificationTimeSeconds);
    }

    public void setOperatingSystem(int operatingSystem) {
        this.operatingSystem = OS.from(operatingSystem);
    }

    public void setOS(OS os) {
        this.operatingSystem = os != null ? os : OS.UNKNOWN;
    }

    void setTrailerCrc(long trailerCrc) {
        this.trailerCrc = trailerCrc;
    }

    void setTrailerISize(long trailerISize) {
        this.trailerISize = trailerISize;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("GzipParameters [bufferSize=").append(this.bufferSize).append(", comment=").append(this.comment).append(", compressionLevel=").append(this.compressionLevel).append(", deflateStrategy=").append(this.deflateStrategy).append(", extraField=").append(this.extraField).append(", fileName=").append(this.fileName).append(", fileNameCharset=").append(this.fileNameCharset).append(", headerCrc=").append(this.headerCrc).append(", modificationInstant=").append(this.modificationInstant).append(", operatingSystem=").append((Object)this.operatingSystem).append(", trailerCrc=").append(this.trailerCrc).append(", trailerISize=").append(this.trailerISize).append("]");
        return builder.toString();
    }

    public static enum OS {
        ACORN_RISCOS(13),
        AMIGA(1),
        ATARI_TOS(5),
        CPM(9),
        FAT(0),
        HPFS(6),
        MACINTOSH(7),
        NTFS(11),
        QDOS(12),
        TOPS_20(10),
        UNIX(3),
        UNKNOWN(255),
        VM_CMS(4),
        VMS(2),
        Z_SYSTEM(8);

        private final int type;

        public static OS from(int code) {
            switch (code) {
                case 13: {
                    return ACORN_RISCOS;
                }
                case 1: {
                    return AMIGA;
                }
                case 5: {
                    return ATARI_TOS;
                }
                case 9: {
                    return CPM;
                }
                case 0: {
                    return FAT;
                }
                case 6: {
                    return HPFS;
                }
                case 7: {
                    return MACINTOSH;
                }
                case 11: {
                    return NTFS;
                }
                case 12: {
                    return QDOS;
                }
                case 10: {
                    return TOPS_20;
                }
                case 3: {
                    return UNIX;
                }
                case 255: {
                    return UNKNOWN;
                }
                case 4: {
                    return VM_CMS;
                }
                case 2: {
                    return VMS;
                }
                case 8: {
                    return Z_SYSTEM;
                }
            }
            return UNKNOWN;
        }

        private OS(int type) {
            this.type = type;
        }

        public int type() {
            return this.type;
        }
    }
}

