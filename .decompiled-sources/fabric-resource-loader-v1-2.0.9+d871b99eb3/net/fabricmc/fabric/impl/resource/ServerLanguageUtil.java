/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.resource;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import net.fabricmc.fabric.impl.resource.pack.ModNioPackResources;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.server.packs.PackType;

public final class ServerLanguageUtil {
    private static final String ASSETS_PREFIX = PackType.CLIENT_RESOURCES.getDirectory() + "/";

    private ServerLanguageUtil() {
    }

    public static Collection<Path> getModLanguageFiles() {
        LinkedHashSet paths = new LinkedHashSet();
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            if (mod.getMetadata().getType().equals("builtin")) continue;
            Map<PackType, Set<String>> map = ModNioPackResources.readNamespaces(mod.getRootPaths(), mod.getMetadata().getId());
            for (String ns : map.get((Object)PackType.CLIENT_RESOURCES)) {
                mod.findPath(ASSETS_PREFIX + ns + "/lang/en_us.json").filter(x$0 -> Files.isRegularFile(x$0, new LinkOption[0])).ifPresent(paths::add);
            }
        }
        return Collections.unmodifiableCollection(paths);
    }
}

