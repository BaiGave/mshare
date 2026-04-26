/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.tinyremapper;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public final class FileSystemReference
implements Closeable {
    private static final Map<FileSystem, Set<FileSystemReference>> openFsMap = new IdentityHashMap<FileSystem, Set<FileSystemReference>>();
    private final FileSystem fileSystem;
    private volatile boolean closed;

    public static FileSystemReference openJar(Path path) throws IOException {
        return FileSystemReference.openJar(path, false);
    }

    public static FileSystemReference openJar(Path path, boolean create) throws IOException {
        return FileSystemReference.open(FileSystemReference.toJarUri(path), create);
    }

    private static URI toJarUri(Path path) {
        URI uri = path.toUri();
        try {
            return new URI("jar:" + uri.getScheme(), uri.getHost(), uri.getPath(), uri.getFragment());
        }
        catch (URISyntaxException e) {
            throw new RuntimeException("can't convert path " + path + " to uri", e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static FileSystemReference open(URI uri, boolean create) throws IOException {
        Map<FileSystem, Set<FileSystemReference>> map = openFsMap;
        synchronized (map) {
            boolean opened = false;
            FileSystem fs = null;
            try {
                fs = FileSystems.getFileSystem(uri);
            }
            catch (FileSystemNotFoundException e) {
                try {
                    fs = FileSystems.newFileSystem(uri, create ? Collections.singletonMap("create", "true") : Collections.emptyMap());
                    opened = true;
                }
                catch (FileSystemAlreadyExistsException f) {
                    fs = FileSystems.getFileSystem(uri);
                }
            }
            FileSystemReference ret = new FileSystemReference(fs);
            Set<FileSystemReference> refs = openFsMap.get(fs);
            if (refs == null) {
                refs = Collections.newSetFromMap(new IdentityHashMap());
                openFsMap.put(fs, refs);
                if (!opened) {
                    refs.add(null);
                }
            } else if (opened) {
                throw new IllegalStateException("opened but already in refs?");
            }
            refs.add(ret);
            return ret;
        }
    }

    private FileSystemReference(FileSystem fs) {
        this.fileSystem = fs;
    }

    public boolean isReadOnly() {
        if (this.closed) {
            throw new IllegalStateException("fs closed");
        }
        return this.fileSystem.isReadOnly();
    }

    public Path getPath(String first, String ... more) {
        if (this.closed) {
            throw new IllegalStateException("fs closed");
        }
        return this.fileSystem.getPath(first, more);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void close() throws IOException {
        Map<FileSystem, Set<FileSystemReference>> map = openFsMap;
        synchronized (map) {
            if (this.closed) {
                return;
            }
            this.closed = true;
            Set<FileSystemReference> refs = openFsMap.get(this.fileSystem);
            if (refs == null || !refs.remove(this)) {
                throw new IllegalStateException("fs " + this.fileSystem + " was already closed");
            }
            if (refs.isEmpty()) {
                openFsMap.remove(this.fileSystem);
                this.fileSystem.close();
            } else if (refs.size() == 1 && refs.contains(null)) {
                openFsMap.remove(this.fileSystem);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String toString() {
        Map<FileSystem, Set<FileSystemReference>> map = openFsMap;
        synchronized (map) {
            Set refs = openFsMap.getOrDefault(this.fileSystem, Collections.emptySet());
            return String.format("%s=%dx,%s", this.fileSystem, refs.size(), refs.contains(null) ? "existing" : "new");
        }
    }
}

