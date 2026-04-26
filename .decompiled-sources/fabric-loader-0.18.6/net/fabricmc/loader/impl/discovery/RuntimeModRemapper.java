/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.discovery;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import net.fabricmc.loader.impl.FormattedException;
import net.fabricmc.loader.impl.discovery.ModCandidateImpl;
import net.fabricmc.loader.impl.launch.FabricLauncher;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.fabricmc.loader.impl.launch.MappingConfiguration;
import net.fabricmc.loader.impl.lib.classtweaker.api.ClassTweaker;
import net.fabricmc.loader.impl.lib.classtweaker.api.ClassTweakerReader;
import net.fabricmc.loader.impl.lib.classtweaker.api.ClassTweakerWriter;
import net.fabricmc.loader.impl.lib.classtweaker.visitors.ClassTweakerRemapperVisitor;
import net.fabricmc.loader.impl.lib.tinyremapper.InputTag;
import net.fabricmc.loader.impl.lib.tinyremapper.NonClassCopyMode;
import net.fabricmc.loader.impl.lib.tinyremapper.OutputConsumerPath;
import net.fabricmc.loader.impl.lib.tinyremapper.TinyRemapper;
import net.fabricmc.loader.impl.lib.tinyremapper.TinyUtils;
import net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.MixinExtension;
import net.fabricmc.loader.impl.util.FileSystemUtil;
import net.fabricmc.loader.impl.util.ManifestUtil;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.fabricmc.loader.impl.util.log.TinyRemapperLoggerAdapter;
import org.objectweb.asm.commons.Remapper;

public final class RuntimeModRemapper {
    private static final String REMAP_TYPE_MANIFEST_KEY = "Fabric-Loom-Mixin-Remap-Type";
    private static final String REMAP_TYPE_MIXIN = "mixin";
    private static final String REMAP_TYPE_STATIC = "static";

    public static void remap(Collection<ModCandidateImpl> modCandidates, Path tmpDir, Path outputDir) {
        String runtimeNs;
        ArrayList<ModCandidateImpl> modsToRemap = new ArrayList<ModCandidateImpl>();
        HashSet<InputTag> remapMixins = new HashSet<InputTag>();
        for (ModCandidateImpl mod : modCandidates) {
            if (!mod.getRequiresRemap()) continue;
            modsToRemap.add(mod);
        }
        if (modsToRemap.isEmpty()) {
            return;
        }
        MappingConfiguration config = FabricLauncherBase.getLauncher().getMappingConfiguration();
        String modNs = config.getDefaultModDistributionNamespace();
        if (modNs.equals(runtimeNs = config.getRuntimeNamespace()) || !config.hasAnyMappings()) {
            return;
        }
        HashMap<ModCandidateImpl, RemapInfo> infoMap = new HashMap<ModCandidateImpl, RemapInfo>();
        TinyRemapper remapper = null;
        try {
            RemapInfo info;
            FileSystem fs;
            FileSystemUtil.FileSystemDelegate jarFs;
            FabricLauncher launcher = FabricLauncherBase.getLauncher();
            ClassTweaker mergedClassTweaker = ClassTweaker.newInstance();
            mergedClassTweaker.visitHeader(modNs);
            for (ModCandidateImpl modCandidateImpl : modsToRemap) {
                RemapInfo info2 = new RemapInfo();
                infoMap.put(modCandidateImpl, info2);
                if (modCandidateImpl.hasPath()) {
                    List<Path> paths = modCandidateImpl.getPaths();
                    if (paths.size() != 1) {
                        throw new UnsupportedOperationException("multiple path for " + modCandidateImpl);
                    }
                    info2.inputPath = paths.get(0);
                } else {
                    info2.inputPath = modCandidateImpl.copyToDir(tmpDir, true);
                    info2.inputIsTemp = true;
                }
                info2.outputPath = outputDir.resolve(modCandidateImpl.getDefaultFileName());
                Files.deleteIfExists(info2.outputPath);
                String classTweaker = modCandidateImpl.getMetadata().getClassTweaker();
                if (classTweaker == null) continue;
                info2.classTweakerPath = classTweaker;
                try {
                    jarFs = FileSystemUtil.getJarFileSystem(info2.inputPath, false);
                    try {
                        fs = jarFs.get();
                        info2.classTweaker = Files.readAllBytes(fs.getPath(classTweaker, new String[0]));
                    }
                    finally {
                        if (jarFs != null) {
                            jarFs.close();
                        }
                    }
                }
                catch (Throwable t) {
                    throw new RuntimeException("Error reading class tweaker for mod '" + modCandidateImpl.getId() + "'!", t);
                }
                ClassTweakerReader.create(mergedClassTweaker).read(info2.classTweaker, modNs);
            }
            remapper = TinyRemapper.newRemapper(new TinyRemapperLoggerAdapter(LogCategory.MOD_REMAP)).withMappings(TinyUtils.createMappingProvider(launcher.getMappingConfiguration().getMappings(), modNs, runtimeNs)).renameInvalidLocals(false).extension(new MixinExtension(remapMixins::contains)).extraAnalyzeVisitor((mrjVersion, className, next) -> mergedClassTweaker.createClassVisitor(589824, next, null)).build();
            try {
                remapper.readClassPathAsync(RuntimeModRemapper.getRemapClasspath().toArray(new Path[0]));
            }
            catch (IOException e) {
                throw new RuntimeException("Failed to populate remap classpath", e);
            }
            String defaultMixinRemapType = System.getProperty("fabric.defaultMixinRemapType", REMAP_TYPE_MIXIN);
            for (ModCandidateImpl mod : modsToRemap) {
                InputTag tag;
                info = (RemapInfo)infoMap.get(mod);
                info.tag = tag = remapper.createInputTag();
                if (RuntimeModRemapper.requiresMixinRemap(info.inputPath, defaultMixinRemapType)) {
                    remapMixins.add(tag);
                }
                remapper.readInputsAsync(tag, info.inputPath);
            }
            for (ModCandidateImpl mod : modsToRemap) {
                info = (RemapInfo)infoMap.get(mod);
                OutputConsumerPath outputConsumer = new OutputConsumerPath.Builder(info.outputPath).build();
                FileSystemUtil.FileSystemDelegate delegate = FileSystemUtil.getJarFileSystem(info.inputPath, false);
                if (delegate.get() == null) {
                    throw new RuntimeException("Could not open JAR file " + info.inputPath.getFileName() + " for NIO reading!");
                }
                Path inputJar = delegate.get().getRootDirectories().iterator().next();
                outputConsumer.addNonClassFiles(inputJar, NonClassCopyMode.FIX_META_INF, remapper);
                info.outputConsumerPath = outputConsumer;
                remapper.apply(outputConsumer, info.tag);
            }
            for (ModCandidateImpl mod : modsToRemap) {
                info = (RemapInfo)infoMap.get(mod);
                if (info.classTweaker == null) continue;
                info.classTweaker = RuntimeModRemapper.remapClassTweaker(info.classTweaker, remapper.getEnvironment().getRemapper(), modNs, runtimeNs);
            }
            remapper.finish();
            for (ModCandidateImpl mod : modsToRemap) {
                info = (RemapInfo)infoMap.get(mod);
                info.outputConsumerPath.close();
                if (info.classTweakerPath != null) {
                    jarFs = FileSystemUtil.getJarFileSystem(info.outputPath, false);
                    try {
                        fs = jarFs.get();
                        Files.delete(fs.getPath(info.classTweakerPath, new String[0]));
                        Files.write(fs.getPath(info.classTweakerPath, new String[0]), info.classTweaker, new OpenOption[0]);
                    }
                    finally {
                        if (jarFs != null) {
                            jarFs.close();
                        }
                    }
                }
                mod.setPaths(Collections.singletonList(info.outputPath));
            }
        }
        catch (Throwable t) {
            try {
                if (remapper != null) {
                    remapper.finish();
                }
                for (RemapInfo info : infoMap.values()) {
                    if (info.outputPath == null) continue;
                    try {
                        Files.deleteIfExists(info.outputPath);
                    }
                    catch (IOException iOException) {
                        Log.warn(LogCategory.MOD_REMAP, "Error deleting failed output jar %s", info.outputPath, iOException);
                    }
                }
                throw new FormattedException("Failed to remap mods!", t);
            }
            catch (Throwable throwable) {
                for (RemapInfo info : infoMap.values()) {
                    try {
                        if (!info.inputIsTemp) continue;
                        Files.deleteIfExists(info.inputPath);
                    }
                    catch (IOException e) {
                        Log.warn(LogCategory.MOD_REMAP, "Error deleting temporary input jar %s", info.inputIsTemp, e);
                    }
                }
                throw throwable;
            }
        }
        for (RemapInfo info : infoMap.values()) {
            try {
                if (!info.inputIsTemp) continue;
                Files.deleteIfExists(info.inputPath);
            }
            catch (IOException e) {
                Log.warn(LogCategory.MOD_REMAP, "Error deleting temporary input jar %s", info.inputIsTemp, e);
            }
        }
    }

