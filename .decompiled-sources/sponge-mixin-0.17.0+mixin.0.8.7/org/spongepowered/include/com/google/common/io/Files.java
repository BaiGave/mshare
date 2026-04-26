/*
 * Decompiled with CFR 0.152.
 */
package org.spongepowered.include.com.google.common.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import org.spongepowered.include.com.google.common.base.Preconditions;
import org.spongepowered.include.com.google.common.collect.ImmutableSet;
import org.spongepowered.include.com.google.common.collect.Lists;
import org.spongepowered.include.com.google.common.collect.TreeTraverser;
import org.spongepowered.include.com.google.common.io.ByteSink;
import org.spongepowered.include.com.google.common.io.ByteSource;
import org.spongepowered.include.com.google.common.io.ByteStreams;
import org.spongepowered.include.com.google.common.io.CharSink;
import org.spongepowered.include.com.google.common.io.CharSource;
import org.spongepowered.include.com.google.common.io.Closer;
import org.spongepowered.include.com.google.common.io.FileWriteMode;
import org.spongepowered.include.com.google.common.io.LineProcessor;
import org.spongepowered.include.com.google.errorprone.annotations.CanIgnoreReturnValue;

public final class Files {
    private static final TreeTraverser<File> FILE_TREE_TRAVERSER = new TreeTraverser<File>(){

        public String toString() {
            return "Files.fileTreeTraverser()";
        }
    };

    public static ByteSource asByteSource(File file) {
        return new FileByteSource(file);
    }

    static byte[] readFile(InputStream in, long expectedSize) throws IOException {
        if (expectedSize > Integer.MAX_VALUE) {
            throw new OutOfMemoryError("file is too large to fit in a byte array: " + expectedSize + " bytes");
        }
        return expectedSize == 0L ? ByteStreams.toByteArray(in) : ByteStreams.toByteArray(in, (int)expectedSize);
    }

    public static ByteSink asByteSink(File file, FileWriteMode ... modes) {
        return new FileByteSink(file, modes);
    }

    public static CharSource asCharSource(File file, Charset charset) {
        return Files.asByteSource(file).asCharSource(charset);
    }

    public static CharSink asCharSink(File file, Charset charset, FileWriteMode ... modes) {
        return Files.asByteSink(file, modes).asCharSink(charset);
    }

    private static FileWriteMode[] modes(boolean append) {
        FileWriteMode[] fileWriteModeArray;
        if (append) {
            FileWriteMode[] fileWriteModeArray2 = new FileWriteMode[1];
            fileWriteModeArray = fileWriteModeArray2;
            fileWriteModeArray2[0] = FileWriteMode.APPEND;
        } else {
            fileWriteModeArray = new FileWriteMode[]{};
        }
        return fileWriteModeArray;
    }

    public static void write(byte[] from, File to) throws IOException {
        Files.asByteSink(to, new FileWriteMode[0]).write(from);
    }

    public static void write(CharSequence from, File to, Charset charset) throws IOException {
        Files.asCharSink(to, charset, new FileWriteMode[0]).write(from);
    }

    public static void append(CharSequence from, File to, Charset charset) throws IOException {
        Files.write(from, to, charset, true);
    }

    private static void write(CharSequence from, File to, Charset charset, boolean append) throws IOException {
        Files.asCharSink(to, charset, Files.modes(append)).write(from);
    }

    public static List<String> readLines(File file, Charset charset) throws IOException {
        return Files.readLines(file, charset, new LineProcessor<List<String>>(){
            final List<String> result = Lists.newArrayList();

            @Override
            public boolean processLine(String line) {
                this.result.add(line);
                return true;
            }

            @Override
            public List<String> getResult() {
                return this.result;
            }
        });
    }

    @CanIgnoreReturnValue
    public static <T> T readLines(File file, Charset charset, LineProcessor<T> callback) throws IOException {
        return Files.asCharSource(file, charset).readLines(callback);
    }

    private static final class FileByteSink
    extends ByteSink {
        private final File file;
        private final ImmutableSet<FileWriteMode> modes;

        private FileByteSink(File file, FileWriteMode ... modes) {
            this.file = Preconditions.checkNotNull(file);
            this.modes = ImmutableSet.copyOf(modes);
        }

        @Override
        public FileOutputStream openStream() throws IOException {
            return new FileOutputStream(this.file, this.modes.contains((Object)FileWriteMode.APPEND));
        }

        public String toString() {
            return "Files.asByteSink(" + this.file + ", " + this.modes + ")";
        }
    }

    private static final class FileByteSource
    extends ByteSource {
        private final File file;

        private FileByteSource(File file) {
            this.file = Preconditions.checkNotNull(file);
        }

        @Override
        public FileInputStream openStream() throws IOException {
            return new FileInputStream(this.file);
        }

        @Override
        public byte[] read() throws IOException {
            try (Closer closer = Closer.create();){
                FileInputStream in = closer.register(this.openStream());
                byte[] byArray = Files.readFile(in, in.getChannel().size());
                return byArray;
            }
        }

        public String toString() {
            return "Files.asByteSource(" + this.file + ")";
        }
    }
}

