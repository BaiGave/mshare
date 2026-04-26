/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.launch.knot;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.jar.Manifest;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.game.GameProvider;
import net.fabricmc.loader.impl.launch.knot.KnotClassLoader;
import net.fabricmc.loader.impl.launch.knot.KnotCompatibilityClassLoader;

interface KnotClassLoaderInterface {
    public static KnotClassLoaderInterface create(boolean useCompatibility, boolean isDevelopment, EnvType envType, GameProvider provider) {
        if (useCompatibility) {
            return new KnotCompatibilityClassLoader(isDevelopment, envType, provider).getDelegate();
        }
        return new KnotClassLoader(isDevelopment, envType, provider).getDelegate();
    }

    public void initializeTransformers();

    public ClassLoader getClassLoader();

    public void addCodeSource(Path var1);

    public void setAllowedPrefixes(Path var1, String ... var2);

    public void setValidParentClassPath(Collection<Path> var1);

    public Manifest getManifest(Path var1);

    public boolean isClassLoaded(String var1);

    public Class<?> loadIntoTarget(String var1) throws ClassNotFoundException;

    public byte[] getRawClassBytes(String var1) throws IOException;

    public byte[] getPreMixinClassBytes(String var1);
}

