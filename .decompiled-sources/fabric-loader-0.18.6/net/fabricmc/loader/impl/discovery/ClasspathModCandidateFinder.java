/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.discovery;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import net.fabricmc.loader.impl.discovery.ModCandidateFinder;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.fabricmc.loader.impl.util.LoaderUtil;
import net.fabricmc.loader.impl.util.UrlConversionException;
import net.fabricmc.loader.impl.util.UrlUtil;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;

public class ClasspathModCandidateFinder
implements ModCandidateFinder {
    @Override
    public void findCandidates(ModCandidateFinder.ModCandidateConsumer out) {
        if (FabricLauncherBase.getLauncher().isDevelopment()) {
            Map<Path, List<Path>> pathGroups = ClasspathModCandidateFinder.getPathGroups();
            try {
                Enumeration<URL> mods = FabricLauncherBase.getLauncher().getTargetClassLoader().getResources("fabric.mod.json");
                while (mods.hasMoreElements()) {
                    URL url = mods.nextElement();
                    try {
                        Path path = LoaderUtil.normalizeExistingPath(UrlUtil.getCodeSource(url, "fabric.mod.json"));
                        List<Path> paths = pathGroups.get(path);
                        if (paths == null) {
                            out.accept(path, false);
                            continue;
                        }
                        out.accept(paths, false);
                    }
                    catch (UrlConversionException e) {
                        Log.debug(LogCategory.DISCOVERY, "Error determining location for fabric.mod.json from %s", url, e);
                    }
                }
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            out.accept(UrlUtil.LOADER_CODE_SOURCE, false);
        }
        catch (Throwable t) {
            Log.debug(LogCategory.DISCOVERY, "Could not retrieve launcher code source!", t);
        }
    }

    private static Map<Path, List<Path>> getPathGroups() {
        String prop = System.getProperty("fabric.classPathGroups");
        if (prop == null) {
            return Collections.emptyMap();
        }
        HashSet<Path> cp = new HashSet<Path>(FabricLauncherBase.getLauncher().getClassPath());
        HashMap<Path, List<Path>> ret = new HashMap<Path, List<Path>>();
        for (String group : prop.split(File.pathSeparator + File.pathSeparator)) {
            LinkedHashSet<Path> paths = new LinkedHashSet<Path>();
            for (String path : group.split(File.pathSeparator)) {
                if (path.isEmpty()) continue;
                Path resolvedPath = Paths.get(path, new String[0]);
                if (!Files.exists(resolvedPath, new LinkOption[0])) {
                    Log.debug(LogCategory.DISCOVERY, "Skipping missing class path group entry %s", path);
                    continue;
                }
                if (!cp.contains(resolvedPath = LoaderUtil.normalizeExistingPath(resolvedPath))) continue;
                paths.add(resolvedPath);
            }
            if (paths.size() < 2) {
                Log.debug(LogCategory.DISCOVERY, "Skipping class path group with no effect: %s", group);
                continue;
            }
            ArrayList pathList = new ArrayList(paths);
            for (Path path : pathList) {
                ret.put(path, pathList);
            }
        }
        return ret;
    }
}

