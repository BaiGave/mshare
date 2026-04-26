/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.io;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteSource;
import com.google.common.io.TempFileCreator;
import com.google.errorprone.annotations.concurrent.GuardedBy;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

@Beta
@J2ktIncompatible
@GwtIncompatible
public final class FileBackedOutputStream
extends OutputStream {
    private final int fileThreshold;
    private final boolean resetOnFinalize;
    private final ByteSource source;
    @GuardedBy(value="this")
    private OutputStream out;
    @GuardedBy(value="this")
    private @Nullable MemoryOutput memory;
    @GuardedBy(value="this")
    private @Nullable File file;

    @VisibleForTesting
    synchronized @Nullable File getFile() {
        return this.file;
    }

    public FileBackedOutputStream(int fileThreshold) {
        this(fileThreshold, false);
    }

    public FileBackedOutputStream(int fileThreshold, boolean resetOnFinalize) {
        Preconditions.checkArgument(fileThreshold >= 0, "fileThreshold must be non-negative, but was %s", fileThreshold);
        this.fileThreshold = fileThreshold;
        this.resetOnFinalize = resetOnFinalize;
        this.memory = new MemoryOutput();
        this.out = this.memory;
        this.source = resetOnFinalize ? new ByteSource(){

            @Override
            public InputStream openStream() throws IOException {
                return FileBackedOutputStream.this.openInputStream();
            }

            protected void finalize() {
                try {
                    FileBackedOutputStream.this.reset();
                }
                catch (Throwable t) {
                    t.printStackTrace(System.err);
                }
            }
        } : new ByteSource(){

            @Override
            public InputStream openStream() throws IOException {
                return FileBackedOutputStream.this.openInputStream();
            }
        };
    }

    public ByteSource asByteSource() {
        return this.source;
    }

    private synchronized InputStream openInputStream() throws IOException {
        if (this.file != null) {
            return new FileInputStream(this.file);
        }
        Objects.requireNonNull(this.memory);
        return new ByteArrayInputStream(this.memory.getBuffer(), 0, this.memory.getCount());
    }

    public synchronized void reset() throws IOException {
        try {
            this.close();
        }
        finally {
            if (this.memory == null) {
                this.memory = new MemoryOutput();
            } else {
                this.memory.reset();
            }
            this.out = this.memory;
            if (this.file != null) {
                File deleteMe = this.file;
                this.file = null;
                if (!deleteMe.delete()) {
                    throw new IOException("Could not delete: " + deleteMe);
                }
            }
        }
    }

    @Override
    public synchronized void write(int b) throws IOException {
        this.update(1);
        this.out.write(b);
    }

    @Override
    public synchronized void write(byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        this.update(len);
        this.out.write(b, off, len);
    }

    @Override
    public synchronized void close() throws IOException {
        this.out.close();
    }

    @Override
    public synchronized void flush() throws IOException {
        this.out.flush();
    }

    @GuardedBy(value="this")
    private void update(int len) throws IOException {
        if (this.memory != null && this.memory.getCount() + len > this.fileThreshold) {
            File temp = TempFileCreator.INSTANCE.createTempFile("FileBackedOutputStream");
            if (this.resetOnFinalize) {
                temp.deleteOnExit();
            }
            try {
                FileOutputStream transfer = new FileOutputStream(temp);
                transfer.write(this.memory.getBuffer(), 0, this.memory.getCount());
                transfer.flush();
                this.out = transfer;
            }
            catch (IOException e) {
                temp.delete();
                throw e;
            }
            this.file = temp;
            this.memory = null;
        }
    }

    private static final class MemoryOutput
    extends ByteArrayOutputStream {
        private MemoryOutput() {
        }

        byte[] getBuffer() {
            return this.buf;
        }

        int getCount() {
            return this.count;
        }
    }
}

