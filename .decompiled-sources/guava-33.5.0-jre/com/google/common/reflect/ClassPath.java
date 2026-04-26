/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.reflect;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.StandardSystemProperty;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;
import com.google.common.reflect.Reflection;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jspecify.annotations.Nullable;

public final class ClassPath {
    private static final Logger logger = Logger.getLogger(ClassPath.class.getName());
    private static final Splitter CLASS_PATH_ATTRIBUTE_SEPARATOR = Splitter.on(" ").omitEmptyStrings();
    private static final String CLASS_FILE_NAME_EXTENSION = ".class";
    private final ImmutableSet<ResourceInfo> resources;

    private ClassPath(ImmutableSet<ResourceInfo> resources) {
        this.resources = resources;
    }

    public static ClassPath from(ClassLoader classloader) throws IOException {
        ImmutableSet<LocationInfo> locations = ClassPath.locationsFrom(classloader);
        HashSet<File> scanned = new HashSet<File>();
        for (LocationInfo location : locations) {
            scanned.add(location.file());
        }
        ImmutableSet.Builder builder = ImmutableSet.builder();
        for (LocationInfo location : locations) {
            builder.addAll(location.scanResources(scanned));
        }
        return new ClassPath((ImmutableSet<ResourceInfo>)builder.build());
    }

    public ImmutableSet<ResourceInfo> getResources() {
        return this.resources;
    }

    public ImmutableSet<ClassInfo> getAllClasses() {
        return FluentIterable.from(this.resources).filter(ClassInfo.class).toSet();
    }

    public ImmutableSet<ClassInfo> getTopLevelClasses() {
        return FluentIterable.from(this.resources).filter(ClassInfo.class).filter(ClassInfo::isTopLevel).toSet();
    }

    public ImmutableSet<ClassInfo> getTopLevelClasses(String packageName) {
        Preconditions.checkNotNull(packageName);
        ImmutableSet.Builder builder = ImmutableSet.builder();
        for (ClassInfo classInfo : this.getTopLevelClasses()) {
            if (!classInfo.getPackageName().equals(packageName)) continue;
            builder.add(classInfo);
        }
        return builder.build();
    }

    public ImmutableSet<ClassInfo> getTopLevelClassesRecursive(String packageName) {
        Preconditions.checkNotNull(packageName);
        String packagePrefix = packageName + '.';
        ImmutableSet.Builder builder = ImmutableSet.builder();
        for (ClassInfo classInfo : this.getTopLevelClasses()) {
            if (!classInfo.getName().startsWith(packagePrefix)) continue;
            builder.add(classInfo);
        }
        return builder.build();
    }

    static ImmutableSet<LocationInfo> locationsFrom(ClassLoader classloader) {
        ImmutableSet.Builder builder = ImmutableSet.builder();
        for (Map.Entry entry : ClassPath.getClassPathEntries(classloader).entrySet()) {
            builder.add(new LocationInfo((File)entry.getKey(), (ClassLoader)entry.getValue()));
        }
        return builder.build();
    }

    @VisibleForTesting
    static ImmutableSet<File> getClassPathFromManifest(File jarFile, @Nullable Manifest manifest) {
        if (manifest == null) {
            return ImmutableSet.of();
        }
        ImmutableSet.Builder builder = ImmutableSet.builder();
        String classpathAttribute = manifest.getMainAttributes().getValue(Attributes.Name.CLASS_PATH.toString());
        if (classpathAttribute != null) {
            for (String path : CLASS_PATH_ATTRIBUTE_SEPARATOR.split(classpathAttribute)) {
                URL url;
                try {
                    url = ClassPath.getClassPathEntry(jarFile, path);
                }
                catch (MalformedURLException e) {
                    logger.warning("Invalid Class-Path entry: " + path);
                    continue;
                }
                if (!url.getProtocol().equals("file")) continue;
                builder.add(ClassPath.toFile(url));
            }
        }
        return builder.build();
    }

