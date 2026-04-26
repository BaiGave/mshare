/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.game;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.FormattedException;
import net.fabricmc.loader.impl.launch.FabricLauncher;
import net.fabricmc.loader.impl.launch.MappingConfiguration;
import net.fabricmc.loader.impl.lib.mappingio.tree.MappingTree;
import net.fabricmc.loader.impl.lib.tinyremapper.InputTag;
import net.fabricmc.loader.impl.lib.tinyremapper.NonClassCopyMode;
import net.fabricmc.loader.impl.lib.tinyremapper.OutputConsumerPath;
import net.fabricmc.loader.impl.lib.tinyremapper.TinyRemapper;
import net.fabricmc.loader.impl.lib.tinyremapper.TinyUtils;
import net.fabricmc.loader.impl.util.LoaderUtil;
import net.fabricmc.loader.impl.util.SystemProperties;
import net.fabricmc.loader.impl.util.UrlConversionException;
import net.fabricmc.loader.impl.util.UrlUtil;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.fabricmc.loader.impl.util.log.TinyRemapperLoggerAdapter;
import org.jetbrains.annotations.Nullable;

public final class GameProviderHelper {
    private static boolean emittedInfo = false;

    private GameProviderHelper() {
    }

    public static Path getCommonGameJar() {
        return GameProviderHelper.getGameJar("fabric.gameJarPath");
    }

    public static Path getEnvGameJar(EnvType env) {
        return GameProviderHelper.getGameJar(env == EnvType.CLIENT ? "fabric.gameJarPath.client" : "fabric.gameJarPath.server");
    }

    private static Path getGameJar(String property) {
        String val = System.getProperty(property);
        if (val == null) {
            return null;
        }
        Path path = Paths.get(val, new String[0]);
        if (!Files.exists(path, new LinkOption[0])) {
            throw new RuntimeException("Game jar " + path + " (" + LoaderUtil.normalizePath(path) + ") configured through " + property + " system property doesn't exist");
        }
        return LoaderUtil.normalizeExistingPath(path);
    }

