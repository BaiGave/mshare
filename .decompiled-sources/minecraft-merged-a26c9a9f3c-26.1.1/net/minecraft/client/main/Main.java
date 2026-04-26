/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.main;

import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;
import com.mojang.blaze3d.TracyBootstrap;
import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.jtracy.TracyClient;
import com.mojang.logging.LogUtils;
import com.mojang.util.UndashedUuid;
import java.io.File;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
import net.fabricmc.api.EnvType;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.Optionull;
import net.minecraft.SharedConstants;
import net.minecraft.client.ClientBootstrap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.main.SilentInitException;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.client.telemetry.TelemetryProperty;
import net.minecraft.client.telemetry.events.GameLoadTimesEvent;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.NativeModuleLister;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.profiling.jfr.Environment;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@net.fabricmc.api.Environment(value=EnvType.CLIENT)
public class Main {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void main(String[] args) {
        GameConfig gameConfig;
        Logger logger;
        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("demo");
        parser.accepts("disableMultiplayer");
        parser.accepts("disableChat");
        parser.accepts("fullscreen");
        parser.accepts("checkGlErrors");
        OptionSpecBuilder renderDebugLabelsOption = parser.accepts("renderDebugLabels");
        OptionSpecBuilder jfrProfilingOption = parser.accepts("jfrProfile");
        OptionSpecBuilder tracyProfilingOption = parser.accepts("tracy");
        OptionSpecBuilder tracyNoImageOption = parser.accepts("tracyNoImages");
        ArgumentAcceptingOptionSpec<String> quickPlayPathOption = parser.accepts("quickPlayPath").withRequiredArg();
        ArgumentAcceptingOptionSpec<String> quickPlaySingleplayerOption = parser.accepts("quickPlaySingleplayer").withOptionalArg();
        ArgumentAcceptingOptionSpec<String> quickPlayMultiplayerOption = parser.accepts("quickPlayMultiplayer").withRequiredArg();
        ArgumentAcceptingOptionSpec<String> quickPlayRealmsOption = parser.accepts("quickPlayRealms").withRequiredArg();
        ArgumentAcceptingOptionSpec<File> gameDirOption = parser.accepts("gameDir").withRequiredArg().ofType(File.class).defaultsTo(new File("."), (File[])new File[0]);
        ArgumentAcceptingOptionSpec<File> assetsDirOption = parser.accepts("assetsDir").withRequiredArg().ofType(File.class);
        ArgumentAcceptingOptionSpec<File> resourcePackDirOption = parser.accepts("resourcePackDir").withRequiredArg().ofType(File.class);
        ArgumentAcceptingOptionSpec<String> proxyHostOption = parser.accepts("proxyHost").withRequiredArg();
        ArgumentAcceptingOptionSpec<Integer> proxyPortOption = parser.accepts("proxyPort").withRequiredArg().defaultsTo("8080", (String[])new String[0]).ofType(Integer.class);
        ArgumentAcceptingOptionSpec<String> proxyUserOption = parser.accepts("proxyUser").withRequiredArg();
        ArgumentAcceptingOptionSpec<String> proxyPassOption = parser.accepts("proxyPass").withRequiredArg();
        ArgumentAcceptingOptionSpec<String> usernameOption = parser.accepts("username").withRequiredArg().defaultsTo("Player" + System.currentTimeMillis() % 1000L, (String[])new String[0]);
        OptionSpecBuilder offlineDeveloperMode = parser.accepts("offlineDeveloperMode");
        ArgumentAcceptingOptionSpec<String> uuidOption = parser.accepts("uuid").withRequiredArg();
        ArgumentAcceptingOptionSpec<String> xuidOption = parser.accepts("xuid").withOptionalArg().defaultsTo("", (String[])new String[0]);
        ArgumentAcceptingOptionSpec<String> clientIdOption = parser.accepts("clientId").withOptionalArg().defaultsTo("", (String[])new String[0]);
        ArgumentAcceptingOptionSpec<String> accessTokenOption = parser.accepts("accessToken").withRequiredArg().required();
        ArgumentAcceptingOptionSpec<String> versionOption = parser.accepts("version").withRequiredArg().required();
        ArgumentAcceptingOptionSpec<Integer> widthOption = parser.accepts("width").withRequiredArg().ofType(Integer.class).defaultsTo(854, (Integer[])new Integer[0]);
        ArgumentAcceptingOptionSpec<Integer> heightOption = parser.accepts("height").withRequiredArg().ofType(Integer.class).defaultsTo(480, (Integer[])new Integer[0]);
        ArgumentAcceptingOptionSpec<Integer> fullscreenWidthOption = parser.accepts("fullscreenWidth").withRequiredArg().ofType(Integer.class);
        ArgumentAcceptingOptionSpec<Integer> fullscreenHeightOption = parser.accepts("fullscreenHeight").withRequiredArg().ofType(Integer.class);
        ArgumentAcceptingOptionSpec<String> assetIndexOption = parser.accepts("assetIndex").withRequiredArg();
        ArgumentAcceptingOptionSpec<String> versionTypeString = parser.accepts("versionType").withRequiredArg().defaultsTo("release", (String[])new String[0]);
        NonOptionArgumentSpec<String> nonOption = parser.nonOptions();
        OptionSet optionSet = parser.parse(args);
        File gameDir = Main.parseArgument(optionSet, gameDirOption);
        String launchedVersion = Main.parseArgument(optionSet, versionOption);
        String stage = "Pre-bootstrap";
        try {
            if (optionSet.has(jfrProfilingOption)) {
                JvmProfiler.INSTANCE.start(Environment.CLIENT);
            }
            if (optionSet.has(tracyProfilingOption)) {
                TracyBootstrap.setup();
            }
            Stopwatch totalTimePreClassLoadTimer = Stopwatch.createStarted(Ticker.systemTicker());
            Stopwatch preWindowPreClassLoadTimer = Stopwatch.createStarted(Ticker.systemTicker());
            GameLoadTimesEvent.INSTANCE.beginStep(TelemetryProperty.LOAD_TIME_TOTAL_TIME_MS, totalTimePreClassLoadTimer);
            GameLoadTimesEvent.INSTANCE.beginStep(TelemetryProperty.LOAD_TIME_PRE_WINDOW_MS, preWindowPreClassLoadTimer);
            SharedConstants.tryDetectVersion();
            TracyClient.reportAppInfo("Minecraft Java Edition " + SharedConstants.getCurrentVersion().name());
            CompletableFuture<?> dataFixerOptimization = DataFixers.optimize(DataFixTypes.TYPES_FOR_LEVEL_LIST);
            CrashReport.preload();
            logger = LogUtils.getLogger();
            stage = "Bootstrap";
            Bootstrap.bootStrap();
            ClientBootstrap.bootstrap();
            GameLoadTimesEvent.INSTANCE.setBootstrapTime(Bootstrap.bootstrapDuration.get());
            Bootstrap.validate();
            stage = "Argument parsing";
            List<String> leftoverArgs = optionSet.valuesOf(nonOption);
            if (!leftoverArgs.isEmpty()) {
                logger.info("Completely ignored arguments: {}", (Object)leftoverArgs);
            }
            String hostName = Main.parseArgument(optionSet, proxyHostOption);
            Proxy proxy = Proxy.NO_PROXY;
            if (hostName != null) {
                try {
                    proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(hostName, (int)Main.parseArgument(optionSet, proxyPortOption)));
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            final String proxyUser = Main.parseArgument(optionSet, proxyUserOption);
            final String proxyPass = Main.parseArgument(optionSet, proxyPassOption);
            if (!proxy.equals(Proxy.NO_PROXY) && Main.stringHasValue(proxyUser) && Main.stringHasValue(proxyPass)) {
                Authenticator.setDefault(new Authenticator(){

                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(proxyUser, proxyPass.toCharArray());
                    }
                });
            }
            int width = Main.parseArgument(optionSet, widthOption);
            int height = Main.parseArgument(optionSet, heightOption);
            OptionalInt fullscreenWidth = Main.ofNullable(Main.parseArgument(optionSet, fullscreenWidthOption));
            OptionalInt fullscreenHeight = Main.ofNullable(Main.parseArgument(optionSet, fullscreenHeightOption));
            boolean isFullscreen = optionSet.has("fullscreen");
            boolean isDemo = optionSet.has("demo");
            boolean disableMultiplayer = optionSet.has("disableMultiplayer");
            boolean disableChat = optionSet.has("disableChat");
            boolean captureTracyImages = !optionSet.has(tracyNoImageOption);
            boolean renderDebugLabels = optionSet.has(renderDebugLabelsOption);
            String versionType = Main.parseArgument(optionSet, versionTypeString);
            File assetsDir = optionSet.has(assetsDirOption) ? Main.parseArgument(optionSet, assetsDirOption) : new File(gameDir, "assets/");
            File resourcePackDir = optionSet.has(resourcePackDirOption) ? Main.parseArgument(optionSet, resourcePackDirOption) : new File(gameDir, "resourcepacks/");
            UUID uuid = Main.hasValidUuid(uuidOption, optionSet, logger) ? UndashedUuid.fromStringLenient((String)uuidOption.value(optionSet)) : UUIDUtil.createOfflinePlayerUUID((String)usernameOption.value(optionSet));
            String assetIndex = optionSet.has(assetIndexOption) ? (String)assetIndexOption.value(optionSet) : null;
            String xuid = optionSet.valueOf(xuidOption);
            String clientId = optionSet.valueOf(clientIdOption);
            String quickPlayLogPath = Main.parseArgument(optionSet, quickPlayPathOption);
            GameConfig.QuickPlayVariant quickPlayVariant = Main.getQuickPlayVariant(optionSet, quickPlaySingleplayerOption, quickPlayMultiplayerOption, quickPlayRealmsOption);
            User user = new User((String)usernameOption.value(optionSet), uuid, (String)accessTokenOption.value(optionSet), Main.emptyStringToEmptyOptional(xuid), Main.emptyStringToEmptyOptional(clientId));
            gameConfig = new GameConfig(new GameConfig.UserData(user, proxy), new DisplayData(width, height, fullscreenWidth, fullscreenHeight, isFullscreen), new GameConfig.FolderData(gameDir, resourcePackDir, assetsDir, assetIndex), new GameConfig.GameData(isDemo, launchedVersion, versionType, disableMultiplayer, disableChat, captureTracyImages, renderDebugLabels, optionSet.has(offlineDeveloperMode)), new GameConfig.QuickPlayData(quickPlayLogPath, quickPlayVariant));
            Util.startTimerHackThread();
            dataFixerOptimization.join();
        }
        catch (Throwable t) {
            CrashReport report = CrashReport.forThrowable(t, stage);
            CrashReportCategory initialization = report.addCategory("Initialization");
            NativeModuleLister.addCrashSection(initialization);
            Minecraft.fillReport(null, null, launchedVersion, null, report);
            Minecraft.crash(null, gameDir, report);
            return;
        }
        Thread shutdownThread = new Thread("Client Shutdown Thread"){

            @Override
            public void run() {
                Minecraft instance = Minecraft.getInstance();
                if (instance == null) {
                    return;
                }
                IntegratedServer server = instance.getSingleplayerServer();
                if (server != null) {
                    server.halt(true);
                }
            }
        };
        shutdownThread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(logger));
        Runtime.getRuntime().addShutdownHook(shutdownThread);
        Minecraft newMinecraft = null;
        try {
            Thread.currentThread().setName("Render thread");
            RenderSystem.initRenderThread();
            newMinecraft = new Minecraft(gameConfig);
        }
        catch (SilentInitException e) {
            Util.shutdownExecutors();
            logger.warn("Failed to create window: ", e);
            return;
        }
        catch (Throwable t) {
            CrashReport report = CrashReport.forThrowable(t, "Initializing game");
            CrashReportCategory initialization = report.addCategory("Initialization");
            NativeModuleLister.addCrashSection(initialization);
            Minecraft.fillReport(newMinecraft, null, gameConfig.game.launchVersion, null, report);
            Minecraft.crash(newMinecraft, gameConfig.location.gameDirectory, report);
            return;
        }
        Minecraft minecraft = newMinecraft;
        minecraft.run();
        try {
            minecraft.stop();
        }
        finally {
            minecraft.destroy();
        }
    }

    private static GameConfig.QuickPlayVariant getQuickPlayVariant(OptionSet optionSet, OptionSpec<String> quickPlaySingleplayerOption, OptionSpec<String> quickPlayMultiplayerOption, OptionSpec<String> quickPlayRealmsOption) {
        long enabledOptions = Stream.of(quickPlaySingleplayerOption, quickPlayMultiplayerOption, quickPlayRealmsOption).filter(optionSet::has).count();
        if (enabledOptions == 0L) {
            return GameConfig.QuickPlayVariant.DISABLED;
        }
        if (enabledOptions > 1L) {
            throw new IllegalArgumentException("Only one quick play option can be specified");
        }
        if (optionSet.has(quickPlaySingleplayerOption)) {
            String worldId = Main.unescapeJavaArgument(Main.parseArgument(optionSet, quickPlaySingleplayerOption));
            return new GameConfig.QuickPlaySinglePlayerData(worldId);
        }
        if (optionSet.has(quickPlayMultiplayerOption)) {
            String serverAddress = Main.unescapeJavaArgument(Main.parseArgument(optionSet, quickPlayMultiplayerOption));
            return Optionull.mapOrDefault(serverAddress, GameConfig.QuickPlayMultiplayerData::new, GameConfig.QuickPlayVariant.DISABLED);
        }
        if (optionSet.has(quickPlayRealmsOption)) {
            String realmId = Main.unescapeJavaArgument(Main.parseArgument(optionSet, quickPlayRealmsOption));
            return Optionull.mapOrDefault(realmId, GameConfig.QuickPlayRealmsData::new, GameConfig.QuickPlayVariant.DISABLED);
        }
        return GameConfig.QuickPlayVariant.DISABLED;
    }

    private static @Nullable String unescapeJavaArgument(@Nullable String arg) {
        if (arg == null) {
            return null;
        }
        return StringEscapeUtils.unescapeJava(arg);
    }

    private static Optional<String> emptyStringToEmptyOptional(String xuid) {
        return xuid.isEmpty() ? Optional.empty() : Optional.of(xuid);
    }

    private static OptionalInt ofNullable(@Nullable Integer value) {
        return value != null ? OptionalInt.of(value) : OptionalInt.empty();
    }

    private static <T> @Nullable T parseArgument(OptionSet optionSet, OptionSpec<T> optionSpec) {
        try {
            return optionSet.valueOf(optionSpec);
        }
        catch (Throwable t) {
            ArgumentAcceptingOptionSpec options;
            List defaultValues;
            if (optionSpec instanceof ArgumentAcceptingOptionSpec && !(defaultValues = (options = (ArgumentAcceptingOptionSpec)optionSpec).defaultValues()).isEmpty()) {
                return (T)defaultValues.get(0);
            }
            throw t;
        }
    }

    private static boolean stringHasValue(@Nullable String string) {
        return string != null && !string.isEmpty();
    }

    private static boolean hasValidUuid(OptionSpec<String> uuidOption, OptionSet optionSet, Logger logger) {
        return optionSet.has(uuidOption) && Main.isUuidValid(uuidOption, optionSet, logger);
    }

    private static boolean isUuidValid(OptionSpec<String> uuidOption, OptionSet optionSet, Logger logger) {
        try {
            UndashedUuid.fromStringLenient(uuidOption.value(optionSet));
        }
        catch (IllegalArgumentException e) {
            logger.warn("Invalid UUID: '{}", (Object)uuidOption.value(optionSet));
            return false;
        }
        return true;
    }

    static {
        System.setProperty("java.awt.headless", "true");
    }
}