    @VisibleForTesting
    static ImmutableMap<File, ClassLoader> getClassPathEntries(ClassLoader classloader) {
        LinkedHashMap<File, ClassLoader> entries = new LinkedHashMap<File, ClassLoader>();
        ClassLoader parent = classloader.getParent();
        if (parent != null) {
            entries.putAll(ClassPath.getClassPathEntries(parent));
        }
        for (URL url : ClassPath.getClassLoaderUrls(classloader)) {
            File file;
            if (!url.getProtocol().equals("file") || entries.containsKey(file = ClassPath.toFile(url))) continue;
            entries.put(file, classloader);
        }
        return ImmutableMap.copyOf(entries);
    }

    private static ImmutableList<URL> getClassLoaderUrls(ClassLoader classloader) {
        if (classloader instanceof URLClassLoader) {
            return ImmutableList.copyOf(((URLClassLoader)classloader).getURLs());
        }
        if (classloader.equals(ClassLoader.getSystemClassLoader())) {
            return ClassPath.parseJavaClassPath();
        }
        return ImmutableList.of();
    }

    @VisibleForTesting
    static ImmutableList<URL> parseJavaClassPath() {
        ImmutableList.Builder urls = ImmutableList.builder();
        for (String entry : Splitter.on(StandardSystemProperty.PATH_SEPARATOR.value()).split(StandardSystemProperty.JAVA_CLASS_PATH.value())) {
            try {
                try {
                    urls.add(new File(entry).toURI().toURL());
                }
                catch (SecurityException e) {
                    urls.add(new URL("file", null, new File(entry).getAbsolutePath()));
                }
            }
            catch (MalformedURLException e) {
                logger.log(Level.WARNING, "malformed classpath entry: " + entry, e);
            }
        }
        return urls.build();
    }

    @VisibleForTesting
    static URL getClassPathEntry(File jarFile, String path) throws MalformedURLException {
        return new URL(jarFile.toURI().toURL(), path);
    }

    @VisibleForTesting
    static String getClassName(String filename) {
        int classNameEnd = filename.length() - CLASS_FILE_NAME_EXTENSION.length();
        return filename.substring(0, classNameEnd).replace('/', '.');
    }

    @VisibleForTesting
    static File toFile(URL url) {
        Preconditions.checkArgument(url.getProtocol().equals("file"));
        try {
            return new File(url.toURI());
        }
        catch (URISyntaxException e) {
            return new File(url.getPath());
        }
    }

    static final class LocationInfo {
        final File home;
        private final ClassLoader classloader;

        LocationInfo(File home, ClassLoader classloader) {
            this.home = Preconditions.checkNotNull(home);
            this.classloader = Preconditions.checkNotNull(classloader);
        }

        public final File file() {
            return this.home;
        }

        public ImmutableSet<ResourceInfo> scanResources() throws IOException {
            return this.scanResources(new HashSet<File>());
        }

        public ImmutableSet<ResourceInfo> scanResources(Set<File> scannedFiles) throws IOException {
            ImmutableSet.Builder<ResourceInfo> builder = ImmutableSet.builder();
            scannedFiles.add(this.home);
            this.scan(this.home, scannedFiles, builder);
            return builder.build();
        }

        private void scan(File file, Set<File> scannedUris, ImmutableSet.Builder<ResourceInfo> builder) throws IOException {
            try {
                if (!file.exists()) {
                    return;
                }
            }
            catch (SecurityException e) {
                logger.warning("Cannot access " + file + ": " + e);
                return;
            }
            if (file.isDirectory()) {
                this.scanDirectory(file, builder);
            } else {
                this.scanJar(file, scannedUris, builder);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private void scanJar(File file, Set<File> scannedUris, ImmutableSet.Builder<ResourceInfo> builder) throws IOException {
            JarFile jarFile;
            try {
                jarFile = new JarFile(file);
            }
            catch (IOException e) {
                return;
            }
            try {
                for (File path : ClassPath.getClassPathFromManifest(file, jarFile.getManifest())) {
                    if (!scannedUris.add(path.getCanonicalFile())) continue;
                    this.scan(path, scannedUris, builder);
                }
                this.scanJarFile(jarFile, builder);
            }
            finally {
                try {
                    jarFile.close();
                }
                catch (IOException iOException) {}
            }
        }

        private void scanJarFile(JarFile file, ImmutableSet.Builder<ResourceInfo> builder) {
            Enumeration<JarEntry> entries = file.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory() || entry.getName().equals("META-INF/MANIFEST.MF")) continue;
                builder.add((Object)ResourceInfo.of(new File(file.getName()), entry.getName(), this.classloader));
            }
        }