    @Nullable
    public static List<Path> getLibraries(String property) {
        String value = System.getProperty(property);
        if (value == null) {
            return null;
        }
        ArrayList<Path> ret = new ArrayList<Path>();
        for (String pathStr : value.split(File.pathSeparator)) {
            if (pathStr.isEmpty()) continue;
            if (pathStr.startsWith("@")) {
                Path path = Paths.get(pathStr.substring(1), new String[0]);
                if (!Files.isRegularFile(path, new LinkOption[0])) {
                    Log.warn(LogCategory.GAME_PROVIDER, "Skipping missing/invalid library list file %s", path);
                    continue;
                }
                try (BufferedReader reader = Files.newBufferedReader(path);){
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if ((line = line.trim()).isEmpty()) continue;
                        GameProviderHelper.addLibrary(line, ret);
                    }
                    continue;
                }
                catch (IOException e) {
                    throw new RuntimeException(String.format("Error reading library list file %s", path), e);
                }
            }
            GameProviderHelper.addLibrary(pathStr, ret);
        }
        return ret;
    }

    public static void addLibrary(String pathStr, List<Path> out) {
        Path path = LoaderUtil.normalizePath(Paths.get(pathStr, new String[0]));
        if (!Files.exists(path, new LinkOption[0])) {
            Log.warn(LogCategory.GAME_PROVIDER, "Skipping missing library path %s", path);
        } else {
            out.add(path);
        }
    }

    public static Optional<Path> getSource(ClassLoader loader, String filename) {
        URL url = loader.getResource(filename);
        if (url != null) {
            try {
                return Optional.of(UrlUtil.getCodeSource(url, filename));
            }
            catch (UrlConversionException e) {
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }

    public static List<Path> getSources(ClassLoader loader, String filename) {
        try {
            Enumeration<URL> urls = loader.getResources(filename);
            ArrayList<Path> paths = new ArrayList<Path>();
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                try {
                    paths.add(UrlUtil.getCodeSource(url, filename));
                }
                catch (UrlConversionException e) {
                    e.printStackTrace();
                }
            }
            return paths;
        }
        catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public static FindResult findFirst(List<Path> paths, Map<Path, ZipFile> zipFiles, boolean isClassName, String ... names) {
        for (String name : names) {
            String file = isClassName ? LoaderUtil.getClassFileName(name) : name;
            for (Path path : paths) {
                if (Files.isDirectory(path, new LinkOption[0])) {
                    if (!Files.exists(path.resolve(file), new LinkOption[0])) continue;
                    return new FindResult(name, path);
                }
                ZipFile zipFile = zipFiles.get(path);
                if (zipFile == null) {
                    try {
                        zipFile = new ZipFile(path.toFile());
                        zipFiles.put(path, zipFile);
                    }
                    catch (IOException e) {
                        throw new RuntimeException("Error reading " + path, e);
                    }
                }
                if (zipFile.getEntry(file) == null) continue;
                return new FindResult(name, path);
            }
        }
        return null;
    }

    public static Map<String, Path> deobfuscate(Map<String, Path> inputFileMap, String sourceNamespace, String gameId, String gameVersion, Path gameDir, FabricLauncher launcher) {
        Log.debug(LogCategory.GAME_REMAP, "Requesting deobfuscation of %s", inputFileMap);
        MappingConfiguration mappingConfig = launcher.getMappingConfiguration();
        String targetNamespace = mappingConfig.getRuntimeNamespace();
        if (sourceNamespace.equals(targetNamespace)) {
            return inputFileMap;
        }
        if (!mappingConfig.matches(gameId, gameVersion)) {
            String mappingsGameId = mappingConfig.getGameId();
            String mappingsGameVersion = mappingConfig.getGameVersion();
            throw new FormattedException("Incompatible mappings", String.format("Supplied mappings for %s %s are incompatible with %s %s, this is likely caused by launcher misbehavior", mappingsGameId != null ? mappingsGameId : "(unknown)", mappingsGameVersion != null ? mappingsGameVersion : "(unknown)", gameId, gameVersion));
        }
        List<String> namespaces = mappingConfig.getNamespaces();
        if (namespaces == null || !namespaces.contains(sourceNamespace) || !namespaces.contains(targetNamespace)) {
            Log.debug(LogCategory.GAME_REMAP, "No mappings, using input files");
            return inputFileMap;
        }
        if (!namespaces.contains(targetNamespace) || !namespaces.contains(sourceNamespace)) {
            Log.debug(LogCategory.GAME_REMAP, "Missing namespace in mappings, using input files");
            return inputFileMap;
        }
        Path deobfJarDir = GameProviderHelper.getDeobfJarDir(gameDir, gameId, gameVersion);
        ArrayList<Path> inputFiles = new ArrayList<Path>(inputFileMap.size());
        ArrayList<Path> outputFiles = new ArrayList<Path>(inputFileMap.size());
        ArrayList<Path> tmpFiles = new ArrayList<Path>(inputFileMap.size());
        HashMap<String, Path> ret = new HashMap<String, Path>(inputFileMap.size());
        boolean anyMissing = false;
        for (Map.Entry<String, Path> entry : inputFileMap.entrySet()) {
            String name = entry.getKey();
            Path inputFile = entry.getValue();
            String deobfJarFilename = String.format("%s-%s.jar", name, targetNamespace);
            Path outputFile = deobfJarDir.resolve(deobfJarFilename);
            Path tmpFile = deobfJarDir.resolve(deobfJarFilename + ".tmp");
            if (Files.exists(tmpFile, new LinkOption[0])) {
                Log.warn(LogCategory.GAME_REMAP, "Incomplete remapped file found! This means that the remapping process failed on the previous launch. If this persists, make sure to let us at Fabric know!");
                try {
                    Files.deleteIfExists(outputFile);
                    Files.deleteIfExists(tmpFile);
                }
                catch (IOException e) {
                    throw new RuntimeException("can't delete incompletely remapped files", e);
                }
            }
            inputFiles.add(inputFile);
            outputFiles.add(outputFile);
            tmpFiles.add(tmpFile);
            ret.put(name, outputFile);
            if (anyMissing || Files.exists(outputFile, new LinkOption[0])) continue;
            anyMissing = true;
        }
        if (!anyMissing) {
            Log.debug(LogCategory.GAME_REMAP, "Remapped files exist already, reusing them");
            return ret;
        }
        Log.debug(LogCategory.GAME_REMAP, "Fabric mapping file detected, applying...");
        if (!emittedInfo) {
            Log.info(LogCategory.GAME_REMAP, "Fabric is preparing JARs on first launch, this may take a few seconds...");
            emittedInfo = true;
        }
        try {
            Files.createDirectories(deobfJarDir, new FileAttribute[0]);
            GameProviderHelper.deobfuscate0(inputFiles, outputFiles, tmpFiles, mappingConfig.getMappings(), sourceNamespace, targetNamespace, launcher);
        }
        catch (IOException e) {
            throw new RuntimeException("error remapping game jars " + inputFiles, e);
        }
        return ret;
    }

    private static Path getDeobfJarDir(Path gameDir, String gameId, String gameVersion) {
        Path ret = gameDir.resolve(".fabric").resolve("remappedJars");
        StringBuilder versionDirName = new StringBuilder();
        if (!gameId.isEmpty()) {
            versionDirName.append(gameId);
        }
        if (!gameVersion.isEmpty()) {
            if (versionDirName.length() > 0) {
                versionDirName.append('-');
            }
            versionDirName.append(gameVersion);
        }
        if (versionDirName.length() > 0) {
            versionDirName.append('-');
        }
        versionDirName.append("0.18.6");
        return ret.resolve(versionDirName.toString().replaceAll("[^\\w\\-\\. ]+", "_"));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void deobfuscate0(List<Path> inputFiles, List<Path> outputFiles, List<Path> tmpFiles, MappingTree mappings, String sourceNamespace, String targetNamespace, FabricLauncher launcher) throws IOException {
        TinyRemapper remapper = TinyRemapper.newRemapper(new TinyRemapperLoggerAdapter(LogCategory.GAME_REMAP)).withMappings(TinyUtils.createMappingProvider(mappings, sourceNamespace, targetNamespace)).rebuildSourceFilenames(true).build();
        HashSet<Path> depPaths = new HashSet<Path>();
        if (SystemProperties.isSet("fabric.debug.deobfuscateWithClasspath")) {
            for (Path path : launcher.getClassPath()) {
                if (inputFiles.contains(path)) continue;
                depPaths.add(path);
                Log.debug(LogCategory.GAME_REMAP, "Appending '%s' to remapper classpath", path);
                remapper.readClassPathAsync(path);
            }
        }
        ArrayList<OutputConsumerPath> outputConsumers = new ArrayList<OutputConsumerPath>(inputFiles.size());
        ArrayList<InputTag> inputTags = new ArrayList<InputTag>(inputFiles.size());
        try {
            int i;
            for (i = 0; i < inputFiles.size(); ++i) {
                Path inputFile = inputFiles.get(i);
                Path tmpFile = tmpFiles.get(i);
                InputTag inputTag = remapper.createInputTag();
                OutputConsumerPath outputConsumer = new OutputConsumerPath.Builder(tmpFile).assumeArchive(true).build();
                outputConsumers.add(outputConsumer);
                inputTags.add(inputTag);
                outputConsumer.addNonClassFiles(inputFile, NonClassCopyMode.FIX_META_INF, remapper);
                remapper.readInputsAsync(inputTag, inputFile);
            }
            for (i = 0; i < inputFiles.size(); ++i) {
                remapper.apply((BiConsumer)outputConsumers.get(i), (InputTag)inputTags.get(i));
            }
        }
        finally {
            for (OutputConsumerPath outputConsumer : outputConsumers) {
                outputConsumer.close();
            }
            remapper.finish();
        }
        depPaths.addAll(tmpFiles);
        for (Path p : depPaths) {
            try {
                p.getFileSystem().close();
            }
            catch (Exception tmpFile) {
                // empty catch block
            }
            try {
                FileSystems.getFileSystem(new URI("jar:" + p.toUri())).close();
            }
            catch (Exception tmpFile) {}
        }
        ArrayList<Path> missing = new ArrayList<Path>();
        for (int i = 0; i < inputFiles.size(); ++i) {
            boolean found;
            Path inputFile = inputFiles.get(i);
            Path tmpFile = tmpFiles.get(i);
            Path outputFile = outputFiles.get(i);
            try (JarFile jar = new JarFile(tmpFile.toFile());){
                found = jar.stream().anyMatch(e -> e.getName().endsWith(".class"));
            }
            if (!found) {
                missing.add(inputFile);
                Files.delete(tmpFile);
                continue;
            }
            Files.move(tmpFile, outputFile, new CopyOption[0]);
        }
        if (!missing.isEmpty()) {
            throw new RuntimeException("Generated deobfuscated JARs contain no classes: " + missing);
        }
    }

    public static final class FindResult {
        public final String name;
        public final Path path;

        FindResult(String name, Path path) {
            this.name = name;
            this.path = path;
        }
    }
}

