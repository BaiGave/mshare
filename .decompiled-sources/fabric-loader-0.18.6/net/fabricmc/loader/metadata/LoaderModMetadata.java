/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.metadata;

import java.util.Collection;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.metadata.EntrypointMetadata;

@Deprecated
public interface LoaderModMetadata
extends ModMetadata {
    public boolean loadsInEnvironment(EnvType var1);

    public List<? extends EntrypointMetadata> getEntrypoints(String var1);

    public Collection<String> getEntrypointKeys();
}

