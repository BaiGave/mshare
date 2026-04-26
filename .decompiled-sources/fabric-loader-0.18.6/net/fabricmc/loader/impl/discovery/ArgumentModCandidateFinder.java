/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.discovery;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.discovery.DirectoryModCandidateFinder;
import net.fabricmc.loader.impl.discovery.ModCandidateFinder;
import net.fabricmc.loader.impl.util.LoaderUtil;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;

public class ArgumentModCandidateFinder
implements ModCandidateFinder {
    private final boolean requiresRemap;

    public ArgumentModCandidateFinder(boolean requiresRemap) {
        this.requiresRemap = requiresRemap;
    }

    @Override
    public void findCandidates(ModCandidateFinder.ModCandidateConsumer out) {
        String list = System.getProperty("fabric.addMods");
        if (list != null) {
            this.addMods(list, "system property", out);
        }
        if ((list = FabricLoaderImpl.INSTANCE.getGameProvider().getArguments().remove("fabric.addMods")) != null) {
            this.addMods(list, "argument", out);
        }
    }

    private void addMods(String list, String source, ModCandidateFinder.ModCandidateConsumer out) {
        for (String pathStr : list.split(File.pathSeparator)) {
            if (pathStr.isEmpty()) continue;
            if (pathStr.startsWith("@")) {
                Path path = Paths.get(pathStr.substring(1), new String[0]);
                if (!Files.isRegularFile(path, new LinkOption[0])) {
                    Log.warn(LogCategory.DISCOVERY, "Skipping missing/invalid %s provided mod list file %s", source, path);
                    continue;
                }
                try (BufferedReader reader = Files.newBufferedReader(path);){
                    String line;
                    String fileSource = String.format("%s file %s", source, path);
                    while ((line = reader.readLine()) != null) {
                        if ((line = line.trim()).isEmpty()) continue;
                        this.addMod(line, fileSource, out);
                    }
                    continue;
                }
                catch (IOException e) {
                    throw new RuntimeException(String.format("Error reading %s provided mod list file %s", source, path), e);
                }
            }
            this.addMod(pathStr, source, out);
        }
    }

    private void addMod(String pathStr, String source, final ModCandidateFinder.ModCandidateConsumer out) {
        final Path path = LoaderUtil.normalizePath(Paths.get(pathStr, new String[0]));
        if (!Files.exists(path, new LinkOption[0])) {
            Log.warn(LogCategory.DISCOVERY, "Skipping missing %s provided mod path %s", source, path);
        } else if (Files.isDirectory(path, new LinkOption[0])) {
            if (ArgumentModCandidateFinder.isHidden(path)) {
                Log.warn(LogCategory.DISCOVERY, "Ignoring hidden %s provided mod path %s", source, path);
                return;
            }
            if (Files.exists(path.resolve("fabric.mod.json"), new LinkOption[0])) {
                out.accept(path, this.requiresRemap);
            } else {
                try {
                    final ArrayList skipped = new ArrayList();
                    Files.walkFileTree(path, (FileVisitor<? super Path>)new SimpleFileVisitor<Path>(this){
                        final /* synthetic */ ArgumentModCandidateFinder this$0;
                        {
                            this.this$0 = this$0;
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            if (DirectoryModCandidateFinder.isValidFile(file)) {
                                out.accept(file, this.this$0.requiresRemap);
                            } else {
                                skipped.add(path.relativize(file).toString());
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                            if (ArgumentModCandidateFinder.isHidden(dir)) {
                                return FileVisitResult.SKIP_SUBTREE;
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
                    if (!skipped.isEmpty()) {
                        Log.warn(LogCategory.DISCOVERY, "Incompatible files in %s provided mod directory %s (non-jar or hidden): %s", source, path, String.join((CharSequence)", ", skipped));
                    }
                }
                catch (IOException e) {
                    Log.warn(LogCategory.DISCOVERY, "Error processing %s provided mod path %s: %s", source, path, e);
                }
            }
        } else if (!DirectoryModCandidateFinder.isValidFile(path)) {
            Log.warn(LogCategory.DISCOVERY, "Incompatible file in %s provided mod path %s (non-jar or hidden)", source, path);
        } else {
            out.accept(path, this.requiresRemap);
        }
    }

    private static boolean isHidden(Path path) {
        try {
            return path.getFileName().toString().startsWith(".") || Files.isHidden(path);
        }
        catch (IOException e) {
            Log.warn(LogCategory.DISCOVERY, "Error determining whether %s is hidden: %s", path, e);
            return true;
        }
    }
}

