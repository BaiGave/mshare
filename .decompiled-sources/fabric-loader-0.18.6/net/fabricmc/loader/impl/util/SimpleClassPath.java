/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipError;
import java.util.zip.ZipFile;
import net.fabricmc.loader.impl.util.LoaderUtil;

public final class SimpleClassPath
implements Closeable {
    private final List<Path> paths;
    private final boolean[] jarMarkers;
    private final ZipFile[] openJars;

    public SimpleClassPath(List<Path> paths) {
        this.paths = paths;
        this.jarMarkers = new boolean[paths.size()];
        this.openJars = new ZipFile[paths.size()];
        for (int i = 0; i < this.jarMarkers.length; ++i) {
            if (Files.isDirectory(paths.get(i), new LinkOption[0])) continue;
            this.jarMarkers[i] = true;
        }
    }

    @Override
    public void close() throws IOException {
        IOException exc = null;
        for (int i = 0; i < this.openJars.length; ++i) {
            ZipFile file = this.openJars[i];
            try {
                if (file != null) {
                    file.close();
                }
            }
            catch (IOException e) {
                if (exc == null) {
                    exc = e;
                }
                exc.addSuppressed(e);
            }
            this.openJars[i] = null;
        }
        if (exc != null) {
            throw exc;
        }
    }

    public List<Path> getPaths() {
        return this.paths;
    }

    public CpEntry getEntry(String subPath) throws IOException {
        for (int i = 0; i < this.jarMarkers.length; ++i) {
            if (this.jarMarkers[i]) {
                ZipEntry entry;
                ZipFile zf = this.openJars[i];
                if (zf == null) {
                    Path path = this.paths.get(i);
                    try {
                        this.openJars[i] = zf = new ZipFile(path.toFile());
                    }
                    catch (IOException | ZipError e) {
                        throw new IOException(String.format("error opening %s: %s", LoaderUtil.normalizePath(path), e), e);
                    }
                }
                if ((entry = zf.getEntry(subPath)) == null) continue;
                return new CpEntry(i, subPath, entry);
            }
            Path file = this.paths.get(i).resolve(subPath);
            if (!Files.isRegularFile(file, new LinkOption[0])) continue;
            return new CpEntry(i, subPath, file);
        }
        return null;
    }

    public InputStream getInputStream(String subPath) throws IOException {
        CpEntry entry = this.getEntry(subPath);
        return entry != null ? entry.getInputStream() : null;
    }

    public final class CpEntry {
        private final int idx;
        private final String subPath;
        private final Object instance;

        private CpEntry(int idx, String subPath, Object instance) {
            this.idx = idx;
            this.subPath = subPath;
            this.instance = instance;
        }

        public Path getOrigin() {
            return (Path)SimpleClassPath.this.paths.get(this.idx);
        }

        public String getSubPath() {
            return this.subPath;
        }

        public InputStream getInputStream() throws IOException {
            if (this.instance instanceof ZipEntry) {
                return SimpleClassPath.this.openJars[this.idx].getInputStream((ZipEntry)this.instance);
            }
            return Files.newInputStream((Path)this.instance, new OpenOption[0]);
        }

        public String toString() {
            return String.format("%s:%s", this.getOrigin(), this.subPath);
        }
    }
}

