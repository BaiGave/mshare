/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import net.fabricmc.loader.impl.util.FileSystemUtil;
import net.fabricmc.loader.impl.util.UrlUtil;

public final class ManifestUtil {
    public static Manifest readManifest(Class<?> cls) throws IOException, URISyntaxException {
        CodeSource cs = cls.getProtectionDomain().getCodeSource();
        if (cs == null) {
            return null;
        }
        URL url = cs.getLocation();
        if (url == null) {
            return null;
        }
        return ManifestUtil.readManifest(url);
    }

    private static Manifest readManifest(URL codeSourceUrl) throws IOException, URISyntaxException {
        Path path = UrlUtil.asPath(codeSourceUrl);
        if (Files.isDirectory(path, new LinkOption[0])) {
            return ManifestUtil.readManifestFromBasePath(path);
        }
        URLConnection connection = new URL("jar:" + codeSourceUrl.toString() + "!/").openConnection();
        if (connection instanceof JarURLConnection) {
            return ((JarURLConnection)connection).getManifest();
        }
        try (FileSystemUtil.FileSystemDelegate jarFs = FileSystemUtil.getJarFileSystem(path, false);){
            Manifest manifest = ManifestUtil.readManifestFromBasePath(jarFs.get().getRootDirectories().iterator().next());
            return manifest;
        }
    }

    public static Manifest readManifest(Path codeSource) throws IOException {
        if (Files.isDirectory(codeSource, new LinkOption[0])) {
            return ManifestUtil.readManifestFromBasePath(codeSource);
        }
        try (FileSystemUtil.FileSystemDelegate jarFs = FileSystemUtil.getJarFileSystem(codeSource, false);){
            Manifest manifest = ManifestUtil.readManifestFromBasePath(jarFs.get().getRootDirectories().iterator().next());
            return manifest;
        }
    }

    public static Manifest readManifestFromBasePath(Path basePath) throws IOException {
        Path path = basePath.resolve("META-INF").resolve("MANIFEST.MF");
        if (!Files.exists(path, new LinkOption[0])) {
            return null;
        }
        try (InputStream stream = Files.newInputStream(path, new OpenOption[0]);){
            Manifest manifest = new Manifest(stream);
            return manifest;
        }
    }

    public static String getManifestValue(Manifest manifest, Attributes.Name name) {
        return manifest.getMainAttributes().getValue(name);
    }

    public static List<URL> getClassPath(Manifest manifest, Path baseDir) throws MalformedURLException {
        String cp = ManifestUtil.getManifestValue(manifest, Attributes.Name.CLASS_PATH);
        if (cp == null) {
            return null;
        }
        StringTokenizer tokenizer = new StringTokenizer(cp);
        ArrayList<URL> ret = new ArrayList<URL>();
        URL context = UrlUtil.asUrl(baseDir);
        while (tokenizer.hasMoreElements()) {
            ret.add(new URL(context, tokenizer.nextToken()));
        }
        return ret;
    }
}

