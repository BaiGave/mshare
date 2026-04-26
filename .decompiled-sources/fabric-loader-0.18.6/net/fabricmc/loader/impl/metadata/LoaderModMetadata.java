/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.metadata;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.ModDependency;
import net.fabricmc.loader.impl.metadata.EntrypointMetadata;
import net.fabricmc.loader.impl.metadata.NestedJarEntry;

public interface LoaderModMetadata
extends net.fabricmc.loader.metadata.LoaderModMetadata {
    public int getSchemaVersion();

    default public String getOldStyleLanguageAdapter() {
        return "net.fabricmc.loader.language.JavaLanguageAdapter";
    }

    public Map<String, String> getLanguageAdapterDefinitions();

    public Collection<NestedJarEntry> getJars();

    public Collection<String> getMixinConfigs(EnvType var1);

    public String getClassTweaker();

    @Override
    public boolean loadsInEnvironment(EnvType var1);

    public Collection<String> getOldInitializers();

    public List<EntrypointMetadata> getEntrypoints(String var1);

    @Override
    public Collection<String> getEntrypointKeys();

    public void emitFormatWarnings();

    public void setVersion(Version var1);

    public void setDependencies(Collection<ModDependency> var1);
}

