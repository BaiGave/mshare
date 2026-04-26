/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.channels;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.util.Objects;

public final class FileChannels {
    @Deprecated
    public static boolean contentEquals(FileChannel channel1, FileChannel channel2, int bufferCapacity) throws IOException {
        return FileChannels.contentEquals((SeekableByteChannel)channel1, (SeekableByteChannel)channel2, bufferCapacity);
    }

    public static boolean contentEquals(ReadableByteChannel channel1, ReadableByteChannel channel2, int bufferCapacity) throws IOException {
        if (Objects.equals(channel1, channel2)) {
            return true;
        }
        ByteBuffer c1Buffer = ByteBuffer.allocateDirect(bufferCapacity);
        ByteBuffer c2Buffer = ByteBuffer.allocateDirect(bufferCapacity);
        int c1NumRead = 0;
        int c2NumRead = 0;
        boolean c1Read0 = false;
        boolean c2Read0 = false;
        while (true) {
            if (!c2Read0) {
                c1NumRead = FileChannels.readToLimit(channel1, c1Buffer);
                c1Buffer.clear();
                boolean bl = c1Read0 = c1NumRead == 0;
            }
            if (!c1Read0) {
                c2NumRead = FileChannels.readToLimit(channel2, c2Buffer);
                c2Buffer.clear();
                boolean bl = c2Read0 = c2NumRead == 0;
            }
            if (c1NumRead == -1 && c2NumRead == -1) {
                return c1Buffer.equals(c2Buffer);
            }
            if (c1NumRead == 0 || c2NumRead == 0) {
                Thread.yield();
                continue;
            }
            if (c1NumRead != c2NumRead) {
                return false;
            }
            if (!c1Buffer.equals(c2Buffer)) break;
        }
        return false;
    }

    public static boolean contentEquals(SeekableByteChannel channel1, SeekableByteChannel channel2, int bufferCapacity) throws IOException {
        long size2;
        if (Objects.equals(channel1, channel2)) {
            return true;
        }
        long size1 = FileChannels.size(channel1);
        if (size1 != (size2 = FileChannels.size(channel2))) {
            return false;
        }
        return size1 == 0L && size2 == 0L || FileChannels.contentEquals((ReadableByteChannel)channel1, (ReadableByteChannel)channel2, bufferCapacity);
    }

    private static int readToLimit(ReadableByteChannel channel, ByteBuffer dst) throws IOException {
        int numRead;
        if (!dst.hasRemaining()) {
            throw new IllegalArgumentException();
        }
        int totalRead = 0;
        while (dst.hasRemaining() && (numRead = channel.read(dst)) != -1) {
            if (numRead == 0) {
                Thread.yield();
                continue;
            }
            totalRead += numRead;
        }
        return totalRead != 0 ? totalRead : -1;
    }

    private static long size(SeekableByteChannel channel) throws IOException {
        return channel != null ? channel.size() : 0L;
    }

    private FileChannels() {
    }
}

