/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.launch;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.jar.Manifest;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.launch.MappingConfiguration;

public interface FabricLauncher {
    public MappingConfiguration getMappingConfiguration();

    public void addToClassPath(Path var1, String ... var2);

    public void setAllowedPrefixes(Path var1, String ... var2);

    public void setValidParentClassPath(Collection<Path> var1);

    public EnvType getEnvironmentType();

    public boolean isClassLoaded(String var1);

    public Class<?> loadIntoTarget(String var1) throws ClassNotFoundException;

    public InputStream getResourceAsStream(String var1);

    public ClassLoader getTargetClassLoader();

    public byte[] getClassByteArray(String var1, boolean var2) throws IOException;

    public Manifest getManifest(Path var1);

    public boolean isDevelopment();

    public String getEntrypoint();

    public List<Path> getClassPath();
}

