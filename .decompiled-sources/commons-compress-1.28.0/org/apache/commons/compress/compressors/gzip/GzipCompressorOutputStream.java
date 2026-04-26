/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.compressors.gzip;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipParameters;

public class GzipCompressorOutputStream
extends CompressorOutputStream<OutputStream> {
    private final Deflater deflater;
    private final byte[] deflateBuffer;
    private final CRC32 crc = new CRC32();

    public GzipCompressorOutputStream(OutputStream out) throws IOException {
        this(out, new GzipParameters());
    }

    public GzipCompressorOutputStream(OutputStream out, GzipParameters parameters) throws IOException {
        super(out);
        this.deflater = new Deflater(parameters.getCompressionLevel(), true);
        this.deflater.setStrategy(parameters.getDeflateStrategy());
        this.deflateBuffer = new byte[parameters.getBufferSize()];
        this.writeMemberHeader(parameters);
    }

    @Override
    public void close() throws IOException {
        if (!this.isClosed()) {
            try {
                this.finish();
            }
            finally {
                this.deflater.end();
                super.close();
            }
        }
    }

    private void deflate() throws IOException {
        int length = this.deflater.deflate(this.deflateBuffer, 0, this.deflateBuffer.length);
        if (length > 0) {
            this.out.write(this.deflateBuffer, 0, length);
        }
    }

    @Override
    public void finish() throws IOException {
        if (!this.deflater.finished()) {
            this.deflater.finish();
            while (!this.deflater.finished()) {
                this.deflate();
            }
            this.writeMemberTrailer();
            this.deflater.reset();
        }
    }

    @Override
    public void write(byte[] buffer) throws IOException {
        this.write(buffer, 0, buffer.length);
    }

    @Override
    public void write(byte[] buffer, int offset, int length) throws IOException {
        this.checkOpen();
        if (this.deflater.finished()) {
            throw new IOException("Cannot write more data, the end of the compressed data stream has been reached.");
        }
        if (length > 0) {
            this.deflater.setInput(buffer, offset, length);
            while (!this.deflater.needsInput()) {
                this.deflate();
            }
            this.crc.update(buffer, offset, length);
        }
    }

    @Override
    public void write(int b) throws IOException {
        this.write(new byte[]{(byte)(b & 0xFF)}, 0, 1);
    }

    private void writeC(String value, Charset charset) throws IOException {
        if (value != null) {
            byte[] ba = value.getBytes(charset);
            this.out.write(ba);
            this.out.write(0);
            this.crc.update(ba);
            this.crc.update(0);
        }
    }

    private void writeMemberHeader(GzipParameters parameters) throws IOException {
        String fileName = parameters.getFileName();
        String comment = parameters.getComment();
        byte[] extra = parameters.getExtraField() != null ? parameters.getExtraField().toByteArray() : null;
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte)31);
        buffer.put((byte)-117);
        buffer.put((byte)8);
        buffer.put((byte)((extra != null ? 4 : 0) | (fileName != null ? 8 : 0) | (comment != null ? 16 : 0) | (parameters.getHeaderCRC() ? 2 : 0)));
        buffer.putInt((int)parameters.getModificationInstant().getEpochSecond());
        int compressionLevel = parameters.getCompressionLevel();
        if (compressionLevel == 9) {
            buffer.put((byte)2);
        } else if (compressionLevel == 1) {
            buffer.put((byte)4);
        } else {
            buffer.put((byte)0);
        }
        buffer.put((byte)parameters.getOperatingSystem());
        this.out.write(buffer.array());
        this.crc.update(buffer.array());
        if (extra != null) {
            this.out.write(extra.length & 0xFF);
            this.out.write(extra.length >>> 8 & 0xFF);
            this.out.write(extra);
            this.crc.update(extra.length & 0xFF);
            this.crc.update(extra.length >>> 8 & 0xFF);
            this.crc.update(extra);
        }
        this.writeC(fileName, parameters.getFileNameCharset());
        this.writeC(comment, parameters.getFileNameCharset());
        if (parameters.getHeaderCRC()) {
            int v = (int)this.crc.getValue() & 0xFFFF;
            this.out.write(v & 0xFF);
            this.out.write(v >>> 8 & 0xFF);
        }
        this.crc.reset();
    }

    private void writeMemberTrailer() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt((int)this.crc.getValue());
        buffer.putInt(this.deflater.getTotalIn());
        this.out.write(buffer.array());
    }
}

