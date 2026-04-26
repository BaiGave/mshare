/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server;

import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import joptsimple.AbstractOptionSpec;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;
import joptsimple.util.PathConverter;
import joptsimple.util.PathProperties;
import net.minecraft.CrashReport;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.SharedConstants;
import net.minecraft.SuppressForbidden;
import net.minecraft.commands.Commands;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.Eula;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.WorldStem;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.profiling.jfr.Environment;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.util.worldupdate.UpgradeProgress;
import net.minecraft.util.worldupdate.WorldUpgrader;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.chunk.storage.RegionFileVersion;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelDataAndDimensions;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class Main {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SuppressForbidden(reason="System.out needed before bootstrap")
    public static void main(String[] args) {
        SharedConstants.tryDetectVersion();
        OptionParser parser = new OptionParser();
        OptionSpecBuilder nogui = parser.accepts("nogui");
        OptionSpecBuilder initSettings = parser.accepts("initSettings", "Initializes 'server.properties' and 'eula.txt', then quits");
        OptionSpecBuilder demo = parser.accepts("demo");
        OptionSpecBuilder bonusChest = parser.accepts("bonusChest");
        OptionSpecBuilder forceUpgrade = parser.accepts("forceUpgrade");
        OptionSpecBuilder eraseCache = parser.accepts("eraseCache");
        OptionSpecBuilder recreateRegionFiles = parser.accepts("recreateRegionFiles");
        OptionSpecBuilder safeMode = parser.accepts("safeMode", "Loads level with vanilla datapack only");
        AbstractOptionSpec help = parser.accepts("help").forHelp();
        ArgumentAcceptingOptionSpec<String> universe = parser.accepts("universe").withRequiredArg().defaultsTo(".", (String[])new String[0]);
        ArgumentAcceptingOptionSpec<String> worldName = parser.accepts("world").withRequiredArg();
        ArgumentAcceptingOptionSpec<Integer> port = parser.accepts("port").withRequiredArg().ofType(Integer.class).defaultsTo(-1, (Integer[])new Integer[0]);
        ArgumentAcceptingOptionSpec<String> serverId = parser.accepts("serverId").withRequiredArg();
        OptionSpecBuilder jfrProfilingOption = parser.accepts("jfrProfile");
        ArgumentAcceptingOptionSpec<Path> pidFile = parser.accepts("pidFile").withRequiredArg().withValuesConvertedBy(new PathConverter(new PathProperties[0]));
        NonOptionArgumentSpec<String> nonOptions = parser.nonOptions();
        try {
            WorldStem worldStem;
            Dynamic<?> levelDataTag;
            OptionSet options = parser.parse(args);
            if (options.has(help)) {
                parser.printHelpOn(System.err);
                return;
            }
            Path pidFilePath = options.valueOf(pidFile);
            if (pidFilePath != null) {
                Main.writePidFile(pidFilePath);
            }
            CrashReport.preload();
            if (options.has(jfrProfilingOption)) {
                JvmProfiler.INSTANCE.start(Environment.SERVER);
            }
            Bootstrap.bootStrap();
            Bootstrap.validate();
            Util.startTimerHackThread();
            Path settingsFile = Paths.get("server.properties", new String[0]);
            DedicatedServerSettings settings = new DedicatedServerSettings(settingsFile);
            settings.forceSave();
            RegionFileVersion.configure(settings.getProperties().regionFileComression);
            Path eulaFile = Paths.get("eula.txt", new String[0]);
            Eula eula = new Eula(eulaFile);
            if (options.has(initSettings)) {
                LOGGER.info("Initialized '{}' and '{}'", (Object)settingsFile.toAbsolutePath(), (Object)eulaFile.toAbsolutePath());
                return;
            }
            if (!eula.hasAgreedToEULA()) {
                LOGGER.info("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
                return;
            }
            File universePath = new File(options.valueOf(universe));
            Services services = Services.create(new YggdrasilAuthenticationService(Proxy.NO_PROXY), universePath);
            String levelName = Optional.ofNullable(options.valueOf(worldName)).orElse(settings.getProperties().levelName);
            LevelStorageSource levelStorageSource = LevelStorageSource.createDefault(universePath.toPath());
            LevelStorageSource.LevelStorageAccess access = levelStorageSource.validateAndCreateAccess(levelName);
            if (access.hasWorldData()) {
                Dynamic<?> levelDataUnfixed;
                try {
                    levelDataUnfixed = access.getUnfixedDataTagWithFallback();
                }
                catch (IOException | NbtException | ReportedNbtException ex) {
                    LOGGER.error("Failed to load world data. World files may be corrupted. Shutting down.", ex);
                    return;
                }
                LevelSummary summary = access.fixAndGetSummaryFromTag(levelDataUnfixed);
                if (summary.requiresManualConversion()) {
                    LOGGER.info("This world must be opened in an older version (like 1.6.4) to be safely converted");
                    return;
                }
                if (!summary.isCompatible()) {
                    LOGGER.info("This world was created by an incompatible version.");
                    return;
                }
                levelDataTag = DataFixers.getFileFixer().fix(access, levelDataUnfixed, new UpgradeProgress());
            } else {
                levelDataTag = null;
            }
            boolean safeModeEnabled = options.has(safeMode);
            if (safeModeEnabled) {
                LOGGER.warn("Safe mode active, only vanilla datapack will be loaded");
            }
            PackRepository packRepository = ServerPacksSource.createPackRepository(access);
            try {
                WorldLoader.InitConfig worldLoadConfig = Main.loadOrCreateConfig(settings.getProperties(), levelDataTag, safeModeEnabled, packRepository);
                worldStem = (WorldStem)Util.blockUntilDone(executor -> WorldLoader.load(worldLoadConfig, context -> {
                    HolderLookup.RegistryLookup datapackDimensions = context.datapackDimensions().lookupOrThrow(Registries.LEVEL_STEM);
                    if (levelDataTag != null) {
                        LevelDataAndDimensions worldData = LevelStorageSource.getLevelDataAndDimensions(access, levelDataTag, context.dataConfiguration(), (Registry<LevelStem>)datapackDimensions, context.datapackWorldgen());
                        return new WorldLoader.DataLoadOutput<LevelDataAndDimensions.WorldDataAndGenSettings>(worldData.worldDataAndGenSettings(), worldData.dimensions().dimensionsRegistryAccess());
                    }
                    LOGGER.info("No existing world data, creating new world");
                    return Main.createNewWorldData(settings, context, (Registry<LevelStem>)datapackDimensions, options.has(demo), options.has(bonusChest));
                }, WorldStem::new, Util.backgroundExecutor(), executor)).get();
            }
            catch (Exception e) {
                LOGGER.warn("Failed to load datapacks, can't proceed with server load. You can either fix your datapacks or reset to vanilla with --safeMode", e);
                return;
            }
            RegistryAccess.Frozen registryHolder = worldStem.registries().compositeAccess();
            WorldData data = worldStem.worldDataAndGenSettings().data();
            boolean recreateRegionFilesValue = options.has(recreateRegionFiles);
            if (options.has(forceUpgrade) || recreateRegionFilesValue) {
                Main.forceUpgrade(access, DataFixers.getDataFixer(), options.has(eraseCache), () -> true, registryHolder, recreateRegionFilesValue);
            }
            access.saveDataTag(data);
            final DedicatedServer dedicatedServer = MinecraftServer.spin(thread -> {
                boolean gui;
                DedicatedServer server = new DedicatedServer((Thread)thread, access, packRepository, worldStem, Optional.empty(), settings, DataFixers.getDataFixer(), services);
                server.setPort((Integer)options.valueOf(port));
                server.setDemo(options.has(demo));
                server.setId((String)options.valueOf(serverId));
                boolean bl = gui = !options.has(nogui) && !options.valuesOf(nonOptions).contains("nogui");
                if (gui && !GraphicsEnvironment.isHeadless()) {
                    server.showGui();
                }
                return server;
            });
            Thread shutdownThread = new Thread("Server Shutdown Thread"){

                @Override
                public void run() {
                    dedicatedServer.halt(true);
                }
            };
            shutdownThread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
            Runtime.getRuntime().addShutdownHook(shutdownThread);
        }
        catch (Throwable t) {
            LOGGER.error(LogUtils.FATAL_MARKER, "Failed to start the minecraft server", t);
        }
    }

    private static WorldLoader.DataLoadOutput<LevelDataAndDimensions.WorldDataAndGenSettings> createNewWorldData(DedicatedServerSettings settings, WorldLoader.DataLoadContext context, Registry<LevelStem> datapackDimensions, boolean demoMode, boolean bonusChest) {
        WorldDimensions dimensions;
        WorldOptions worldOptions;
        LevelSettings createLevelSettings;
        if (demoMode) {
            createLevelSettings = MinecraftServer.DEMO_SETTINGS;
            worldOptions = WorldOptions.DEMO_OPTIONS;
            dimensions = WorldPresets.createNormalWorldDimensions(context.datapackWorldgen());
        } else {
            DedicatedServerProperties properties = settings.getProperties();
            createLevelSettings = new LevelSettings(properties.levelName, properties.gameMode.get(), new LevelSettings.DifficultySettings(properties.difficulty.get(), properties.hardcore, false), false, context.dataConfiguration());
            worldOptions = bonusChest ? properties.worldOptions.withBonusChest(true) : properties.worldOptions;
            dimensions = properties.createDimensions(context.datapackWorldgen());
        }
        WorldDimensions.Complete finalDimensions = dimensions.bake(datapackDimensions);
        Lifecycle lifecycle = finalDimensions.lifecycle().add(context.datapackWorldgen().allRegistriesLifecycle());
        PrimaryLevelData primaryLevelData = new PrimaryLevelData(createLevelSettings, finalDimensions.specialWorldProperty(), lifecycle);
        return new WorldLoader.DataLoadOutput<LevelDataAndDimensions.WorldDataAndGenSettings>(new LevelDataAndDimensions.WorldDataAndGenSettings(primaryLevelData, new WorldGenSettings(worldOptions, dimensions)), finalDimensions.dimensionsRegistryAccess());
    }

    private static void writePidFile(Path path) {
        try {
            long pid = ProcessHandle.current().pid();
            Files.writeString(path, (CharSequence)Long.toString(pid), new OpenOption[0]);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static WorldLoader.InitConfig loadOrCreateConfig(DedicatedServerProperties properties, @Nullable Dynamic<?> levelDataTag, boolean safeModeEnabled, PackRepository packRepository) {
        WorldDataConfiguration dataConfigToUse;
        boolean initMode;
        if (levelDataTag != null) {
            WorldDataConfiguration storedConfiguration = LevelStorageSource.readDataConfig(levelDataTag);
            initMode = false;
            dataConfigToUse = storedConfiguration;
        } else {
            initMode = true;
            dataConfigToUse = new WorldDataConfiguration(properties.initialDataPackConfiguration, FeatureFlags.DEFAULT_FLAGS);
        }
        WorldLoader.PackConfig packConfig = new WorldLoader.PackConfig(packRepository, dataConfigToUse, safeModeEnabled, initMode);
        return new WorldLoader.InitConfig(packConfig, Commands.CommandSelection.DEDICATED, properties.functionPermissions);
    }

    private static void forceUpgrade(LevelStorageSource.LevelStorageAccess storageSource, DataFixer fixerUpper, boolean eraseCache, BooleanSupplier isRunning, RegistryAccess registryAccess, boolean recreateRegionFiles) {
        LOGGER.info("Forcing world upgrade!");
        try (WorldUpgrader upgrader = new WorldUpgrader(storageSource, fixerUpper, registryAccess, eraseCache, recreateRegionFiles);){
            Component lastStatus = null;
            while (!upgrader.isFinished()) {
                int totalChunks;
                Component status = upgrader.getStatus();
                if (lastStatus != status) {
                    lastStatus = status;
                    LOGGER.info(upgrader.getStatus().getString());
                }
                if ((totalChunks = upgrader.getTotalChunks()) > 0) {
                    int done = upgrader.getConverted() + upgrader.getSkipped();
                    LOGGER.info("{}% completed ({} / {} chunks)...", Mth.floor((float)done / (float)totalChunks * 100.0f), done, totalChunks);
                }
                if (!isRunning.getAsBoolean()) {
                    upgrader.cancel();
                    continue;
                }
                try {
                    Thread.sleep(1000L);
                }
                catch (InterruptedException interruptedException) {}
            }
        }
    }
}

