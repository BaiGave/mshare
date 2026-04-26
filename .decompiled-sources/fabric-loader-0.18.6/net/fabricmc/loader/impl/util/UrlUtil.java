/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.util;

import java.io.File;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import net.fabricmc.loader.impl.util.ExceptionUtil;
import net.fabricmc.loader.impl.util.UrlConversionException;

public final class UrlUtil {
    public static final Path LOADER_CODE_SOURCE = UrlUtil.getCodeSource(UrlUtil.class);

    public static Path getCodeSource(URL url, String localPath) throws UrlConversionException {
        try {
            URLConnection connection = url.openConnection();
            if (connection instanceof JarURLConnection) {
                return UrlUtil.asPath(((JarURLConnection)connection).getJarFileURL());
            }
            URI uri = url.toURI();
            String path = uri.getPath();
            if (path.endsWith(localPath)) {
                String basePath = path.substring(0, path.length() - localPath.length());
                URI baseUri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), basePath, uri.getQuery(), uri.getFragment());
                return Paths.get(baseUri);
            }
            throw new UrlConversionException("Could not figure out code source for file '" + localPath + "' in URL '" + url + "'!");
        }
        catch (Exception e) {
            throw new UrlConversionException(e);
        }
    }

    public static Path asPath(URL url) {
        try {
            return Paths.get(url.toURI());
        }
        catch (URISyntaxException e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    public static URL asUrl(File file) throws MalformedURLException {
        return file.toURI().toURL();
    }

    public static URL asUrl(Path path) throws MalformedURLException {
        return path.toUri().toURL();
    }

    public static Path getCodeSource(Class<?> cls) {
        CodeSource cs = cls.getProtectionDomain().getCodeSource();
        if (cs == null) {
            return null;
        }
        return UrlUtil.asPath(cs.getLocation());
    }
}

