/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.game.minecraft;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.ObjectShare;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.ModDependency;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.FormattedException;
import net.fabricmc.loader.impl.game.GameProvider;
import net.fabricmc.loader.impl.game.GameProviderHelper;
import net.fabricmc.loader.impl.game.LibClassifier;
import net.fabricmc.loader.impl.game.minecraft.BundlerProcessor;
import net.fabricmc.loader.impl.game.minecraft.McLibrary;
import net.fabricmc.loader.impl.game.minecraft.McVersion;
import net.fabricmc.loader.impl.game.minecraft.McVersionLookup;
import net.fabricmc.loader.impl.game.minecraft.patch.BrandingPatch;
import net.fabricmc.loader.impl.game.minecraft.patch.EntrypointPatch;
import net.fabricmc.loader.impl.game.minecraft.patch.EntrypointPatchFML125;
import net.fabricmc.loader.impl.game.minecraft.patch.TinyFDPatch;
import net.fabricmc.loader.impl.game.patch.GameTransformer;
import net.fabricmc.loader.impl.launch.FabricLauncher;
import net.fabricmc.loader.impl.launch.MappingConfiguration;
import net.fabricmc.loader.impl.metadata.BuiltinModMetadata;
import net.fabricmc.loader.impl.metadata.ModDependencyImpl;
import net.fabricmc.loader.impl.util.Arguments;
import net.fabricmc.loader.impl.util.ExceptionUtil;
import net.fabricmc.loader.impl.util.LoaderUtil;
import net.fabricmc.loader.impl.util.SystemProperties;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.fabricmc.loader.impl.util.log.LogHandler;

