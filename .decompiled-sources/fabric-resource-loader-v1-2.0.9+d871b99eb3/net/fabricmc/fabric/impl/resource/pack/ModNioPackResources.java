/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.resource.pack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.resource.v1.pack.ModPackResources;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.fabric.impl.resource.pack.ModPackResourcesUtil;
import net.fabricmc.fabric.impl.resource.pack.ModResourcePackCreator;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.util.FileUtil;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModNioPackResources
implements PackResources,
ModPackResources {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModNioPackResources.class);
    private static final Pattern RESOURCE_PACK_PATH = Pattern.compile("[a-z0-9-_.]+");
    private static final FileSystem DEFAULT_FS = FileSystems.getDefault();
    private final String id;
    private final ModContainer mod;
    private final List<Path> basePaths;
    private final PackType type;
    private final PackActivationType activationType;
    private final Map<PackType, Set<String>> namespaces;
    private final PackLocationInfo metadata;
    private final boolean modBundled;
    private static final String resPrefix = PackType.CLIENT_RESOURCES.getDirectory() + "/";
    private static final String dataPrefix = PackType.SERVER_DATA.getDirectory() + "/";

    public static @Nullable ModNioPackResources create(String id, ModContainer mod, String subPath, PackType type, PackActivationType activationType, boolean modBundled) {
        List<Path> paths;
        List<Path> rootPaths = mod.getRootPaths();
        if (subPath == null) {
            paths = rootPaths;
        } else {
            paths = new ArrayList<Path>(rootPaths.size());
            for (Path path : rootPaths) {
                Path childPath = (path = path.toAbsolutePath().normalize()).resolve(subPath.replace("/", path.getFileSystem().getSeparator())).normalize();
                if (!childPath.startsWith(path) || !ModNioPackResources.exists(childPath)) continue;
                paths.add(childPath);
            }
        }
        if (paths.isEmpty()) {
            return null;
        }
        String packId = subPath != null && modBundled ? id + "_" + subPath : id;
        MutableComponent displayName = subPath == null ? Component.translatable("pack.name.fabricMod", mod.getMetadata().getName()) : Component.translatable("pack.name.fabricMod.subPack", mod.getMetadata().getName(), Component.translatable("resourcePack." + subPath + ".name"));
        PackLocationInfo metadata = new PackLocationInfo(packId, displayName, ModResourcePackCreator.RESOURCE_PACK_SOURCE, Optional.of(new KnownPack("vanilla", packId, mod.getMetadata().getVersion().getFriendlyString())));
        ModNioPackResources ret = new ModNioPackResources(packId, mod, paths, type, activationType, modBundled, metadata);
        return ret.getNamespaces(type).isEmpty() ? null : ret;
    }

    private ModNioPackResources(String id, ModContainer mod, List<Path> paths, PackType type, PackActivationType activationType, boolean modBundled, PackLocationInfo metadata) {
        this.id = id;
        this.mod = mod;
        this.basePaths = paths;
        this.type = type;
        this.activationType = activationType;
        this.modBundled = modBundled;
        this.namespaces = ModNioPackResources.readNamespaces(paths, mod.getMetadata().getId());
        this.metadata = metadata;
    }

    @Override
    public ModNioPackResources createOverlay(String overlay) {
        return new ModNioPackResources(this.id, this.mod, this.basePaths.stream().map(path -> path.resolve(overlay)).toList(), this.type, this.activationType, this.modBundled, this.metadata);
    }

    public static Map<PackType, Set<String>> readNamespaces(List<Path> paths, String modId) {
        EnumMap<PackType, Set<String>> ret = new EnumMap<PackType, Set<String>>(PackType.class);
        for (PackType type : PackType.values()) {
            Set namespaces = null;
            for (Path path : paths) {
                Path dir = path.resolve(type.getDirectory());
                if (!Files.isDirectory(dir, new LinkOption[0])) continue;
                String separator = path.getFileSystem().getSeparator();
                try {
                    DirectoryStream<Path> ds = Files.newDirectoryStream(dir);
                    try {
                        for (Path p : ds) {
                            if (!Files.isDirectory(p, new LinkOption[0])) continue;
                            String s = p.getFileName().toString();
                            if (!RESOURCE_PACK_PATH.matcher(s = s.replace(separator, "")).matches()) {
                                LOGGER.warn("Fabric NioResourcePack: ignored invalid namespace: {} in mod ID {}", (Object)s, (Object)modId);
                                continue;
                            }
                            if (namespaces == null) {
                                namespaces = new HashSet();
                            }
                            namespaces.add(s);
                        }
                    }
                    finally {
                        if (ds == null) continue;
                        ds.close();
                    }
                }
                catch (IOException e) {
                    LOGGER.warn("getNamespaces in mod " + modId + " failed!", e);
                }
            }
            ret.put(type, namespaces != null ? namespaces : Collections.emptySet());
        }
        return ret;
    }

    private Path getPath(String filename) {
        if (this.hasAbsentNs(filename)) {
            return null;
        }
        for (Path basePath : this.basePaths) {
            Path childPath = basePath.resolve(filename.replace("/", basePath.getFileSystem().getSeparator())).toAbsolutePath().normalize();
            if (!childPath.startsWith(basePath) || !ModNioPackResources.exists(childPath)) continue;
            return childPath;
        }
        return null;
    }

    private boolean hasAbsentNs(String filename) {
        int prefixLen;
        if (filename.startsWith(resPrefix)) {
            prefixLen = resPrefix.length();
            PackType type = PackType.CLIENT_RESOURCES;
        } else if (filename.startsWith(dataPrefix)) {
            prefixLen = dataPrefix.length();
            PackType type = PackType.SERVER_DATA;
        } else {
            return false;
        }
        int nsEnd = filename.indexOf(47, prefixLen);
        if (nsEnd < 0) {
            return false;
        }
        return !this.namespaces.get((Object)this.type).contains(filename.substring(prefixLen, nsEnd));
    }

    private IoSupplier<InputStream> openFile(String filename) {
        Path path = this.getPath(filename);
        if (path != null && Files.isRegularFile(path, new LinkOption[0])) {
            return () -> Files.newInputStream(path, new OpenOption[0]);
        }
        if (ModPackResourcesUtil.containsDefault(filename, this.modBundled)) {
            return () -> ModPackResourcesUtil.openDefault(this.mod, this.type, filename);
        }
        return null;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String ... pathSegments) {
        FileUtil.validatePath(pathSegments);
        return this.openFile(String.join((CharSequence)"/", pathSegments));
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType type, Identifier id) {
        Path path = this.getPath(ModNioPackResources.getFilename(type, id));
        return path == null ? null : IoSupplier.create(path);
    }

    @Override
    public void listResources(PackType type, final String namespace, String path, final PackResources.ResourceOutput visitor) {
        if (!this.namespaces.getOrDefault((Object)type, Collections.emptySet()).contains(namespace)) {
            return;
        }
        for (Path basePath : this.basePaths) {
            final String separator = basePath.getFileSystem().getSeparator();
            final Path nsPath = basePath.resolve(type.getDirectory()).resolve(namespace);
            Path searchPath = nsPath.resolve(path.replace("/", separator)).normalize();
            if (!ModNioPackResources.exists(searchPath)) continue;
            try {
                Files.walkFileTree(searchPath, (FileVisitor<? super Path>)new SimpleFileVisitor<Path>(this){
                    final /* synthetic */ ModNioPackResources this$0;
                    {
                        ModNioPackResources modNioPackResources = this$0;
                        Objects.requireNonNull(modNioPackResources);
                        this.this$0 = modNioPackResources;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        String filename = nsPath.relativize(file).toString().replace(separator, "/");
                        Identifier identifier = Identifier.tryBuild(namespace, filename);
                        if (identifier == null) {
                            LOGGER.error("Invalid path in mod resource-pack {}: {}:{}, ignoring", this.this$0.id, namespace, filename);
                        } else {
                            visitor.accept(identifier, IoSupplier.create(file));
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
            catch (IOException e) {
                LOGGER.warn("findResources at " + path + " in namespace " + namespace + ", mod " + this.mod.getMetadata().getId() + " failed!", e);
            }
        }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return this.namespaces.getOrDefault((Object)type, Set.of());
    }

    @Override
    public <T> T getMetadataSection(MetadataSectionType<T> metaReader) throws IOException {
        try (InputStream is = Objects.requireNonNull(this.openFile("pack.mcmeta")).get();){
            ResourceMetadata resourceMetadata = ResourceMetadata.fromJsonStream(is);
            Optional<Object> section = resourceMetadata.getSection(metaReader);
            T t = section.orElse(null);
            return t;
        }
    }

    @Override
    public PackLocationInfo location() {
        return this.metadata;
    }

    @Override
    public void close() {
    }

    @Override
    public ModMetadata getFabricModMetadata() {
        return this.mod.getMetadata();
    }

    public PackActivationType getActivationType() {
        return this.activationType;
    }

    @Override
    public String packId() {
        return this.id;
    }

    private static boolean exists(Path path) {
        return path.getFileSystem() == DEFAULT_FS ? path.toFile().exists() : Files.exists(path, new LinkOption[0]);
    }

    private static String getFilename(PackType type, Identifier id) {
        return String.format(Locale.ROOT, "%s/%s/%s", type.getDirectory(), id.getNamespace(), id.getPath());
    }
}

