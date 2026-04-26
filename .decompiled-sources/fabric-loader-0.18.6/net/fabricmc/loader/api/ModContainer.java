/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.api;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.ModOrigin;

public interface ModContainer {
    public ModMetadata getMetadata();

    public List<Path> getRootPaths();

    default public Optional<Path> findPath(String file) {
        for (Path root : this.getRootPaths()) {
            Path path = root.resolve(file.replace("/", root.getFileSystem().getSeparator()));
            if (!Files.exists(path, new LinkOption[0])) continue;
            return Optional.of(path);
        }
        return Optional.empty();
    }

    public ModOrigin getOrigin();

    public Optional<ModContainer> getContainingMod();

    public Collection<ModContainer> getContainedMods();

    @Deprecated
    default public Path getRoot() {
        return this.getRootPath();
    }

    @Deprecated
    public Path getRootPath();

    @Deprecated
    public Path getPath(String var1);
}

