/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.launch.knot;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.security.CodeSource;
import java.security.cert.Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Manifest;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.game.GameProvider;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.fabricmc.loader.impl.launch.knot.KnotClassLoaderInterface;
import net.fabricmc.loader.impl.launch.knot.MixinServiceKnot;
import net.fabricmc.loader.impl.transformer.FabricTransformer;
import net.fabricmc.loader.impl.util.ExceptionUtil;
import net.fabricmc.loader.impl.util.FileSystemUtil;
import net.fabricmc.loader.impl.util.LoaderUtil;
import net.fabricmc.loader.impl.util.ManifestUtil;
import net.fabricmc.loader.impl.util.SystemProperties;
import net.fabricmc.loader.impl.util.UrlConversionException;
import net.fabricmc.loader.impl.util.UrlUtil;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;

final class KnotClassDelegate<T extends ClassLoader>
implements KnotClassLoaderInterface {
    private static final boolean LOG_CLASS_LOAD = SystemProperties.isSet("fabric.debug.logClassLoad");
    private static final boolean LOG_CLASS_LOAD_ERRORS = LOG_CLASS_LOAD || SystemProperties.isSet("fabric.debug.logClassLoadErrors");
    private static final boolean LOG_TRANSFORM_ERRORS = SystemProperties.isSet("fabric.debug.logTransformErrors");
    private static final boolean DISABLE_ISOLATION = SystemProperties.isSet("fabric.debug.disableClassPathIsolation");
    private static final ClassLoader PLATFORM_CLASS_LOADER = KnotClassDelegate.getPlatformClassLoader();
    private final Map<Path, Metadata> metadataCache = new ConcurrentHashMap<Path, Metadata>();
    private final T classLoader;
    private final ClassLoader parentClassLoader;
    private final GameProvider provider;
    private final boolean isDevelopment;
    private final EnvType envType;
    private IMixinTransformer mixinTransformer;
    private boolean transformInitialized = false;
    private volatile Set<Path> codeSources = Collections.emptySet();
    private volatile Set<Path> validParentCodeSources = null;
    private final Map<Path, String[]> allowedPrefixes = new ConcurrentHashMap<Path, String[]>();
    private final Set<String> parentSourcedClasses = Collections.newSetFromMap(new ConcurrentHashMap());
    private static final Collection<Path> JVM_NATIVE_DIRS = KnotClassDelegate.computeJvmNativeDirs();
    private static final Map<String, String> PROCESSED_NATIVES = new HashMap<String, String>();

    KnotClassDelegate(boolean isDevelopment, EnvType envType, T classLoader, ClassLoader parentClassLoader, GameProvider provider) {
        this.isDevelopment = isDevelopment;
        this.envType = envType;
        this.classLoader = classLoader;
        this.parentClassLoader = parentClassLoader;
        this.provider = provider;
    }

    @Override
    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    @Override
    public void initializeTransformers() {
        if (this.transformInitialized) {
            throw new IllegalStateException("Cannot initialize KnotClassDelegate twice!");
        }
        this.mixinTransformer = MixinServiceKnot.getTransformer();
        if (this.mixinTransformer == null) {
            try {
                Constructor<?> ctor = Class.forName("org.spongepowered.asm.mixin.transformer.MixinTransformer").getConstructor(new Class[0]);
                ctor.setAccessible(true);
                this.mixinTransformer = (IMixinTransformer)ctor.newInstance(new Object[0]);
            }
            catch (ReflectiveOperationException e) {
                Log.debug(LogCategory.KNOT, "Can't create Mixin transformer through reflection (only applicable for 0.8-0.8.2): %s", e);
                throw new IllegalStateException("mixin transformer unavailable?");
            }
        }
        this.transformInitialized = true;
    }

    private IMixinTransformer getMixinTransformer() {
        assert (this.mixinTransformer != null);
        return this.mixinTransformer;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void addCodeSource(Path path) {
        path = LoaderUtil.normalizeExistingPath(path);
        KnotClassDelegate knotClassDelegate = this;
        synchronized (knotClassDelegate) {
            Set<Path> codeSources = this.codeSources;
            if (codeSources.contains(path)) {
                return;
            }
            HashSet<Path> newCodeSources = new HashSet<Path>(codeSources.size() + 1, 1.0f);
            newCodeSources.addAll(codeSources);
            newCodeSources.add(path);
            this.codeSources = newCodeSources;
        }
        try {
            ((ClassLoaderAccess)this.classLoader).addUrlFwd(UrlUtil.asUrl(path));
        }
        catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        if (LOG_CLASS_LOAD_ERRORS) {
            Log.info(LogCategory.KNOT, "added code source %s", path);
        }
    }

    @Override
    public void setAllowedPrefixes(Path codeSource, String ... prefixes) {
        codeSource = LoaderUtil.normalizeExistingPath(codeSource);
        if (prefixes.length == 0) {
            this.allowedPrefixes.remove(codeSource);
        } else {
            this.allowedPrefixes.put(codeSource, prefixes);
        }
    }

    @Override
    public void setValidParentClassPath(Collection<Path> paths) {
        HashSet<Path> validPaths = new HashSet<Path>(paths.size(), 1.0f);
        for (Path path : paths) {
            validPaths.add(LoaderUtil.normalizeExistingPath(path));
        }
        this.validParentCodeSources = validPaths;
    }

    @Override
    public Manifest getManifest(Path codeSource) {
        return this.getMetadata((Path)LoaderUtil.normalizeExistingPath((Path)codeSource)).manifest;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean isClassLoaded(String name) {
        Object object = ((ClassLoaderAccess)this.classLoader).getClassLoadingLockFwd(name);
        synchronized (object) {
            return ((ClassLoaderAccess)this.classLoader).findLoadedClassFwd(name) != null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Class<?> loadIntoTarget(String name) throws ClassNotFoundException {
        Object object = ((ClassLoaderAccess)this.classLoader).getClassLoadingLockFwd(name);
        synchronized (object) {
            Class<?> c = ((ClassLoaderAccess)this.classLoader).findLoadedClassFwd(name);
            if (c == null) {
                c = this.tryLoadClass(name, true);
                if (c == null) {
                    throw new ClassNotFoundException("can't find class " + name);
                }
                if (LOG_CLASS_LOAD) {
                    Log.info(LogCategory.KNOT, "loaded class %s into target", name);
                }
            }
            ((ClassLoaderAccess)this.classLoader).resolveClassFwd(c);
            return c;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        var3_3 = ((ClassLoaderAccess)this.classLoader).getClassLoadingLockFwd(name);
        synchronized (var3_3) {
            c = ((ClassLoaderAccess)this.classLoader).findLoadedClassFwd(name);
            if (c == null) {
                if (name.startsWith("java.")) {
                    c = KnotClassDelegate.PLATFORM_CLASS_LOADER.loadClass(name);
                } else {
                    c = this.tryLoadClass(name, false);
                    if (c == null) {
                        fileName = LoaderUtil.getClassFileName(name);
                        url = this.parentClassLoader.getResource(fileName);
                        if (url == null) {
                            try {
                                c = KnotClassDelegate.PLATFORM_CLASS_LOADER.loadClass(name);
                                if (!KnotClassDelegate.LOG_CLASS_LOAD) ** GOTO lbl34
                                Log.info(LogCategory.KNOT, "loaded resources-less class %s from platform class loader");
                            }
                            catch (ClassNotFoundException e) {
                                if (KnotClassDelegate.LOG_CLASS_LOAD_ERRORS) {
                                    Log.warn(LogCategory.KNOT, "can't find class %s", new Object[]{name});
                                }
                                throw e;
                            }
                        } else {
                            if (!this.isValidParentUrl(url, fileName)) {
                                msg = String.format("can't load class %s at %s as it hasn't been exposed to the game (yet? The system property fabric.classPathGroups may not be set correctly in-dev)", new Object[]{name, KnotClassDelegate.getCodeSource(url, fileName)});
                                if (KnotClassDelegate.LOG_CLASS_LOAD_ERRORS) {
                                    Log.warn(LogCategory.KNOT, msg);
                                }
                                throw new ClassNotFoundException(msg);
                            }
                            if (KnotClassDelegate.LOG_CLASS_LOAD) {
                                Log.info(LogCategory.KNOT, "loading class %s using the parent class loader", new Object[]{name});
                            }
                            c = this.parentClassLoader.loadClass(name);
                        }
                    } else if (KnotClassDelegate.LOG_CLASS_LOAD) {
                        Log.info(LogCategory.KNOT, "loaded class %s", new Object[]{name});
                    }
                }
            }
lbl34:
            // 9 sources

            if (resolve) {
                ((ClassLoaderAccess)this.classLoader).resolveClassFwd(c);
            }
            return c;
        }
    }

    private boolean isValidParentUrl(URL url, String fileName) {
        if (url == null) {
            return false;
        }
        if (DISABLE_ISOLATION) {
            return true;
        }
        if (!KnotClassDelegate.hasRegularCodeSource(url)) {
            return true;
        }
        Path codeSource = KnotClassDelegate.getCodeSource(url, fileName);
        Set<Path> validParentCodeSources = this.validParentCodeSources;
        if (validParentCodeSources != null) {
            return validParentCodeSources.contains(codeSource) || PLATFORM_CLASS_LOADER.getResource(fileName) != null;
        }
        return !this.codeSources.contains(codeSource);
    }

    Class<?> tryLoadClass(String name, boolean allowFromParent) throws ClassNotFoundException {
        Metadata metadata;
        byte[] input;
        block14: {
            String pkgString;
            Path codeSource;
            String[] prefixes;
            String fileName;
            URL url;
            if (name.startsWith("java.")) {
                return null;
            }
            if (!this.allowedPrefixes.isEmpty() && !DISABLE_ISOLATION && (url = ((ClassLoader)this.classLoader).getResource(fileName = LoaderUtil.getClassFileName(name))) != null && KnotClassDelegate.hasRegularCodeSource(url) && (prefixes = this.allowedPrefixes.get(codeSource = KnotClassDelegate.getCodeSource(url, fileName))) != null) {
                assert (prefixes.length > 0);
                boolean found = false;
                for (String prefix : prefixes) {
                    if (!name.startsWith(prefix)) continue;
                    found = true;
                    break;
                }
                if (!found) {
                    String msg = "class " + name + " is currently restricted from being loaded";
                    if (LOG_CLASS_LOAD_ERRORS) {
                        Log.warn(LogCategory.KNOT, msg);
                    }
                    throw new ClassNotFoundException(msg);
                }
            }
            if (!allowFromParent && !this.parentSourcedClasses.isEmpty()) {
                int pos = name.length();
                while ((pos = name.lastIndexOf(36, pos - 1)) > 0) {
                    if (!this.parentSourcedClasses.contains(name.substring(0, pos))) continue;
                    allowFromParent = true;
                    break;
                }
            }
            if ((input = this.getPostMixinClassByteArray(name, allowFromParent)) == null) {
                return null;
            }
            Class<?> existingClass = ((ClassLoaderAccess)this.classLoader).findLoadedClassFwd(name);
            if (existingClass != null) {
                return existingClass;
            }
            if (allowFromParent) {
                this.parentSourcedClasses.add(name);
            }
            metadata = this.getMetadata(name);
            int pkgDelimiterPos = name.lastIndexOf(46);
            if (pkgDelimiterPos > 0 && ((ClassLoaderAccess)this.classLoader).getPackageFwd(pkgString = name.substring(0, pkgDelimiterPos)) == null) {
                try {
                    ((ClassLoaderAccess)this.classLoader).definePackageFwd(pkgString, null, null, null, null, null, null, null);
                }
                catch (IllegalArgumentException e) {
                    if (((ClassLoaderAccess)this.classLoader).getPackageFwd(pkgString) != null) break block14;
                    throw e;
                }
            }
        }
        return ((ClassLoaderAccess)this.classLoader).defineClassFwd(name, input, 0, input.length, metadata.codeSource);
    }

    private Metadata getMetadata(String name) {
        String fileName = LoaderUtil.getClassFileName(name);
        URL url = ((ClassLoader)this.classLoader).getResource(fileName);
        if (url == null || !KnotClassDelegate.hasRegularCodeSource(url)) {
            return Metadata.EMPTY;
        }
        return this.getMetadata(KnotClassDelegate.getCodeSource(url, fileName));
    }

    private Metadata getMetadata(Path codeSource) {
        return this.metadataCache.computeIfAbsent(codeSource, path -> {
            Certificate[] certificates;
            CodeSource cs;
            Manifest manifest;
            block13: {
                manifest = null;
                cs = null;
                certificates = null;
                try {
                    if (Files.isDirectory(path, new LinkOption[0])) {
                        manifest = ManifestUtil.readManifestFromBasePath(path);
                        break block13;
                    }
                    URLConnection connection = new URL("jar:" + path.toUri().toString() + "!/").openConnection();
                    if (connection instanceof JarURLConnection) {
                        manifest = ((JarURLConnection)connection).getManifest();
                        certificates = ((JarURLConnection)connection).getCertificates();
                    }
                    if (manifest != null) break block13;
                    try (FileSystemUtil.FileSystemDelegate jarFs = FileSystemUtil.getJarFileSystem(path, false);){
                        manifest = ManifestUtil.readManifestFromBasePath(jarFs.get().getRootDirectories().iterator().next());
                    }
                }
                catch (IOException | FileSystemNotFoundException e) {
                    if (!FabricLauncherBase.getLauncher().isDevelopment()) break block13;
                    Log.warn(LogCategory.KNOT, "Failed to load manifest", e);
                }
            }
            if (cs == null) {
                try {
                    cs = new CodeSource(UrlUtil.asUrl(path), certificates);
                }
                catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
            return new Metadata(manifest, cs);
        });
    }

    private byte[] getPostMixinClassByteArray(String name, boolean allowFromParent) {
        byte[] transformedClassArray = this.getPreMixinClassByteArray(name, allowFromParent);
        if (!this.transformInitialized || !KnotClassDelegate.canTransformClass(name)) {
            return transformedClassArray;
        }
        try {
            return this.getMixinTransformer().transformClassBytes(name, name, transformedClassArray);
        }
        catch (Throwable t) {
            String msg = String.format("Mixin transformation of %s failed", name);
            if (LOG_TRANSFORM_ERRORS) {
                Log.warn(LogCategory.KNOT, msg, t);
            }
            throw new RuntimeException(msg, t);
        }
    }

    @Override
    public byte[] getPreMixinClassBytes(String name) {
        return this.getPreMixinClassByteArray(name, true);
    }

    private byte[] getPreMixinClassByteArray(String name, boolean allowFromParent) {
        name = name.replace('/', '.');
        if (!this.transformInitialized || !KnotClassDelegate.canTransformClass(name)) {
            try {
                return this.getRawClassByteArray(name, allowFromParent);
            }
            catch (IOException e) {
                throw new RuntimeException("Failed to load class file for '" + name + "'!", e);
            }
        }
        byte[] input = this.provider.getEntrypointTransformer().transform(name);
        if (input == null) {
            try {
                input = this.getRawClassByteArray(name, allowFromParent);
            }
            catch (IOException e) {
                throw new RuntimeException("Failed to load class file for '" + name + "'!", e);
            }
        }
        if (input != null) {
            return FabricTransformer.transform(this.isDevelopment, this.envType, name, input);
        }
        return null;
    }

    private static boolean canTransformClass(String name) {
        return !(name = name.replace('/', '.')).startsWith("org.apache.logging.log4j");
    }

    @Override
    public byte[] getRawClassBytes(String name) throws IOException {
        return this.getRawClassByteArray(name, true);
    }

    private byte[] getRawClassByteArray(String name, boolean allowFromParent) throws IOException {
        URL url = ((ClassLoaderAccess)this.classLoader).findResourceFwd(name = LoaderUtil.getClassFileName(name));
        if (url == null) {
            if (!allowFromParent) {
                return null;
            }
            url = this.parentClassLoader.getResource(name);
            if (!this.isValidParentUrl(url, name)) {
                if (LOG_CLASS_LOAD) {
                    Log.info(LogCategory.KNOT, "refusing to load class %s at %s from parent class loader", name, url != null ? KnotClassDelegate.getCodeSource(url, name) : "null");
                }
                return null;
            }
        }
        try (InputStream inputStream = url.openStream();){
            int len;
            int a = inputStream.available();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(a < 32 ? 32768 : a);
            byte[] buffer = new byte[8192];
            while ((len = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }
            byte[] byArray = outputStream.toByteArray();
            return byArray;
        }
    }

    private static boolean hasRegularCodeSource(URL url) {
        return url.getProtocol().equals("file") || url.getProtocol().equals("jar");
    }

    private static Path getCodeSource(URL url, String fileName) {
        try {
            return LoaderUtil.normalizeExistingPath(UrlUtil.getCodeSource(url, fileName));
        }
        catch (UrlConversionException e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    private static ClassLoader getPlatformClassLoader() {
        try {
            return (ClassLoader)ClassLoader.class.getMethod("getPlatformClassLoader", new Class[0]).invoke(null, new Object[0]);
        }
        catch (NoSuchMethodException e) {
            return new ClassLoader(null){};
        }
        catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private static Collection<Path> computeJvmNativeDirs() {
        String[] libPathProperties;
        HashSet<Path> ret = new HashSet<Path>();
        for (String libPathProperty : libPathProperties = new String[]{"sun.boot.library.path", "java.library.path"}) {
            String value = System.getProperty(libPathProperty);
            if (value == null || value.isEmpty()) continue;
            for (String pathStr : value.split(File.pathSeparator)) {
                try {
                    Path path = Paths.get(pathStr, new String[0]);
                    if (!Files.exists(path, new LinkOption[0])) continue;
                    ret.add(path);
                }
                catch (InvalidPathException e) {
                    Log.warn(LogCategory.KNOT, "Ignoring invalid library path %s", pathStr);
                }
            }
        }
        return ret;
    }

    synchronized String findLibrary(String libname) {
        Path libFile;
        Path codeSource;
        String ret = PROCESSED_NATIVES.get(libname);
        if (ret != null) {
            return ret;
        }
        String fileName = System.mapLibraryName(libname);
        for (Path dir : JVM_NATIVE_DIRS) {
            Path file = dir.resolve(fileName);
            if (!Files.exists(file, new LinkOption[0])) continue;
            return null;
        }
        URL url = ((ClassLoader)this.classLoader).getResource(fileName);
        if (url == null) {
            return null;
        }
        try {
            codeSource = UrlUtil.getCodeSource(url, fileName);
        }
        catch (UrlConversionException e) {
            throw new RuntimeException(e);
        }
        if (Files.isDirectory(codeSource, new LinkOption[0])) {
            libFile = codeSource.resolve(fileName);
        } else {
            Path cacheDir = null;
            try {
                cacheDir = FabricLoaderImpl.INSTANCE.getGameDir().resolve(".fabric").resolve("natives");
                assert (cacheDir.isAbsolute());
                Files.createDirectories(cacheDir, new FileAttribute[0]);
            }
            catch (IllegalStateException e) {
                return null;
            }
            catch (IOException e) {
                Log.warn(LogCategory.KNOT, "Error creating natives cache directory %s", cacheDir, e);
                return null;
            }
            libFile = cacheDir.resolve(fileName);
            Log.debug(LogCategory.KNOT, "Extracting native %s from class path %s to %s", libname, url, libFile);
            try {
                KnotClassDelegate.copyZipEntryIfDistinct(codeSource, fileName, libFile);
            }
            catch (IOException e) {
                Log.warn(LogCategory.KNOT, "Error extracting native %s to %s", url, cacheDir, e);
                return null;
            }
        }
        ret = libFile.toString();
        PROCESSED_NATIVES.put(libname, ret);
        Log.debug(LogCategory.KNOT, "Supplying native %s from class path (%s)", libname, ret);
        return ret;
    }

    private static void copyZipEntryIfDistinct(Path zipFile, String fileName, Path output) throws IOException {
        try (ZipFile zf = new ZipFile(zipFile.toFile());){
            ZipEntry entry = zf.getEntry(fileName);
            if (entry == null) {
                throw new FileNotFoundException(String.format("zip file %s doesn't contain %s", zipFile, fileName));
            }
            if (Files.exists(output, new LinkOption[0])) {
                long expectedSize = entry.getSize();
                long expectedCrc = entry.getCrc();
                if (Files.size(output) == expectedSize) {
                    CRC32 crc = new CRC32();
                    byte[] buffer = new byte[16384];
                    try (InputStream is = Files.newInputStream(output, new OpenOption[0]);){
                        int len;
                        while ((len = is.read(buffer)) >= 0) {
                            crc.update(buffer, 0, len);
                        }
                    }
                    if (crc.getValue() == expectedCrc) {
                        return;
                    }
                }
            }
            Files.copy(zf.getInputStream(entry), output, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    static interface ClassLoaderAccess {
        public void addUrlFwd(URL var1);

        public URL findResourceFwd(String var1);

        public Package getPackageFwd(String var1);

        public Package definePackageFwd(String var1, String var2, String var3, String var4, String var5, String var6, String var7, URL var8) throws IllegalArgumentException;

        public Object getClassLoadingLockFwd(String var1);

        public Class<?> findLoadedClassFwd(String var1);

        public Class<?> defineClassFwd(String var1, byte[] var2, int var3, int var4, CodeSource var5);

        public void resolveClassFwd(Class<?> var1);
    }

    static final class Metadata {
        static final Metadata EMPTY = new Metadata(null, null);
        final Manifest manifest;
        final CodeSource codeSource;

        Metadata(Manifest manifest, CodeSource codeSource) {
            this.manifest = manifest;
            this.codeSource = codeSource;
        }
    }
}

