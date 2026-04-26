/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.utils;

import java.io.File;
import java.nio.file.Path;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.file.PathUtils;

@Deprecated
public class FileNameUtils {
    @Deprecated
    public static String getBaseName(Path path) {
        return PathUtils.getBaseName(path);
    }

    @Deprecated
    public static String getBaseName(String fileName) {
        if (fileName == null) {
            return null;
        }
        return FilenameUtils.removeExtension(new File(fileName).getName());
    }

    @Deprecated
    public static String getExtension(Path path) {
        return PathUtils.getExtension(path);
    }

    @Deprecated
    public static String getExtension(String fileName) {
        return FilenameUtils.getExtension(fileName);
    }
}

