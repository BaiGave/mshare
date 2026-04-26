/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.launch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.ModDependency;
import net.fabricmc.loader.api.metadata.version.VersionInterval;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.ModContainerImpl;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.fabricmc.loader.impl.launch.MappingConfiguration;
import net.fabricmc.loader.impl.launch.knot.MixinServiceKnot;
import net.fabricmc.loader.impl.launch.knot.MixinServiceKnotBootstrap;
import net.fabricmc.loader.impl.lib.mappingio.tree.MappingTree;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.fabricmc.loader.impl.util.mappings.MixinIntermediaryDevRemapper;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.transformer.Config;

public final class FabricMixinBootstrap {
    private static boolean initialized = false;

    private FabricMixinBootstrap() {
    }

    public static void init(EnvType side, FabricLoaderImpl loader) {
        if (initialized) {
            throw new RuntimeException("FabricMixinBootstrap has already been initialized!");
        }
        System.setProperty("mixin.bootstrapService", MixinServiceKnotBootstrap.class.getName());
        System.setProperty("mixin.service", MixinServiceKnot.class.getName());
        MixinBootstrap.init();
        if (FabricLauncherBase.getLauncher().isDevelopment()) {
            MappingConfiguration config = FabricLauncherBase.getLauncher().getMappingConfiguration();
            Iterator<Config> mappings = config.getMappings();
            String modNs = config.getDefaultModDistributionNamespace();
            String runtimeNs = config.getRuntimeNamespace();
            if (config.hasAnyMappings() && !modNs.equals(runtimeNs)) {
                ArrayList<String> namespaces = new ArrayList<String>(mappings.getDstNamespaces());
                namespaces.add(mappings.getSrcNamespace());
                if (namespaces.contains(modNs) && namespaces.contains(runtimeNs)) {
                    System.setProperty("mixin.env.remapRefMap", "true");
                    try {
                        MixinIntermediaryDevRemapper remapper = new MixinIntermediaryDevRemapper((MappingTree)((Object)mappings), modNs, runtimeNs);
                        MixinEnvironment.getDefaultEnvironment().getRemappers().add(remapper);
                        Log.info(LogCategory.MIXIN, "Loaded Fabric development mappings for mixin remapper!");
                    }
                    catch (Exception e) {
                        Log.error(LogCategory.MIXIN, "Fabric development environment setup error - the game will probably crash soon!", e);
                    }
                }
            }
        }
        HashMap<String, ModContainerImpl> configToModMap = new HashMap<String, ModContainerImpl>();
        for (ModContainerImpl mod : loader.getModsInternal()) {
            for (String config : mod.getMetadata().getMixinConfigs(side)) {
                ModContainerImpl prev = configToModMap.putIfAbsent(config, mod);
                if (prev != null) {
                    throw new RuntimeException(String.format("Non-unique Mixin config name %s used by the mods %s and %s", config, prev.getMetadata().getId(), mod.getMetadata().getId()));
                }
                try {
                    Mixins.addConfiguration(config);
                }
                catch (Throwable t) {
                    throw new RuntimeException(String.format("Error parsing or using Mixin config %s for mod %s", config, mod.getMetadata().getId()), t);
                }
            }
        }
        for (Config config : Mixins.getConfigs()) {
            ModContainerImpl mod = (ModContainerImpl)configToModMap.get(config.getName());
            if (mod != null) continue;
        }
        try {
            IMixinConfig.class.getMethod("decorate", String.class, Object.class);
            MixinConfigDecorator.apply(configToModMap);
        }
        catch (NoSuchMethodException e) {
            Log.info(LogCategory.MIXIN, "Detected old Mixin version without config decoration support");
        }
        initialized = true;
    }

    private static final class MixinConfigDecorator {
        private static final List<LoaderMixinVersionEntry> versions = new ArrayList<LoaderMixinVersionEntry>();

        private MixinConfigDecorator() {
        }

        static void apply(Map<String, ModContainerImpl> configToModMap) {
            for (Config rawConfig : Mixins.getConfigs()) {
                ModContainerImpl mod = configToModMap.get(rawConfig.getName());
                if (mod == null) continue;
                IMixinConfig config = rawConfig.getConfig();
                config.decorate("fabric-modId", mod.getMetadata().getId());
                config.decorate("fabric-compat", MixinConfigDecorator.getMixinCompat(mod));
            }
        }

        private static int getMixinCompat(ModContainerImpl mod) {
            List<VersionInterval> reqIntervals = Collections.singletonList(VersionInterval.INFINITE);
            for (ModDependency dep : mod.getMetadata().getDependencies()) {
                if (!dep.getModId().equals("fabricloader") && !dep.getModId().equals("fabric-loader")) continue;
                if (dep.getKind() == ModDependency.Kind.DEPENDS) {
                    reqIntervals = VersionInterval.and(reqIntervals, dep.getVersionIntervals());
                    continue;
                }
                if (dep.getKind() != ModDependency.Kind.BREAKS) continue;
                reqIntervals = VersionInterval.and(reqIntervals, VersionInterval.not(dep.getVersionIntervals()));
            }
            if (reqIntervals.isEmpty()) {
                throw new IllegalStateException("mod " + mod + " is incompatible with every loader version?");
            }
            Version minLoaderVersion = reqIntervals.get(0).getMin();
            if (minLoaderVersion != null) {
                for (LoaderMixinVersionEntry version : versions) {
                    if (minLoaderVersion.compareTo(version.loaderVersion) < 0) continue;
                    return version.mixinVersion;
                }
            }
            return 9002;
        }

        private static void addVersion(String minLoaderVersion, int mixinCompat) {
            try {
                versions.add(new LoaderMixinVersionEntry(SemanticVersion.parse(minLoaderVersion), mixinCompat));
            }
            catch (VersionParsingException e) {
                throw new RuntimeException(e);
            }
        }

        static {
            MixinConfigDecorator.addVersion("0.18.4", 17000);
            MixinConfigDecorator.addVersion("0.17.3", 16005);
            MixinConfigDecorator.addVersion("0.16.0", 14000);
            MixinConfigDecorator.addVersion("0.12.0-", 10000);
        }

        private static final class LoaderMixinVersionEntry {
            final SemanticVersion loaderVersion;
            final int mixinVersion;

            LoaderMixinVersionEntry(SemanticVersion loaderVersion, int mixinVersion) {
                this.loaderVersion = loaderVersion;
                this.mixinVersion = mixinVersion;
            }
        }
    }
}

