/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.api;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.MappingResolver;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.ObjectShare;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.impl.FabricLoaderImpl;

public interface FabricLoader {
    public static FabricLoader getInstance() {
        FabricLoaderImpl ret = FabricLoaderImpl.INSTANCE;
        if (ret == null) {
            throw new RuntimeException("Accessed FabricLoader too early!");
        }
        return ret;
    }

    public <T> List<T> getEntrypoints(String var1, Class<T> var2);

    public <T> List<EntrypointContainer<T>> getEntrypointContainers(String var1, Class<T> var2);

    public <T> void invokeEntrypoints(String var1, Class<T> var2, Consumer<? super T> var3);

    public ObjectShare getObjectShare();

    public MappingResolver getMappingResolver();

    public Optional<ModContainer> getModContainer(String var1);

    public Collection<ModContainer> getAllMods();

    public boolean isModLoaded(String var1);

    public boolean isDevelopmentEnvironment();

    public EnvType getEnvironmentType();

    public String getRawGameVersion();

    @Deprecated
    public Object getGameInstance();

    public Path getGameDir();

    @Deprecated
    public File getGameDirectory();

    public Path getConfigDir();

    @Deprecated
    public File getConfigDirectory();

    public String[] getLaunchArguments(boolean var1);
}