    private static byte[] remapClassTweaker(byte[] input, Remapper remapper, String modNs, String runtimeNs) {
        ClassTweakerWriter writer = ClassTweakerWriter.create(3);
        ClassTweakerRemapperVisitor remappingDecorator = new ClassTweakerRemapperVisitor(writer, remapper, modNs, runtimeNs);
        ClassTweakerReader reader = ClassTweakerReader.create(remappingDecorator);
        reader.read(input, modNs);
        return writer.getOutput();
    }

    private static List<Path> getRemapClasspath() throws IOException {
        String remapClasspathFile = System.getProperty("fabric.remapClasspathFile");
        if (remapClasspathFile == null) {
            throw new RuntimeException("No remapClasspathFile provided");
        }
        String content = new String(Files.readAllBytes(Paths.get(remapClasspathFile, new String[0])), StandardCharsets.UTF_8);
        return Arrays.stream(content.split(File.pathSeparator)).map(x$0 -> Paths.get(x$0, new String[0])).collect(Collectors.toList());
    }

    private static boolean requiresMixinRemap(Path inputPath, String defaultMixinRemapType) throws IOException, URISyntaxException {
        Manifest manifest = ManifestUtil.readManifest(inputPath);
        if (manifest == null) {
            return false;
        }
        Attributes mainAttributes = manifest.getMainAttributes();
        String remapType = mainAttributes.getValue(REMAP_TYPE_MANIFEST_KEY);
        if (remapType == null) {
            remapType = defaultMixinRemapType;
        }
        return REMAP_TYPE_STATIC.equalsIgnoreCase(remapType);
    }

    private static class RemapInfo {
        InputTag tag;
        Path inputPath;
        Path outputPath;
        boolean inputIsTemp;
        OutputConsumerPath outputConsumerPath;
        String classTweakerPath;
        byte[] classTweaker;

        private RemapInfo() {
        }
    }
}

