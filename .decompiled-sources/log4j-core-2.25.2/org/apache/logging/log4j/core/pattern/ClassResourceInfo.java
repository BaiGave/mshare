/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import java.net.URL;
import java.security.CodeSource;
import java.util.function.Consumer;

final class ClassResourceInfo {
    static final ClassResourceInfo UNKNOWN = new ClassResourceInfo();
    private final Consumer<StringBuilder> renderer;
    final Class<?> clazz;

    private ClassResourceInfo() {
        this.renderer = buffer -> buffer.append("~[?:?]");
        this.clazz = null;
    }

    ClassResourceInfo(Class<?> clazz, boolean exact) {
        String exactnessPrefix = exact ? "" : "~";
        String location = ClassResourceInfo.getLocation(clazz);
        String version = ClassResourceInfo.getVersion(clazz);
        this.renderer = buffer -> {
            buffer.append(exactnessPrefix);
            buffer.append("[");
            buffer.append(location);
            buffer.append(":");
            buffer.append(version);
            buffer.append("]");
        };
        this.clazz = clazz;
    }

    private static String getLocation(Class<?> clazz) {
        try {
            URL locationUrl;
            CodeSource source = clazz.getProtectionDomain().getCodeSource();
            if (source != null && (locationUrl = source.getLocation()) != null) {
                String normalizedLocationUrl = locationUrl.toString().replace('\\', '/');
                int separatorIndex = normalizedLocationUrl.lastIndexOf("/");
                if (separatorIndex >= 0 && separatorIndex == normalizedLocationUrl.length() - 1) {
                    separatorIndex = normalizedLocationUrl.lastIndexOf("/", separatorIndex - 1);
                }
                return normalizedLocationUrl.substring(separatorIndex + 1);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return "?";
    }

    private static String getVersion(Class<?> clazz) {
        String version;
        Package classPackage = clazz.getPackage();
        if (classPackage != null && (version = classPackage.getImplementationVersion()) != null) {
            return version;
        }
        return "?";
    }

    void render(StringBuilder buffer) {
        this.renderer.accept(buffer);
    }
}