        private void scanDirectory(File directory, ImmutableSet.Builder<ResourceInfo> builder) throws IOException {
            HashSet<File> currentPath = new HashSet<File>();
            currentPath.add(directory.getCanonicalFile());
            this.scanDirectory(directory, "", currentPath, builder);
        }

        private void scanDirectory(File directory, String packagePrefix, Set<File> currentPath, ImmutableSet.Builder<ResourceInfo> builder) throws IOException {
            File[] files = directory.listFiles();
            if (files == null) {
                logger.warning("Cannot read directory " + directory);
                return;
            }
            for (File f : files) {
                String name = f.getName();
                if (f.isDirectory()) {
                    File deref = f.getCanonicalFile();
                    if (!currentPath.add(deref)) continue;
                    this.scanDirectory(deref, packagePrefix + name + "/", currentPath, builder);
                    currentPath.remove(deref);
                    continue;
                }
                String resourceName = packagePrefix + name;
                if (resourceName.equals("META-INF/MANIFEST.MF")) continue;
                builder.add((Object)ResourceInfo.of(f, resourceName, this.classloader));
            }
        }

        public boolean equals(@Nullable Object obj) {
            if (obj instanceof LocationInfo) {
                LocationInfo that = (LocationInfo)obj;
                return this.home.equals(that.home) && this.classloader.equals(that.classloader);
            }
            return false;
        }

        public int hashCode() {
            return this.home.hashCode();
        }

        public String toString() {
            return this.home.toString();
        }
    }

    public static final class ClassInfo
    extends ResourceInfo {
        private final String className;

        ClassInfo(File file, String resourceName, ClassLoader loader) {
            super(file, resourceName, loader);
            this.className = ClassPath.getClassName(resourceName);
        }

        public String getPackageName() {
            return Reflection.getPackageName(this.className);
        }

        public String getSimpleName() {
            int lastDollarSign = this.className.lastIndexOf(36);
            if (lastDollarSign != -1) {
                String innerClassName = this.className.substring(lastDollarSign + 1);
                return CharMatcher.inRange('0', '9').trimLeadingFrom(innerClassName);
            }
            String packageName = this.getPackageName();
            if (packageName.isEmpty()) {
                return this.className;
            }
            return this.className.substring(packageName.length() + 1);
        }

        public String getName() {
            return this.className;
        }

        public boolean isTopLevel() {
            return this.className.indexOf(36) == -1;
        }

        public Class<?> load() {
            try {
                return this.loader.loadClass(this.className);
            }
            catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public String toString() {
            return this.className;
        }
    }

    public static class ResourceInfo {
        private final File file;
        private final String resourceName;
        final ClassLoader loader;

        static ResourceInfo of(File file, String resourceName, ClassLoader loader) {
            if (resourceName.endsWith(ClassPath.CLASS_FILE_NAME_EXTENSION)) {
                return new ClassInfo(file, resourceName, loader);
            }
            return new ResourceInfo(file, resourceName, loader);
        }

        ResourceInfo(File file, String resourceName, ClassLoader loader) {
            this.file = Preconditions.checkNotNull(file);
            this.resourceName = Preconditions.checkNotNull(resourceName);
            this.loader = Preconditions.checkNotNull(loader);
        }

        public final URL url() {
            URL url = this.loader.getResource(this.resourceName);
            if (url == null) {
                throw new NoSuchElementException(this.resourceName);
            }
            return url;
        }

        public final ByteSource asByteSource() {
            return Resources.asByteSource(this.url());
        }

        public final CharSource asCharSource(Charset charset) {
            return Resources.asCharSource(this.url(), charset);
        }

        public final String getResourceName() {
            return this.resourceName;
        }

        final File getFile() {
            return this.file;
        }

        public int hashCode() {
            return this.resourceName.hashCode();
        }

        public boolean equals(@Nullable Object obj) {
            if (obj instanceof ResourceInfo) {
                ResourceInfo that = (ResourceInfo)obj;
                return this.resourceName.equals(that.resourceName) && this.loader == that.loader;
            }
            return false;
        }

        public String toString() {
            return this.resourceName;
        }
    }
}