public class MinecraftGameProvider
implements GameProvider {
    private static final String[] ALLOWED_EARLY_CLASS_PREFIXES = new String[]{"org.apache.logging.log4j.", "com.mojang.util."};
    private static final Set<String> SENSITIVE_ARGS = new HashSet<String>(Arrays.asList("accesstoken", "clientid", "profileproperties", "proxypass", "proxyuser", "username", "userproperties", "uuid", "xuid"));
    private EnvType envType;
    private String entrypoint;
    private Arguments arguments;
    private final List<Path> gameJars = new ArrayList<Path>(2);
    private Path realmsJar;
    private final Set<Path> logJars = new HashSet<Path>();
    private boolean log4jAvailable;
    private boolean slf4jAvailable;
    private final List<Path> miscGameLibraries = new ArrayList<Path>();
    private Collection<Path> validParentClassPath;
    private McVersion versionData;
    private boolean hasModLoader = false;
    private final GameTransformer transformer = new GameTransformer(new EntrypointPatch(this), new BrandingPatch(), new EntrypointPatchFML125(), new TinyFDPatch());
    private static final Set<GameProvider.BuiltinTransform> TRANSFORM_WIDENALL_STRIPENV_CLASSTWEAKS = EnumSet.of(GameProvider.BuiltinTransform.WIDEN_ALL_PACKAGE_ACCESS, GameProvider.BuiltinTransform.STRIP_ENVIRONMENT, GameProvider.BuiltinTransform.CLASS_TWEAKS);
    private static final Set<GameProvider.BuiltinTransform> TRANSFORM_WIDENALL_CLASSTWEAKS = EnumSet.of(GameProvider.BuiltinTransform.WIDEN_ALL_PACKAGE_ACCESS, GameProvider.BuiltinTransform.CLASS_TWEAKS);
    private static final Set<GameProvider.BuiltinTransform> TRANSFORM_STRIPENV = EnumSet.of(GameProvider.BuiltinTransform.STRIP_ENVIRONMENT);

    @Override
    public String getGameId() {
        return "minecraft";
    }

    @Override
    public String getGameName() {
        return "Minecraft";
    }

    @Override
    public String getRawGameVersion() {
        return this.versionData.getRaw();
    }

    @Override
    public String getNormalizedGameVersion() {
        return this.versionData.getNormalized();
    }

    @Override
    public Collection<GameProvider.BuiltinMod> getBuiltinMods() {
        BuiltinModMetadata.Builder metadata = new BuiltinModMetadata.Builder(this.getGameId(), this.getNormalizedGameVersion()).setName(this.getGameName());
        if (this.versionData.getClassVersion().isPresent()) {
            int version = this.versionData.getClassVersion().getAsInt() - 44;
            try {
                metadata.addDependency(new ModDependencyImpl(ModDependency.Kind.DEPENDS, "java", Collections.singletonList(String.format(Locale.ENGLISH, ">=%d", version))));
            }
            catch (VersionParsingException e) {
                throw new RuntimeException(e);
            }
        }
        return Collections.singletonList(new GameProvider.BuiltinMod(this.gameJars, metadata.build()));
    }

    public Path getGameJar() {
        return this.gameJars.get(0);
    }

    @Override
    public String getEntrypoint() {
        return this.entrypoint;
    }

    @Override
    public Path getLaunchDirectory() {
        if (this.arguments == null) {
            return Paths.get(".", new String[0]);
        }
        return MinecraftGameProvider.getLaunchDirectory(this.arguments);
    }

    @Override
    public boolean requiresUrlClassLoader() {
        return this.hasModLoader;
    }

    @Override
    public Set<GameProvider.BuiltinTransform> getBuiltinTransforms(String className) {
        boolean isMinecraftClass;
        boolean bl = isMinecraftClass = className.startsWith("net.minecraft.") || className.startsWith("com.mojang.minecraft.") || className.startsWith("com.mojang.rubydung.") || className.startsWith("com.mojang.blaze3d.") || className.indexOf(46) < 0;
        if (isMinecraftClass) {
            if (FabricLoaderImpl.INSTANCE.isDevelopmentEnvironment()) {
                return TRANSFORM_WIDENALL_STRIPENV_CLASSTWEAKS;
            }
            return TRANSFORM_WIDENALL_CLASSTWEAKS;
        }
        return TRANSFORM_STRIPENV;
    }

    @Override
    public boolean isEnabled() {
        return !SystemProperties.isSet("fabric.skipMcProvider");
    }

    @Override
    public boolean locateGame(FabricLauncher launcher, String[] args) {
        String version;
        this.envType = launcher.getEnvironmentType();
        this.arguments = new Arguments();
        this.arguments.parse(args);
        try {
            Path assetsJar;
            boolean commonGameJarDeclared;
            LibClassifier<McLibrary> classifier = new LibClassifier<McLibrary>(McLibrary.class, this.envType, this);
            McLibrary envGameLib = this.envType == EnvType.CLIENT ? McLibrary.MC_CLIENT : McLibrary.MC_SERVER;
            Path commonGameJar = GameProviderHelper.getCommonGameJar();
            Path envGameJar = GameProviderHelper.getEnvGameJar(this.envType);
            boolean bl = commonGameJarDeclared = commonGameJar != null;
            if (commonGameJarDeclared) {
                if (envGameJar != null) {
                    classifier.process(envGameJar, new McLibrary[]{McLibrary.MC_COMMON});
                }
                classifier.process(commonGameJar, new McLibrary[0]);
            } else if (envGameJar != null) {
                classifier.process(envGameJar, new McLibrary[0]);
            }
            classifier.process(launcher.getClassPath(), new McLibrary[0]);
            if (classifier.has(McLibrary.MC_BUNDLER)) {
                BundlerProcessor.process(classifier);
            }
            if ((envGameJar = classifier.getOrigin(envGameLib)) == null) {
                return false;
            }
            commonGameJar = classifier.getOrigin(McLibrary.MC_COMMON);
            if (commonGameJarDeclared && commonGameJar == null) {
                Log.warn(LogCategory.GAME_PROVIDER, "The declared common game jar didn't contain any of the expected classes!");
            }
            this.gameJars.add(envGameJar);
            if (commonGameJar != null && !commonGameJar.equals(envGameJar)) {
                this.gameJars.add(commonGameJar);
            }
            if ((assetsJar = classifier.getOrigin(McLibrary.MC_ASSETS_ROOT)) != null && !assetsJar.equals(commonGameJar) && !assetsJar.equals(envGameJar)) {
                this.gameJars.add(assetsJar);
            }
            this.entrypoint = classifier.getClassName(envGameLib);
            this.realmsJar = classifier.getOrigin(McLibrary.REALMS);
            this.hasModLoader = classifier.has(McLibrary.MODLOADER);
            this.log4jAvailable = classifier.has(McLibrary.LOG4J_API) && classifier.has(McLibrary.LOG4J_CORE);
            this.slf4jAvailable = classifier.has(McLibrary.SLF4J_API) && classifier.has(McLibrary.SLF4J_CORE);
            boolean hasLogLib = this.log4jAvailable || this.slf4jAvailable;
            Log.configureBuiltin(hasLogLib, !hasLogLib);
            for (McLibrary lib : McLibrary.LOGGING) {
                Path path = classifier.getOrigin(lib);
                if (path == null) continue;
                if (hasLogLib) {
                    this.logJars.add(path);
                    continue;
                }
                if (this.gameJars.contains(path)) continue;
                this.miscGameLibraries.add(path);
            }
            this.miscGameLibraries.addAll(classifier.getUnmatchedOrigins());
            this.validParentClassPath = classifier.getSystemLibraries();
        }
        catch (IOException e) {
            throw ExceptionUtil.wrap(e);
        }
        ObjectShare share = FabricLoaderImpl.INSTANCE.getObjectShare();
        share.put("fabric-loader:inputGameJar", this.gameJars.get(0));
        share.put("fabric-loader:inputGameJars", Collections.unmodifiableList(new ArrayList<Path>(this.gameJars)));
        if (this.realmsJar != null) {
            share.put("fabric-loader:inputRealmsJar", this.realmsJar);
        }
        if ((version = this.arguments.remove("fabric.gameVersion")) == null) {
            version = System.getProperty("fabric.gameVersion");
        }
        this.versionData = McVersionLookup.getVersion(this.gameJars, this.entrypoint, version);
        MinecraftGameProvider.processArgumentMap(this.arguments, this.envType);
        return true;
    }

    private static void processArgumentMap(Arguments argMap, EnvType envType) {
        switch (envType) {
            case CLIENT: {
                if (!argMap.containsKey("accessToken")) {
                    argMap.put("accessToken", "FabricMC");
                }
                if (!argMap.containsKey("version")) {
                    argMap.put("version", "Fabric");
                }
                String versionType = "";
                if (argMap.containsKey("versionType") && !argMap.get("versionType").equalsIgnoreCase("release")) {
                    versionType = argMap.get("versionType") + "/";
                }
                argMap.put("versionType", versionType + "Fabric");
                if (argMap.containsKey("gameDir")) break;
                argMap.put("gameDir", MinecraftGameProvider.getLaunchDirectory(argMap).toAbsolutePath().normalize().toString());
                break;
            }
            case SERVER: {
                argMap.remove("version");
                argMap.remove("gameDir");
                argMap.remove("assetsDir");
            }
        }
    }

    private static Path getLaunchDirectory(Arguments argMap) {
        return Paths.get(argMap.getOrDefault("gameDir", "."), new String[0]);
    }

    @Override
    public void initialize(FabricLauncher launcher) {
        launcher.setValidParentClassPath(this.validParentClassPath);
        MappingConfiguration config = launcher.getMappingConfiguration();
        String runtimeNs = config.getRuntimeNamespace();
        String gameNs = System.getProperty("fabric.gameMappingNamespace");
        if (gameNs == null) {
            List<String> mappingNamespaces;
            gameNs = "official";
            if (config.hasAnyMappings() && (mappingNamespaces = config.getNamespaces()) != null) {
                if (launcher.isDevelopment() && mappingNamespaces.contains("named")) {
                    gameNs = "named";
                } else if (!mappingNamespaces.contains("official")) {
                    gameNs = this.envType == EnvType.CLIENT ? "clientOfficial" : "serverOfficial";
                }
            }
        }
        Log.debug(LogCategory.GAME_PROVIDER, "namespace detection result: game=%s runtime=%s mod-default=%s", gameNs, runtimeNs, config.getDefaultModDistributionNamespace());
        if (!gameNs.equals(runtimeNs)) {
            int i;
            Map<String, Path> obfJars = new HashMap<String, Path>(3);
            String[] names = new String[this.gameJars.size()];
            for (i = 0; i < this.gameJars.size(); ++i) {
                String name = i == 0 ? this.envType.name().toLowerCase(Locale.ENGLISH) : (i == 1 ? "common" : String.format(Locale.ENGLISH, "extra-%d", i - 2));
                obfJars.put(name, this.gameJars.get(i));
                names[i] = name;
            }
            if (this.realmsJar != null) {
                obfJars.put("realms", this.realmsJar);
            }
            obfJars = GameProviderHelper.deobfuscate(obfJars, gameNs, this.getGameId(), this.getNormalizedGameVersion(), this.getLaunchDirectory(), launcher);
            for (i = 0; i < this.gameJars.size(); ++i) {
                Path newJar = obfJars.get(names[i]);
                Path oldJar = this.gameJars.set(i, newJar);
                if (!this.logJars.remove(oldJar)) continue;
                this.logJars.add(newJar);
            }
            this.realmsJar = obfJars.get("realms");
        }
        if (!this.logJars.isEmpty() && !Boolean.getBoolean("fabric.unitTest")) {
            for (Path jar : this.logJars) {
                if (this.gameJars.contains(jar)) {
                    launcher.addToClassPath(jar, ALLOWED_EARLY_CLASS_PREFIXES);
                    continue;
                }
                launcher.addToClassPath(jar, new String[0]);
            }
        }
        this.setupLogHandler(launcher, true);
        this.transformer.locateEntrypoints(launcher, this.gameJars);
    }

    private void setupLogHandler(FabricLauncher launcher, boolean useTargetCl) {
        System.setProperty("log4j2.formatMsgNoLookups", "true");
        try {
            Class<?> logHandlerCls;
            String logHandlerClsName;
            if (this.log4jAvailable) {
                logHandlerClsName = "net.fabricmc.loader.impl.game.minecraft.Log4jLogHandler";
            } else if (this.slf4jAvailable) {
                logHandlerClsName = "net.fabricmc.loader.impl.game.minecraft.Slf4jLogHandler";
            } else {
                return;
            }
            ClassLoader prevCl = Thread.currentThread().getContextClassLoader();
            if (useTargetCl) {
                Thread.currentThread().setContextClassLoader(launcher.getTargetClassLoader());
                logHandlerCls = launcher.loadIntoTarget(logHandlerClsName);
            } else {
                logHandlerCls = Class.forName(logHandlerClsName);
            }
            Log.init((LogHandler)logHandlerCls.getConstructor(new Class[0]).newInstance(new Object[0]));
            Thread.currentThread().setContextClassLoader(prevCl);
        }
        catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Arguments getArguments() {
        return this.arguments;
    }

    @Override
    public String[] getLaunchArguments(boolean sanitize) {
        if (this.arguments == null) {
            return new String[0];
        }
        String[] ret = this.arguments.toArray();
        if (!sanitize) {
            return ret;
        }
        int writeIdx = 0;
        for (int i = 0; i < ret.length; ++i) {
            String arg = ret[i];
            if (i + 1 < ret.length && arg.startsWith("--") && SENSITIVE_ARGS.contains(arg.substring(2).toLowerCase(Locale.ENGLISH))) {
                ++i;
                continue;
            }
            ret[writeIdx++] = arg;
        }
        if (writeIdx < ret.length) {
            ret = Arrays.copyOf(ret, writeIdx);
        }
        return ret;
    }

    @Override
    public GameTransformer getEntrypointTransformer() {
        return this.transformer;
    }

    @Override
    public boolean canOpenErrorGui() {
        if (this.arguments == null || this.envType == EnvType.CLIENT) {
            return true;
        }
        List<String> extras = this.arguments.getExtraArgs();
        return !extras.contains("nogui") && !extras.contains("--nogui");
    }

    @Override
    public boolean hasAwtSupport() {
        return !LoaderUtil.hasMacOs();
    }

    @Override
    public void unlockClassPath(FabricLauncher launcher) {
        for (Path gameJar : this.gameJars) {
            if (this.logJars.contains(gameJar)) {
                launcher.setAllowedPrefixes(gameJar, new String[0]);
                continue;
            }
            launcher.addToClassPath(gameJar, new String[0]);
        }
        if (this.realmsJar != null) {
            launcher.addToClassPath(this.realmsJar, new String[0]);
        }
        for (Path lib : this.miscGameLibraries) {
            launcher.addToClassPath(lib, new String[0]);
        }
    }

    @Override
    public void launch(ClassLoader loader) {
        MethodHandle invoker;
        String targetClass = this.entrypoint;
        if (this.envType == EnvType.CLIENT && targetClass.contains("Applet")) {
            targetClass = "net.fabricmc.loader.impl.game.minecraft.applet.AppletMain";
        }
        try {
            Class<?> c = loader.loadClass(targetClass);
            invoker = MethodHandles.lookup().findStatic(c, "main", MethodType.methodType(Void.TYPE, String[].class));
        }
        catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException e) {
            throw FormattedException.ofLocalized("exception.minecraft.invokeFailure", e);
        }
        try {
            invoker.invokeExact(this.arguments.toArray());
        }
        catch (Throwable t) {
            throw FormattedException.ofLocalized("exception.minecraft.generic", t);
        }
    }
}

