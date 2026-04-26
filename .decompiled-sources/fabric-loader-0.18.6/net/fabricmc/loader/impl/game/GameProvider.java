/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.game;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.impl.game.patch.GameTransformer;
import net.fabricmc.loader.impl.launch.FabricLauncher;
import net.fabricmc.loader.impl.util.Arguments;
import net.fabricmc.loader.impl.util.LoaderUtil;

public interface GameProvider {
    public String getGameId();

    public String getGameName();

    public String getRawGameVersion();

    public String getNormalizedGameVersion();

    public Collection<BuiltinMod> getBuiltinMods();

    public String getEntrypoint();

    public Path getLaunchDirectory();

    public boolean requiresUrlClassLoader();

    public Set<BuiltinTransform> getBuiltinTransforms(String var1);

    public boolean isEnabled();

    public boolean locateGame(FabricLauncher var1, String[] var2);

    public void initialize(FabricLauncher var1);

    public GameTransformer getEntrypointTransformer();

    public void unlockClassPath(FabricLauncher var1);

    public void launch(ClassLoader var1);

    default public boolean displayCrash(Throwable exception, String context) {
        return false;
    }

    public Arguments getArguments();

    public String[] getLaunchArguments(boolean var1);

    default public String getRuntimeNamespace(String defaultNs) {
        return defaultNs;
    }

    default public String getDefaultModDistributionNamespace(String defaultNs) {
        return defaultNs;
    }

    default public boolean canOpenErrorGui() {
        return true;
    }

    default public boolean hasAwtSupport() {
        return LoaderUtil.hasAwtSupport();
    }

    public static class BuiltinMod {
        public final List<Path> paths;
        public final ModMetadata metadata;

        public BuiltinMod(List<Path> paths, ModMetadata metadata) {
            Objects.requireNonNull(paths, "null paths");
            Objects.requireNonNull(metadata, "null metadata");
            this.paths = paths;
            this.metadata = metadata;
        }
    }

    public static enum BuiltinTransform {
        STRIP_ENVIRONMENT,
        WIDEN_ALL_PACKAGE_ACCESS,
        CLASS_TWEAKS;

    }
}

