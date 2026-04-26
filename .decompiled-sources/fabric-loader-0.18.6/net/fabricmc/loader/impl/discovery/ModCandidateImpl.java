/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.discovery;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.ModDependency;
import net.fabricmc.loader.impl.discovery.BuiltinMetadataWrapper;
import net.fabricmc.loader.impl.discovery.DomainObject;
import net.fabricmc.loader.impl.discovery.ModDiscoverer;
import net.fabricmc.loader.impl.discovery.ModLoadCondition;
import net.fabricmc.loader.impl.game.GameProvider;
import net.fabricmc.loader.impl.metadata.DependencyOverrides;
import net.fabricmc.loader.impl.metadata.LoaderModMetadata;
import net.fabricmc.loader.impl.metadata.VersionOverrides;

public final class ModCandidateImpl
implements DomainObject.Mod {
    static final Comparator<ModCandidateImpl> ID_VERSION_COMPARATOR = new Comparator<ModCandidateImpl>(){

        @Override
        public int compare(ModCandidateImpl a, ModCandidateImpl b) {
            int cmp = a.getId().compareTo(b.getId());
            return cmp != 0 ? cmp : a.getVersion().compareTo(b.getVersion());
        }
    };
    private final List<Path> originPaths;
    private List<Path> paths;
    private final String localPath;
    private final long hash;
    private final LoaderModMetadata metadata;
    private final boolean requiresRemap;
    private final Collection<ModCandidateImpl> nestedMods;
    private final Collection<ModCandidateImpl> parentMods;
    private int minNestLevel;
    private SoftReference<ByteBuffer> dataRef;
    private static final Pattern FILE_NAME_SANITIZING_PATTERN = Pattern.compile("[^\\w\\.\\-\\+]+");

    static ModCandidateImpl createBuiltin(GameProvider.BuiltinMod mod, VersionOverrides versionOverrides, DependencyOverrides depOverrides) {
        BuiltinMetadataWrapper metadata = new BuiltinMetadataWrapper(mod.metadata);
        versionOverrides.apply(metadata);
        depOverrides.apply(metadata);
        return new ModCandidateImpl(mod.paths, null, -1L, metadata, false, Collections.emptyList());
    }

    static ModCandidateImpl createPlain(List<Path> paths, LoaderModMetadata metadata, boolean requiresRemap, Collection<ModCandidateImpl> nestedMods) {
        return new ModCandidateImpl(paths, null, -1L, metadata, requiresRemap, nestedMods);
    }

    static ModCandidateImpl createNested(String localPath, long hash, LoaderModMetadata metadata, boolean requiresRemap, Collection<ModCandidateImpl> nestedMods) {
        return new ModCandidateImpl(null, localPath, hash, metadata, requiresRemap, nestedMods);
    }

    static long hash(ZipEntry entry) {
        if (entry.getSize() < 0L || entry.getCrc() < 0L) {
            throw new IllegalArgumentException("uninitialized entry: " + entry);
        }
        return entry.getCrc() << 32 | entry.getSize();
    }

    private static long getSize(long hash) {
        return hash & 0xFFFFFFFFL;
    }

    private ModCandidateImpl(List<Path> paths, String localPath, long hash, LoaderModMetadata metadata, boolean requiresRemap, Collection<ModCandidateImpl> nestedMods) {
        this.originPaths = paths;
        this.paths = paths;
        this.localPath = localPath;
        this.metadata = metadata;
        this.hash = hash;
        this.requiresRemap = requiresRemap;
        this.nestedMods = nestedMods;
        this.parentMods = paths == null ? new ArrayList() : Collections.emptyList();
        this.minNestLevel = paths != null ? 0 : Integer.MAX_VALUE;
    }

    public List<Path> getOriginPaths() {
        return this.originPaths;
    }

    public boolean hasPath() {
        return this.paths != null;
    }

    public List<Path> getPaths() {
        if (this.paths == null) {
            throw new IllegalStateException("no path set");
        }
        return this.paths;
    }

    public void setPaths(List<Path> paths) {
        if (paths == null) {
            throw new NullPointerException("null paths");
        }
        this.paths = paths;
        this.clearCachedData();
    }

    public String getLocalPath() {
        if (this.localPath != null) {
            return this.localPath;
        }
        if (this.paths.size() == 1) {
            return this.paths.get(0).toString();
        }
        return this.paths.toString();
    }

    public LoaderModMetadata getMetadata() {
        return this.metadata;
    }

    @Override
    public String getId() {
        return this.metadata.getId();
    }

    @Override
    public Version getVersion() {
        return this.metadata.getVersion();
    }

    public Collection<String> getProvides() {
        return this.metadata.getProvides();
    }

    public boolean isBuiltin() {
        return this.metadata.getType().equals("builtin");
    }

    public ModLoadCondition getLoadCondition() {
        return this.minNestLevel == 0 ? ModLoadCondition.ALWAYS : ModLoadCondition.IF_POSSIBLE;
    }

    public Collection<ModDependency> getDependencies() {
        return this.metadata.getDependencies();
    }

    public boolean getRequiresRemap() {
        return this.requiresRemap;
    }

    public Collection<ModCandidateImpl> getNestedMods() {
        return this.nestedMods;
    }

    public Collection<ModCandidateImpl> getParentMods() {
        return this.parentMods;
    }

    boolean addParent(ModCandidateImpl parent) {
        if (this.minNestLevel == 0) {
            return false;
        }
        if (this.parentMods.contains(parent)) {
            return false;
        }
        this.parentMods.add(parent);
        this.updateMinNestLevel(parent);
        return true;
    }

    public int getMinNestLevel() {
        return this.minNestLevel;
    }

    boolean resetMinNestLevel() {
        if (this.minNestLevel > 0) {
            this.minNestLevel = Integer.MAX_VALUE;
            return true;
        }
        return false;
    }

    boolean updateMinNestLevel(ModCandidateImpl parent) {
        if (this.minNestLevel <= parent.minNestLevel) {
            return false;
        }
        this.minNestLevel = parent.minNestLevel + 1;
        return true;
    }

    public boolean isRoot() {
        return this.minNestLevel == 0;
    }

    void setData(ByteBuffer data) {
        this.dataRef = new SoftReference<ByteBuffer>(data);
    }

    void clearCachedData() {
        this.dataRef = null;
    }

    public Path copyToDir(Path outputDir, boolean temp) throws IOException {
        Files.createDirectories(outputDir, new FileAttribute[0]);
        Path ret = null;
        try {
            if (temp) {
                ret = Files.createTempFile(outputDir, this.getId(), ".jar", new FileAttribute[0]);
            } else {
                ret = outputDir.resolve(this.getDefaultFileName());
                if (Files.exists(ret, new LinkOption[0])) {
                    if (Files.size(ret) == ModCandidateImpl.getSize(this.hash)) {
                        return ret;
                    }
                    Files.deleteIfExists(ret);
                }
            }
            this.copyToFile(ret);
        }
        catch (Throwable t) {
            if (ret != null) {
                Files.deleteIfExists(ret);
            }
            throw t;
        }
        return ret;
    }

    String getDefaultFileName() {
        String ret = String.format("%s-%s-%s.jar", this.getId(), FILE_NAME_SANITIZING_PATTERN.matcher(this.getVersion().getFriendlyString()).replaceAll("_"), Long.toHexString(ModCandidateImpl.mixHash(this.hash)));
        if (ret.length() > 64) {
            ret = ret.substring(0, 32).concat(ret.substring(ret.length() - 32));
        }
        return ret;
    }

    private static long mixHash(long hash) {
        hash ^= hash >>> 33;
        hash *= -49064778989728563L;
        hash ^= hash >>> 33;
        hash *= -4265267296055464877L;
        hash ^= hash >>> 33;
        return hash;
    }

    private void copyToFile(Path out) throws IOException {
        ByteBuffer data;
        SoftReference<ByteBuffer> dataRef = this.dataRef;
        if (dataRef != null && (data = dataRef.get()) != null) {
            Files.copy(new ByteArrayInputStream(data.array(), data.arrayOffset() + data.position(), data.arrayOffset() + data.limit()), out, StandardCopyOption.REPLACE_EXISTING);
            return;
        }
        if (this.paths != null) {
            if (this.paths.size() != 1) {
                throw new UnsupportedOperationException("multiple paths for " + this);
            }
            Files.copy(this.paths.get(0), out, new CopyOption[0]);
            return;
        }
        ModCandidateImpl parent = this.getBestSourcingParent();
        if (parent.paths != null) {
            if (parent.paths.size() != 1) {
                throw new UnsupportedOperationException("multiple parent paths for " + this);
            }
            try (ZipFile zf = new ZipFile(parent.paths.get(0).toFile());){
                ZipEntry entry = zf.getEntry(this.localPath);
                if (entry == null) {
                    throw new IOException(String.format("can't find nested mod %s in its parent mod %s", this, parent));
                }
                Files.copy(zf.getInputStream(entry), out, new CopyOption[0]);
            }
        }
        ByteBuffer data2 = parent.getData();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(data2.array(), data2.arrayOffset() + data2.position(), data2.arrayOffset() + data2.limit()));){
            ZipEntry entry = null;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.getName().equals(this.localPath)) continue;
                Files.copy(zis, out, new CopyOption[0]);
                return;
            }
        }
        throw new IOException(String.format("can't find nested mod %s in its parent mod %s", this, parent));
    }

    private ByteBuffer getData() throws IOException {
        ByteBuffer ret;
        SoftReference<ByteBuffer> dataRef = this.dataRef;
        if (dataRef != null && (ret = dataRef.get()) != null) {
            return ret;
        }
        if (this.paths != null) {
            if (this.paths.size() != 1) {
                throw new UnsupportedOperationException("multiple paths for " + this);
            }
            ret = ByteBuffer.wrap(Files.readAllBytes(this.paths.get(0)));
        } else {
            ModCandidateImpl parent = this.getBestSourcingParent();
            if (parent.paths != null) {
                if (parent.paths.size() != 1) {
                    throw new UnsupportedOperationException("multiple parent paths for " + this);
                }
                try (ZipFile zf = new ZipFile(parent.paths.get(0).toFile());){
                    ZipEntry entry = zf.getEntry(this.localPath);
                    if (entry == null) {
                        throw new IOException(String.format("can't find nested mod %s in its parent mod %s", this, parent));
                    }
                    ret = ModDiscoverer.readMod(zf.getInputStream(entry));
                }
            }
            ByteBuffer data = parent.getData();
            ret = null;
            try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(data.array(), data.arrayOffset() + data.position(), data.arrayOffset() + data.limit()));){
                ZipEntry entry = null;
                while ((entry = zis.getNextEntry()) != null) {
                    if (!entry.getName().equals(this.localPath)) continue;
                    ret = ModDiscoverer.readMod(zis);
                    break;
                }
            }
            if (ret == null) {
                throw new IOException(String.format("can't find nested mod %s in its parent mods %s", this, parent));
            }
        }
        this.dataRef = new SoftReference<ByteBuffer>(ret);
        return ret;
    }

    private ModCandidateImpl getBestSourcingParent() {
        if (this.parentMods.isEmpty()) {
            return null;
        }
        ModCandidateImpl ret = null;
        for (ModCandidateImpl parent : this.parentMods) {
            if (parent.minNestLevel >= this.minNestLevel) continue;
            if (parent.paths != null && parent.paths.size() == 1 || parent.dataRef != null && parent.dataRef.get() != null) {
                return parent;
            }
            if (ret != null) continue;
            ret = parent;
        }
        if (ret == null) {
            throw new IllegalStateException("invalid nesting?");
        }
        return ret;
    }

    public String toString() {
        return String.format("%s %s", this.getId(), this.getVersion());
    }
}

