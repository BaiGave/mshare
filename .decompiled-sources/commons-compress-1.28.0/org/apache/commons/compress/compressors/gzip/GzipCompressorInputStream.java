/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.compressors.gzip;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.zip.CRC32;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.ExtraField;
import org.apache.commons.compress.compressors.gzip.GzipParameters;
import org.apache.commons.compress.compressors.gzip.GzipUtils;
import org.apache.commons.compress.utils.ByteUtils;
import org.apache.commons.compress.utils.InputStreamStatistics;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.function.IOConsumer;
import org.apache.commons.io.input.BoundedInputStream;

public class GzipCompressorInputStream
extends CompressorInputStream
implements InputStreamStatistics {
    private static final IOConsumer<GzipCompressorInputStream> NOOP = IOConsumer.noop();
    private final byte[] buf = new byte[8192];
    private int bufUsed;
    private final BoundedInputStream countingStream;
    private final CRC32 crc = new CRC32();
    private final boolean decompressConcatenated;
    private boolean endReached;
    private final Charset fileNameCharset;
    private final InputStream in;
    private Inflater inflater = new Inflater(true);
    private final byte[] oneByte = new byte[1];
    private GzipParameters parameters;
    private final IOConsumer<GzipCompressorInputStream> onMemberStart;
    private final IOConsumer<GzipCompressorInputStream> onMemberEnd;

    public static Builder builder() {
        return new Builder();
    }

    public static boolean matches(byte[] signature, int length) {
        return length >= 2 && signature[0] == 31 && signature[1] == -117;
    }

    private static byte[] readToNull(DataInput inData) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();){
            int b;
            while ((b = inData.readUnsignedByte()) != 0) {
                bos.write(b);
            }
            byte[] byArray = bos.toByteArray();
            return byArray;
        }
    }

    private GzipCompressorInputStream(Builder builder) throws IOException {
        this.countingStream = ((BoundedInputStream.Builder)BoundedInputStream.builder().setInputStream(builder.getInputStream())).get();
        this.in = this.countingStream.markSupported() ? this.countingStream : new BufferedInputStream(this.countingStream);
        this.decompressConcatenated = builder.decompressConcatenated;
        this.fileNameCharset = builder.fileNameCharset;
        this.onMemberStart = builder.onMemberStart != null ? builder.onMemberStart : NOOP;
        this.onMemberEnd = builder.onMemberEnd != null ? builder.onMemberEnd : NOOP;
        this.init(true);
    }

    public GzipCompressorInputStream(InputStream inputStream) throws IOException {
        this((Builder)GzipCompressorInputStream.builder().setInputStream(inputStream));
    }

    @Deprecated
    public GzipCompressorInputStream(InputStream inputStream, boolean decompressConcatenated) throws IOException {
        this(((Builder)GzipCompressorInputStream.builder().setInputStream(inputStream)).setDecompressConcatenated(decompressConcatenated));
    }

    @Override
    public void close() throws IOException {
        if (this.inflater != null) {
            this.inflater.end();
            this.inflater = null;
        }
        if (this.in != System.in) {
            this.in.close();
        }
    }

    @Override
    public long getCompressedCount() {
        return this.countingStream.getCount();
    }

    public GzipParameters getMetaData() {
        return this.parameters;
    }

    private boolean init(boolean isFirstMember) throws IOException {
        if (!isFirstMember && !this.decompressConcatenated) {
            throw new IllegalStateException("Unexpected: isFirstMember and decompressConcatenated are both false.");
        }
        int magic0 = this.in.read();
        if (magic0 == -1 && !isFirstMember) {
            return false;
        }
        if (magic0 != 31 || this.in.read() != 139) {
            throw new IOException(isFirstMember ? "Input is not in the .gz format." : "Unexpected data after a valid .gz stream.");
        }
        this.parameters = new GzipParameters();
        this.parameters.setFileNameCharset(this.fileNameCharset);
        DataInputStream inData = new DataInputStream(this.in);
        int method = inData.readUnsignedByte();
        if (method != 8) {
            throw new IOException("Unsupported compression method " + method + " in the .gz header");
        }
        int flg = inData.readUnsignedByte();
        if ((flg & 0xE0) != 0) {
            throw new IOException("Reserved flags are set in the .gz header.");
        }
        this.parameters.setModificationTime(ByteUtils.fromLittleEndian(inData, 4));
        switch (inData.readUnsignedByte()) {
            case 2: {
                this.parameters.setCompressionLevel(9);
                break;
            }
            case 4: {
                this.parameters.setCompressionLevel(1);
                break;
            }
            default: {
                this.parameters.setCompressionLevel(-1);
            }
        }
        this.parameters.setOperatingSystem(inData.readUnsignedByte());
        if ((flg & 4) != 0) {
            int xlen = inData.readUnsignedByte();
            byte[] extra = new byte[xlen |= inData.readUnsignedByte() << 8];
            inData.readFully(extra);
            this.parameters.setExtraField(ExtraField.fromBytes(extra));
        }
        if ((flg & 8) != 0) {
            this.parameters.setFileName(new String(GzipCompressorInputStream.readToNull(inData), this.parameters.getFileNameCharset()));
        }
        if ((flg & 0x10) != 0) {
            this.parameters.setComment(new String(GzipCompressorInputStream.readToNull(inData), this.parameters.getFileNameCharset()));
        }
        if ((flg & 2) != 0) {
            this.parameters.setHeaderCRC(true);
            inData.readShort();
        }
        this.inflater.reset();
        this.crc.reset();
        this.onMemberStart.accept(this);
        return true;
    }

    @Override
    public int read() throws IOException {
        return this.read(this.oneByte, 0, 1) == -1 ? -1 : this.oneByte[0] & 0xFF;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (len == 0) {
            return 0;
        }
        if (this.endReached) {
            return -1;
        }
        int size = 0;
        while (len > 0) {
            int ret;
            if (this.inflater.needsInput()) {
                this.in.mark(this.buf.length);
                this.bufUsed = this.in.read(this.buf);
                if (this.bufUsed == -1) {
                    throw new EOFException();
                }
                this.inflater.setInput(this.buf, 0, this.bufUsed);
            }
            try {
                ret = this.inflater.inflate(b, off, len);
            }
            catch (DataFormatException e) {
                throw new IOException("Gzip-compressed data is corrupt.", e);
            }
            this.crc.update(b, off, ret);
            off += ret;
            len -= ret;
            size += ret;
            this.count(ret);
            if (!this.inflater.finished()) continue;
            this.in.reset();
            int skipAmount = this.bufUsed - this.inflater.getRemaining();
            if (IOUtils.skip(this.in, (long)skipAmount) != (long)skipAmount) {
                throw new IOException();
            }
            this.bufUsed = 0;
            DataInputStream inData = new DataInputStream(this.in);
            long trailerCrc = ByteUtils.fromLittleEndian(inData, 4);
            if (trailerCrc != this.crc.getValue()) {
                throw new IOException("Gzip-compressed data is corrupt (CRC32 error).");
            }
            long iSize = ByteUtils.fromLittleEndian(inData, 4);
            if (iSize != (this.inflater.getBytesWritten() & 0xFFFFFFFFL)) {
                throw new IOException("Gzip-compressed data is corrupt (uncompressed size mismatch).");
            }
            this.parameters.setTrailerCrc(trailerCrc);
            this.parameters.setTrailerISize(iSize);
            this.onMemberEnd.accept(this);
            if (this.decompressConcatenated && this.init(false)) continue;
            this.inflater.end();
            this.inflater = null;
            this.endReached = true;
            return size == 0 ? -1 : size;
        }
        return size;
    }

    public static class Builder
    extends AbstractStreamBuilder<GzipCompressorInputStream, Builder> {
        private boolean decompressConcatenated;
        private Charset fileNameCharset = GzipUtils.GZIP_ENCODING;
        private IOConsumer<GzipCompressorInputStream> onMemberStart;
        private IOConsumer<GzipCompressorInputStream> onMemberEnd;

        @Override
        public GzipCompressorInputStream get() throws IOException {
            return new GzipCompressorInputStream(this);
        }

        public Builder setDecompressConcatenated(boolean decompressConcatenated) {
            this.decompressConcatenated = decompressConcatenated;
            return this;
        }

        public Builder setFileNameCharset(Charset fileNameCharset) {
            this.fileNameCharset = fileNameCharset;
            return this;
        }

        public Builder setOnMemberEnd(IOConsumer<GzipCompressorInputStream> onMemberEnd) {
            this.onMemberEnd = onMemberEnd;
            return this;
        }

        public Builder setOnMemberStart(IOConsumer<GzipCompressorInputStream> onMemberStart) {
            this.onMemberStart = onMemberStart;
            return this;
        }
    }
}

