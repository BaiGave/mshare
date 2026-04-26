/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public final class LoaderUtil {
    private static final ConcurrentMap<Path, Path> pathNormalizationCache = new ConcurrentHashMap<Path, Path>();
    private static final String FABRIC_LOADER_CLASS = "net/fabricmc/loader/api/FabricLoader.class";
    private static final String ASM_CLASS = "org/objectweb/asm/ClassReader.class";

    public static String getClassFileName(String className) {
        return className.replace('.', '/').concat(".class");
    }

    public static Path normalizePath(Path path) {
        if (Files.exists(path, new LinkOption[0])) {
            return LoaderUtil.normalizeExistingPath(path);
        }
        return path.toAbsolutePath().normalize();
    }

    public static Path normalizeExistingPath(Path path) {
        return pathNormalizationCache.computeIfAbsent(path, LoaderUtil::normalizeExistingPath0);
    }

    private static Path normalizeExistingPath0(Path path) {
        try {
            return path.toRealPath(new LinkOption[0]);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void verifyNotInTargetCl(Class<?> cls) {
        if (cls.getClassLoader().getClass().getName().equals("net.fabricmc.loader.impl.launch.knot.KnotClassLoader")) {
            throw new IllegalStateException("trying to load " + cls.getName() + " from target class loader");
        }
    }

    public static void verifyClasspath() {
        try {
            ArrayList<URL> resources = Collections.list(LoaderUtil.class.getClassLoader().getResources(FABRIC_LOADER_CLASS));
            if (resources.size() > 1) {
                throw new IllegalStateException("duplicate fabric loader classes found on classpath: " + resources.stream().map(URL::toString).collect(Collectors.joining(", ")));
            }
            if (resources.size() < 1) {
                throw new AssertionError((Object)"net/fabricmc/loader/api/FabricLoader.class not detected on the classpath?! (perhaps it was renamed?)");
            }
            resources = Collections.list(LoaderUtil.class.getClassLoader().getResources(ASM_CLASS));
            if (resources.size() > 1) {
                throw new IllegalStateException("duplicate ASM classes found on classpath: " + resources.stream().map(URL::toString).collect(Collectors.joining(", ")));
            }
            if (resources.size() < 1) {
                throw new IllegalStateException("ASM not detected on the classpath (or perhaps org/objectweb/asm/ClassReader.class was renamed?)");
            }
        }
        catch (IOException e) {
            throw new UncheckedIOException("Failed to get resources", e);
        }
    }

    public static boolean hasMacOs() {
        return System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("mac");
    }

    public static boolean hasAwtSupport() {
        if (LoaderUtil.hasMacOs()) {
            for (String key : System.getenv().keySet()) {
                if (!key.startsWith("JAVA_STARTED_ON_FIRST_THREAD_")) continue;
                return false;
            }
        }
        return true;
    }
}

