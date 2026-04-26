/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.game;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipError;
import java.util.zip.ZipFile;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.game.GameProvider;
import net.fabricmc.loader.impl.game.GameProviderHelper;
import net.fabricmc.loader.impl.game.LoaderLibrary;
import net.fabricmc.loader.impl.util.LoaderUtil;
import net.fabricmc.loader.impl.util.ManifestUtil;
import net.fabricmc.loader.impl.util.SystemProperties;
import net.fabricmc.loader.impl.util.UrlUtil;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;

public final class LibClassifier<L extends Enum<L>> {
    private static final boolean DEBUG = SystemProperties.isSet("fabric.debug.logLibClassification");
    private final List<L> libs;
    private final Map<L, Path> origins;
    private final Map<L, String> localPaths;
    private final Set<Path> systemLibraries = new HashSet<Path>();
    private final List<Path> unmatchedOrigins = new ArrayList<Path>();

    public LibClassifier(Class<L> cls, EnvType env, GameProvider gameProvider) throws IOException {
        List<Path> gameLibs;
        Enum[] libs = (Enum[])cls.getEnumConstants();
        this.libs = new ArrayList<L>(libs.length);
        this.origins = new EnumMap<L, Path>(cls);
        this.localPaths = new EnumMap<L, String>(cls);
        for (Enum enum_ : libs) {
            if (!((LibraryType)((Object)enum_)).isApplicable(env)) continue;
            this.libs.add(enum_);
        }
        StringBuilder sb = DEBUG ? new StringBuilder() : null;
        List<Path> systemLibs = GameProviderHelper.getLibraries("fabric.systemLibraries");
        if (systemLibs != null) {
            for (Path path : systemLibs) {
                assert (path.equals(LoaderUtil.normalizeExistingPath(path)));
                if (!this.systemLibraries.add(path) || !DEBUG) continue;
                sb.append(String.format("\ud83c\uddf8 %s%n", path));
            }
        }
        boolean junitRun = SystemProperties.isSet("fabric.unitTest");
        for (LoaderLibrary lib : LoaderLibrary.values()) {
            if (!lib.isApplicable(env, junitRun)) continue;
            if (lib.path != null) {
                Path path = LoaderUtil.normalizeExistingPath(lib.path);
                this.systemLibraries.add(path);
                if (!DEBUG) continue;
                sb.append(String.format("\u2705 %s %s%n", lib.name(), path));
                continue;
            }
            if (!DEBUG) continue;
            sb.append(String.format("\u274e %s%n", lib.name()));
        }
        Path path = UrlUtil.getCodeSource(gameProvider.getClass());
        if (path != null) {
            Path path2 = LoaderUtil.normalizeExistingPath(path);
            if (this.systemLibraries.add(path2) && DEBUG) {
                sb.append(String.format("\u2705 gameprovider %s%n", path2));
            }
        } else if (DEBUG) {
            sb.append("\u274e gameprovider");
        }
        if (DEBUG) {
            Log.info(LogCategory.LIB_CLASSIFICATION, "Loader/system libraries:%n%s", sb);
        }
        if ((gameLibs = GameProviderHelper.getLibraries("fabric.gameLibraries")) != null) {
            this.process(gameLibs, new Enum[0]);
        }
        this.processManifestClassPath(LoaderLibrary.SERVER_LAUNCH, env, junitRun);
    }

    private void processManifestClassPath(LoaderLibrary lib, EnvType env, boolean junitRun) throws IOException {
        Manifest manifest;
        if (lib.path == null || !lib.isApplicable(env, junitRun) || !Files.isRegularFile(lib.path, new LinkOption[0])) {
            return;
        }
        try (ZipFile zf = new ZipFile(lib.path.toFile());){
            ZipEntry entry = zf.getEntry("META-INF/MANIFEST.MF");
            if (entry == null) {
                return;
            }
            manifest = new Manifest(zf.getInputStream(entry));
        }
        List<URL> cp = ManifestUtil.getClassPath(manifest, lib.path);
        if (cp == null) {
            return;
        }
        for (URL url : cp) {
            this.process(url);
        }
    }

