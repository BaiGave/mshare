/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.launch.common;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import net.fabricmc.api.EnvType;

@Deprecated
public interface FabricLauncher {
    public void propose(URL var1);

    public EnvType getEnvironmentType();

    public boolean isClassLoaded(String var1);

    public InputStream getResourceAsStream(String var1);

    public ClassLoader getTargetClassLoader();

    public byte[] getClassByteArray(String var1, boolean var2) throws IOException;

    public boolean isDevelopment();

    public Collection<URL> getLoadTimeDependencies();
}

