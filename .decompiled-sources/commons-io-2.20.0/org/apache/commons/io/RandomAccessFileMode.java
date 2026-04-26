/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import org.apache.commons.io.IORandomAccessFile;
import org.apache.commons.io.function.IOConsumer;
import org.apache.commons.io.function.IOFunction;

public enum RandomAccessFileMode {
    READ_ONLY("r", 1),
    READ_WRITE("rw", 2),
    READ_WRITE_SYNC_ALL("rws", 4),
    READ_WRITE_SYNC_CONTENT("rwd", 3);

    private static final String R = "r";
    private static final String RW = "rw";
    private static final String RWD = "rwd";
    private static final String RWS = "rws";
    private final int level;
    private final String mode;

    public static RandomAccessFileMode valueOf(OpenOption ... openOption) {
        RandomAccessFileMode bestFit = READ_ONLY;
        block5: for (OpenOption option : openOption) {
            if (!(option instanceof StandardOpenOption)) continue;
            switch ((StandardOpenOption)option) {
                case WRITE: {
                    if (bestFit.implies(READ_WRITE)) continue block5;
                    bestFit = READ_WRITE;
                    continue block5;
                }
                case DSYNC: {
                    if (bestFit.implies(READ_WRITE_SYNC_CONTENT)) continue block5;
                    bestFit = READ_WRITE_SYNC_CONTENT;
                    continue block5;
                }
                case SYNC: {
                    if (bestFit.implies(READ_WRITE_SYNC_ALL)) continue block5;
                    bestFit = READ_WRITE_SYNC_ALL;
                    continue block5;
                }
                default: {
                    continue block5;
                }
            }
        }
        return bestFit;
    }

    public static RandomAccessFileMode valueOfMode(String mode) {
        switch (mode) {
            case "r": {
                return READ_ONLY;
            }
            case "rw": {
                return READ_WRITE;
            }
            case "rwd": {
                return READ_WRITE_SYNC_CONTENT;
            }
            case "rws": {
                return READ_WRITE_SYNC_ALL;
            }
        }
        throw new IllegalArgumentException(mode);
    }

    private RandomAccessFileMode(String mode, int level) {
        this.mode = mode;
        this.level = level;
    }

    public void accept(Path file, IOConsumer<RandomAccessFile> consumer) throws IOException {
        try (RandomAccessFile raf = this.create(file);){
            consumer.accept(raf);
        }
    }

    public <T> T apply(Path file, IOFunction<RandomAccessFile, T> function) throws IOException {
        try (RandomAccessFile raf = this.create(file);){
            T t = function.apply(raf);
            return t;
        }
    }

    public RandomAccessFile create(File file) throws FileNotFoundException {
        return new IORandomAccessFile(file, this.mode);
    }

    public RandomAccessFile create(Path file) throws FileNotFoundException {
        return this.create(Objects.requireNonNull(file.toFile(), "file"));
    }

    public RandomAccessFile create(String name) throws FileNotFoundException {
        return new IORandomAccessFile(name, this.mode);
    }

    private int getLevel() {
        return this.level;
    }

    public String getMode() {
        return this.mode;
    }

    public boolean implies(RandomAccessFileMode other) {
        return this.getLevel() >= other.getLevel();
    }

    public IORandomAccessFile io(String name) throws FileNotFoundException {
        return new IORandomAccessFile(name, this.mode);
    }
}