    public void process(URL url) throws IOException {
        this.process(UrlUtil.asPath(url), new Enum[0]);
    }

    @SafeVarargs
    public final void process(Iterable<Path> paths, L ... excludedLibs) throws IOException {
        Set excluded = LibClassifier.makeSet(excludedLibs);
        for (Path path : paths) {
            this.process(path, excluded);
        }
    }

    @SafeVarargs
    public final void process(Path path, L ... excludedLibs) throws IOException {
        this.process(path, LibClassifier.makeSet(excludedLibs));
    }

    private static <L extends Enum<L>> Set<L> makeSet(L[] libs) {
        if (libs.length == 0) {
            return Collections.emptySet();
        }
        EnumSet<L> ret = EnumSet.of(libs[0]);
        for (int i = 1; i < libs.length; ++i) {
            ret.add(libs[i]);
        }
        return ret;
    }

    private void process(Path path, Set<L> excludedLibs) throws IOException {
        if (this.systemLibraries.contains(path = LoaderUtil.normalizeExistingPath(path))) {
            return;
        }
        boolean matched = false;
        if (Files.isDirectory(path, new LinkOption[0])) {
            block7: for (Enum lib : this.libs) {
                if (excludedLibs.contains(lib) || this.origins.containsKey(lib)) continue;
                for (String p : ((LibraryType)((Object)lib)).getPaths()) {
                    if (!Files.exists(path.resolve(p), new LinkOption[0])) continue;
                    matched = true;
                    this.addLibrary(lib, path, p);
                    continue block7;
                }
            }
        } else {
            try (ZipFile zf = new ZipFile(path.toFile());){
                block9: for (Enum lib : this.libs) {
                    if (excludedLibs.contains(lib) || this.origins.containsKey(lib)) continue;
                    for (String p : ((LibraryType)((Object)lib)).getPaths()) {
                        if (zf.getEntry(p) == null) continue;
                        matched = true;
                        this.addLibrary(lib, path, p);
                        continue block9;
                    }
                }
            }
            catch (IOException | ZipError e) {
                throw new IOException("error reading " + path, e);
            }
        }
        if (!matched) {
            this.unmatchedOrigins.add(path);
            if (DEBUG) {
                Log.info(LogCategory.LIB_CLASSIFICATION, "unmatched %s", path);
            }
        }
    }

    private void addLibrary(L lib, Path originPath, String localPath) {
        Path prev = this.origins.put(lib, originPath);
        if (prev != null) {
            throw new IllegalStateException("lib " + lib + " was already added");
        }
        this.localPaths.put(lib, localPath);
        if (DEBUG) {
            Log.info(LogCategory.LIB_CLASSIFICATION, "%s %s (%s)", ((Enum)lib).name(), originPath, localPath);
        }
    }

    @SafeVarargs
    public final boolean is(Path path, L ... libs) {
        for (L lib : libs) {
            if (!path.equals(this.origins.get(lib))) continue;
            return true;
        }
        return false;
    }

    public boolean has(L lib) {
        return this.origins.containsKey(lib);
    }

    public Path getOrigin(L lib) {
        return this.origins.get(lib);
    }

    public String getLocalPath(L lib) {
        return this.localPaths.get(lib);
    }

    public String getClassName(L lib) {
        String localPath = this.localPaths.get(lib);
        if (localPath == null || !localPath.endsWith(".class")) {
            return null;
        }
        return localPath.substring(0, localPath.length() - 6).replace('/', '.');
    }

    public List<Path> getUnmatchedOrigins() {
        return this.unmatchedOrigins;
    }

    public Collection<Path> getSystemLibraries() {
        return this.systemLibraries;
    }

    public boolean remove(Path path) {
        if (this.unmatchedOrigins.remove(path)) {
            return true;
        }
        boolean ret = false;
        Iterator<Map.Entry<L, Path>> it = this.origins.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<L, Path> entry = it.next();
            if (!entry.getValue().equals(path)) continue;
            this.localPaths.remove(entry.getKey());
            it.remove();
            ret = true;
        }
        return ret;
    }

    public static interface LibraryType {
        public boolean isApplicable(EnvType var1);

        public String[] getPaths();
    }
}

