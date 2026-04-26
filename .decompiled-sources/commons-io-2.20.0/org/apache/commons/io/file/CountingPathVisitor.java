/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.file;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.function.UnaryOperator;
import org.apache.commons.io.file.Counters;
import org.apache.commons.io.file.PathFilter;
import org.apache.commons.io.file.SimplePathVisitor;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SymbolicLinkFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.function.IOBiFunction;

public class CountingPathVisitor
extends SimplePathVisitor {
    static final String[] EMPTY_STRING_ARRAY = new String[0];
    private final Counters.PathCounters pathCounters;
    private final PathFilter fileFilter;
    private final PathFilter directoryFilter;
    private final UnaryOperator<Path> directoryPostTransformer;

    static IOFileFilter defaultDirectoryFilter() {
        return TrueFileFilter.INSTANCE;
    }

    static UnaryOperator<Path> defaultDirectoryTransformer() {
        return UnaryOperator.identity();
    }

    static IOFileFilter defaultFileFilter() {
        return new SymbolicLinkFileFilter(FileVisitResult.TERMINATE, FileVisitResult.CONTINUE);
    }

    static Counters.PathCounters defaultPathCounters() {
        return Counters.longPathCounters();
    }

    public static CountingPathVisitor withBigIntegerCounters() {
        return ((Builder)new Builder().setPathCounters(Counters.bigIntegerPathCounters())).get();
    }

    public static CountingPathVisitor withLongCounters() {
        return ((Builder)new Builder().setPathCounters(Counters.longPathCounters())).get();
    }

    CountingPathVisitor(AbstractBuilder<?, ?> builder) {
        super(builder);
        this.pathCounters = builder.getPathCounters();
        this.fileFilter = builder.getFileFilter();
        this.directoryFilter = builder.getDirectoryFilter();
        this.directoryPostTransformer = builder.getDirectoryPostTransformer();
    }

    public CountingPathVisitor(Counters.PathCounters pathCounters) {
        this((AbstractBuilder<?, ?>)new Builder().setPathCounters(pathCounters));
    }

    public CountingPathVisitor(Counters.PathCounters pathCounters, PathFilter fileFilter, PathFilter directoryFilter) {
        this.pathCounters = Objects.requireNonNull(pathCounters, "pathCounters");
        this.fileFilter = Objects.requireNonNull(fileFilter, "fileFilter");
        this.directoryFilter = Objects.requireNonNull(directoryFilter, "directoryFilter");
        this.directoryPostTransformer = UnaryOperator.identity();
    }

    @Deprecated
    public CountingPathVisitor(Counters.PathCounters pathCounters, PathFilter fileFilter, PathFilter directoryFilter, IOBiFunction<Path, IOException, FileVisitResult> visitFileFailed) {
        super(visitFileFailed);
        this.pathCounters = Objects.requireNonNull(pathCounters, "pathCounters");
        this.fileFilter = Objects.requireNonNull(fileFilter, "fileFilter");
        this.directoryFilter = Objects.requireNonNull(directoryFilter, "directoryFilter");
        this.directoryPostTransformer = UnaryOperator.identity();
    }

    protected boolean accept(Path file, BasicFileAttributes attributes) {
        return Files.exists(file, new LinkOption[0]) && this.fileFilter.accept(file, attributes) == FileVisitResult.CONTINUE;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CountingPathVisitor)) {
            return false;
        }
        CountingPathVisitor other = (CountingPathVisitor)obj;
        return Objects.equals(this.pathCounters, other.pathCounters);
    }

    public Counters.PathCounters getPathCounters() {
        return this.pathCounters;
    }

    public int hashCode() {
        return Objects.hash(this.pathCounters);
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        this.updateDirCounter((Path)this.directoryPostTransformer.apply(dir), exc);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attributes) throws IOException {
        FileVisitResult accept = this.directoryFilter.accept(dir, attributes);
        return accept != FileVisitResult.CONTINUE ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
    }

    public String toString() {
        return this.pathCounters.toString();
    }

    protected void updateDirCounter(Path dir, IOException exc) {
        this.pathCounters.getDirectoryCounter().increment();
    }

    protected void updateFileCounters(Path file, BasicFileAttributes attributes) {
        this.pathCounters.getFileCounter().increment();
        this.pathCounters.getByteCounter().add(attributes.size());
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
        if (this.accept(file, attributes)) {
            this.updateFileCounters(file, attributes);
        }
        return FileVisitResult.CONTINUE;
    }

    public static class Builder
    extends AbstractBuilder<CountingPathVisitor, Builder> {
        @Override
        public CountingPathVisitor get() {
            return new CountingPathVisitor(this);
        }
    }

    public static abstract class AbstractBuilder<T, B extends AbstractBuilder<T, B>>
    extends SimplePathVisitor.AbstractBuilder<T, B> {
        private Counters.PathCounters pathCounters = CountingPathVisitor.defaultPathCounters();
        private PathFilter fileFilter = CountingPathVisitor.defaultFileFilter();
        private PathFilter directoryFilter = CountingPathVisitor.defaultDirectoryFilter();
        private UnaryOperator<Path> directoryPostTransformer = CountingPathVisitor.defaultDirectoryTransformer();

        PathFilter getDirectoryFilter() {
            return this.directoryFilter;
        }

        UnaryOperator<Path> getDirectoryPostTransformer() {
            return this.directoryPostTransformer;
        }

        PathFilter getFileFilter() {
            return this.fileFilter;
        }

        Counters.PathCounters getPathCounters() {
            return this.pathCounters;
        }

        public B setDirectoryFilter(PathFilter directoryFilter) {
            this.directoryFilter = directoryFilter != null ? directoryFilter : CountingPathVisitor.defaultDirectoryFilter();
            return (B)((AbstractBuilder)this.asThis());
        }

        public B setDirectoryPostTransformer(UnaryOperator<Path> directoryTransformer) {
            this.directoryPostTransformer = directoryTransformer != null ? directoryTransformer : CountingPathVisitor.defaultDirectoryTransformer();
            return (B)((AbstractBuilder)this.asThis());
        }

        public B setFileFilter(PathFilter fileFilter) {
            this.fileFilter = fileFilter != null ? fileFilter : CountingPathVisitor.defaultFileFilter();
            return (B)((AbstractBuilder)this.asThis());
        }

        public B setPathCounters(Counters.PathCounters pathCounters) {
            this.pathCounters = pathCounters != null ? pathCounters : CountingPathVisitor.defaultPathCounters();
            return (B)((AbstractBuilder)this.asThis());
        }
    }
}

