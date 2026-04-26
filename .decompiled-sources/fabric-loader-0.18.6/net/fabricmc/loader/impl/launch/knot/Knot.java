/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.launch.knot;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.FormattedException;
import net.fabricmc.loader.impl.game.GameProvider;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.fabricmc.loader.impl.launch.FabricMixinBootstrap;
import net.fabricmc.loader.impl.launch.knot.KnotClassLoaderInterface;
import net.fabricmc.loader.impl.util.LoaderUtil;
import net.fabricmc.loader.impl.util.SystemProperties;
import net.fabricmc.loader.impl.util.UrlUtil;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;

public final class Knot
extends FabricLauncherBase {
    protected Map<String, Object> properties = new HashMap<String, Object>();
    private KnotClassLoaderInterface classLoader;
    private EnvType envType;
    private final List<Path> classPath = new ArrayList<Path>();
    private GameProvider provider;
    private boolean unlocked;

    public static void launch(String[] args, EnvType type) {
        Knot.setupUncaughtExceptionHandler();
        try {
            Knot knot = new Knot(type);
            ClassLoader cl = knot.init(args);
            if (knot.provider == null) {
                throw new IllegalStateException("Game provider was not initialized! (Knot#init(String[]))");
            }
            knot.provider.launch(cl);
        }
        catch (FormattedException e) {
            Knot.handleFormattedException(e);
        }
    }

    public Knot(EnvType type) {
        this.envType = type;
    }

    public ClassLoader init(String[] args) {
        Knot.setProperties(this.properties);
        if (this.envType == null) {
            String side = System.getProperty("fabric.side");
            if (side == null) {
                throw new RuntimeException("Please specify side or use a dedicated Knot!");
            }
            switch (side.toLowerCase(Locale.ROOT)) {
                case "client": {
                    this.envType = EnvType.CLIENT;
                    break;
                }
                case "server": {
                    this.envType = EnvType.SERVER;
                    break;
                }
                default: {
                    throw new RuntimeException("Invalid side provided: must be \"client\" or \"server\"!");
                }
            }
        }
        this.classPath.clear();
        ArrayList<String> missing = null;
        ArrayList<String> unsupported = null;
        for (String cpEntry : System.getProperty("java.class.path").split(File.pathSeparator)) {
            if (cpEntry.equals("*") || cpEntry.endsWith(File.separator + "*")) {
                if (unsupported == null) {
                    unsupported = new ArrayList<String>();
                }
                unsupported.add(cpEntry);
                continue;
            }
            Path path = Paths.get(cpEntry, new String[0]);
            if (!Files.exists(path, new LinkOption[0])) {
                if (missing == null) {
                    missing = new ArrayList<String>();
                }
                missing.add(cpEntry);
                continue;
            }
            this.classPath.add(LoaderUtil.normalizeExistingPath(path));
        }
        if (unsupported != null) {
            Log.warn(LogCategory.KNOT, "Knot does not support wildcard class path entries: %s - the game may not load properly!", String.join((CharSequence)", ", unsupported));
        }
        if (missing != null) {
            Log.warn(LogCategory.KNOT, "Class path entries reference missing files: %s - the game may not load properly!", String.join((CharSequence)", ", missing));
        }
        this.provider = this.createGameProvider(args);
        Log.finishBuiltinConfig();
        Log.info(LogCategory.GAME_PROVIDER, "Loading %s %s with Fabric Loader %s", this.provider.getGameName(), this.provider.getRawGameVersion(), "0.18.6");
        boolean useCompatibility = this.provider.requiresUrlClassLoader() || SystemProperties.isSet("fabric.loader.useCompatibilityClassLoader");
        this.classLoader = KnotClassLoaderInterface.create(useCompatibility, this.isDevelopment(), this.envType, this.provider);
        ClassLoader cl = this.classLoader.getClassLoader();
        Thread.currentThread().setContextClassLoader(cl);
        FabricLoaderImpl loader = FabricLoaderImpl.INSTANCE;
        loader.setGameProvider(this.provider);
        this.provider.initialize(this);
        loader.load();
        loader.freeze();
        FabricLoaderImpl.INSTANCE.loadClassTweakers();
        FabricMixinBootstrap.init(this.getEnvironmentType(), loader);
        FabricLauncherBase.finishMixinBootstrapping();
        this.classLoader.initializeTransformers();
        this.provider.unlockClassPath(this);
        this.unlocked = true;
        try {
            loader.invokeEntrypoints("preLaunch", PreLaunchEntrypoint.class, PreLaunchEntrypoint::onPreLaunch);
        }
        catch (RuntimeException e) {
            throw FormattedException.ofLocalized("exception.initializerFailure", e);
        }
        return cl;
    }

    private GameProvider createGameProvider(String[] args) {
        GameProvider embeddedGameProvider = Knot.findEmbedddedGameProvider();
        if (embeddedGameProvider != null && embeddedGameProvider.isEnabled() && embeddedGameProvider.locateGame(this, args)) {
            return embeddedGameProvider;
        }
        ArrayList<GameProvider> failedProviders = new ArrayList<GameProvider>();
        for (GameProvider provider : ServiceLoader.load(GameProvider.class)) {
            if (!provider.isEnabled()) continue;
            if (provider != embeddedGameProvider && provider.locateGame(this, args)) {
                return provider;
            }
            failedProviders.add(provider);
        }
        String msg = failedProviders.isEmpty() ? "No game providers present on the class path!" : (failedProviders.size() == 1 ? String.format("%s game provider couldn't locate the game! The game may be absent from the class path, lacks some expected files, suffers from jar corruption or is of an unsupported variety/version.", ((GameProvider)failedProviders.get(0)).getGameName()) : String.format("None of the game providers (%s) were able to locate their game!", failedProviders.stream().map(GameProvider::getGameName).collect(Collectors.joining(", "))));
        Log.error(LogCategory.GAME_PROVIDER, msg);
        throw new RuntimeException(msg);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static GameProvider findEmbedddedGameProvider() {
        try {
            Path flPath = UrlUtil.getCodeSource(Knot.class);
            if (flPath == null) return null;
            if (!flPath.getFileName().toString().endsWith(".jar")) {
                return null;
            }
            try (ZipFile zf = new ZipFile(flPath.toFile());){
                ZipEntry entry = zf.getEntry("META-INF/services/net.fabricmc.loader.impl.game.GameProvider");
                if (entry == null) {
                    GameProvider gameProvider = null;
                    return gameProvider;
                }
                try (InputStream is = zf.getInputStream(entry);){
                    int len;
                    byte[] buffer = new byte[100];
                    int offset = 0;
                    while ((len = is.read(buffer, offset, buffer.length - offset)) >= 0) {
                        if ((offset += len) != buffer.length) continue;
                        buffer = Arrays.copyOf(buffer, buffer.length * 2);
                    }
                    String content = new String(buffer, 0, offset, StandardCharsets.UTF_8).trim();
                    if (content.indexOf(10) >= 0) {
                        GameProvider gameProvider = null;
                        return gameProvider;
                    }
                    int pos = content.indexOf(35);
                    if (pos >= 0) {
                        content = content.substring(0, pos).trim();
                    }
                    if (content.isEmpty()) return null;
                    GameProvider gameProvider = (GameProvider)Class.forName(content).getConstructor(new Class[0]).newInstance(new Object[0]);
                    return gameProvider;
                }
            }
        }
        catch (IOException | ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Path> getClassPath() {
        return this.classPath;
    }

    @Override
    public void addToClassPath(Path path, String ... allowedPrefixes) {
        Log.debug(LogCategory.KNOT, "Adding " + path + " to classpath.");
        this.classLoader.setAllowedPrefixes(path, allowedPrefixes);
        this.classLoader.addCodeSource(path);
    }

    @Override
    public void setAllowedPrefixes(Path path, String ... prefixes) {
        this.classLoader.setAllowedPrefixes(path, prefixes);
    }

    @Override
    public void setValidParentClassPath(Collection<Path> paths) {
        this.classLoader.setValidParentClassPath(paths);
    }

    @Override
    public EnvType getEnvironmentType() {
        return this.envType;
    }

    @Override
    public boolean isClassLoaded(String name) {
        return this.classLoader.isClassLoaded(name);
    }

    @Override
    public Class<?> loadIntoTarget(String name) throws ClassNotFoundException {
        return this.classLoader.loadIntoTarget(name);
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return this.classLoader.getClassLoader().getResourceAsStream(name);
    }

    @Override
    public ClassLoader getTargetClassLoader() {
        KnotClassLoaderInterface classLoader = this.classLoader;
        return classLoader != null ? classLoader.getClassLoader() : null;
    }

    @Override
    public byte[] getClassByteArray(String name, boolean runTransformers) throws IOException {
        if (!this.unlocked) {
            throw new IllegalStateException("early getClassByteArray access");
        }
        if (runTransformers) {
            return this.classLoader.getPreMixinClassBytes(name);
        }
        return this.classLoader.getRawClassBytes(name);
    }

    @Override
    public Manifest getManifest(Path originPath) {
        return this.classLoader.getManifest(originPath);
    }

    @Override
    public String getEntrypoint() {
        return this.provider.getEntrypoint();
    }

    public static void main(String[] args) {
        new Knot(null).init(args);
    }

    static {
        LoaderUtil.verifyNotInTargetCl(Knot.class);
        LoaderUtil.verifyClasspath();
    }
}

