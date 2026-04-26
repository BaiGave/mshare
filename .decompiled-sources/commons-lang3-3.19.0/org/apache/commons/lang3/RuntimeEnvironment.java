/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.Arrays;

public class RuntimeEnvironment {
    private static boolean fileExists(String path) {
        return Files.exists(Paths.get(path, new String[0]), new LinkOption[0]);
    }

    public static Boolean inContainer() {
        return RuntimeEnvironment.inContainer("");
    }

    static boolean inContainer(String dirPrefix) {
        String value = RuntimeEnvironment.readFile(dirPrefix + "/proc/1/environ", "container");
        if (value != null) {
            return !value.isEmpty();
        }
        return RuntimeEnvironment.fileExists(dirPrefix + "/.dockerenv") || RuntimeEnvironment.fileExists(dirPrefix + "/run/.containerenv");
    }

    private static String readFile(String envVarFile, String key) {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(envVarFile, new String[0]));
            String content = new String(bytes, Charset.defaultCharset());
            String[] lines = content.split(String.valueOf('\u0000'));
            String prefix = key + "=";
            return Arrays.stream(lines).filter(line -> line.startsWith(prefix)).map(line -> line.split("=", 2)).map(keyValue -> keyValue[1]).findFirst().orElse(null);
        }
        catch (IOException e) {
            return null;
        }
    }

    @Deprecated
    public RuntimeEnvironment() {
    }
}

