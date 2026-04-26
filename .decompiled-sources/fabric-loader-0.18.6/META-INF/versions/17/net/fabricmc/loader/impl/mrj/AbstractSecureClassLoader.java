/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.mrj;

import java.security.SecureClassLoader;

/*
 * Multiple versions of this class in jar - see https://www.benf.org/other/cfr/multi-version-jar.html
 */
public abstract class AbstractSecureClassLoader
extends SecureClassLoader {
    public AbstractSecureClassLoader(String name, ClassLoader parent) {
        super(name, parent);
    }

    static {
        ClassLoader.registerAsParallelCapable();
    }
}

