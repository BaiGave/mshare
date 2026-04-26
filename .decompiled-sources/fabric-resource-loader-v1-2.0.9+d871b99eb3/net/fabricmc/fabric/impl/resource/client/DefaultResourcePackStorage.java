/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.resource.client;

import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.impl.resource.pack.FabricPack;
import net.fabricmc.fabric.impl.resource.pack.ModNioPackResources;
import net.fabricmc.fabric.impl.resource.pack.ModResourcePackCreator;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;

public final class DefaultResourcePackStorage {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Path DATA_DIR = FabricLoader.getInstance().getGameDir().resolve("data");
    private static final Path TRACKER_FILE_PATH = DATA_DIR.resolve("fabric_default_resource_packs.json");
    private static final Path OLD_TRACKER_FILE_PATH = DATA_DIR.resolve("fabricDefaultResourcePacks.dat");
    private static final Codec<Set<String>> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.STRING.listOf().fieldOf("values")).forGetter(List::copyOf)).apply((Applicative<Set, ?>)instance, Set::copyOf));

    public static List<String> process(Collection<String> originalResourcePacks) {
        if (Files.notExists(DATA_DIR, new LinkOption[0])) {
            try {
                Files.createDirectories(DATA_DIR, new FileAttribute[0]);
            }
            catch (IOException e) {
                LOGGER.warn("[Fabric Resource Loader] Could not create data directory: {}", (Object)DATA_DIR.toAbsolutePath());
            }
        }
        HashSet<String> trackedPacks = new HashSet<String>(DefaultResourcePackStorage.read());
        HashSet<String> removedPacks = new HashSet<String>(trackedPacks);
        LinkedHashSet<String> resourcePacks = new LinkedHashSet<String>(originalResourcePacks);
        ArrayList profiles = new ArrayList();
        ModResourcePackCreator.CLIENT_RESOURCE_PACK_PROVIDER.loadPacks(profiles::add);
        for (Pack profile : profiles) {
            if (((FabricPack)((Object)profile)).fabric$isHidden()) continue;
            PackResources pack = profile.open();
            try {
                ModNioPackResources builtinPack;
                if (!(pack instanceof ModNioPackResources) || !(builtinPack = (ModNioPackResources)pack).getActivationType().isEnabledByDefault()) continue;
                if (trackedPacks.add(builtinPack.packId())) {
                    resourcePacks.add(profile.getId());
                    continue;
                }
                removedPacks.remove(builtinPack.packId());
            }
            finally {
                if (pack == null) continue;
                pack.close();
            }
        }
        trackedPacks.removeAll(removedPacks);
        DefaultResourcePackStorage.write(trackedPacks);
        return new ArrayList<String>(resourcePacks);
    }

    /*
     * Enabled aggressive exception aggregation
     */
    private static @Unmodifiable Set<String> read() {
        if (Files.exists(TRACKER_FILE_PATH, new LinkOption[0])) {
            try (BufferedReader fileReader = Files.newBufferedReader(TRACKER_FILE_PATH);){
                Set set;
                try (JsonReader reader = new JsonReader(fileReader);){
                    set = (Set)CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).getOrThrow();
                }
                return set;
            }
            catch (Exception e) {
                LOGGER.warn("[Fabric Resource Loader] Could not read {}", (Object)TRACKER_FILE_PATH.toAbsolutePath(), (Object)e);
            }
        }
        if (Files.exists(OLD_TRACKER_FILE_PATH, new LinkOption[0])) {
            try {
                CompoundTag data = NbtIo.readCompressed(OLD_TRACKER_FILE_PATH, NbtAccounter.unlimitedHeap());
                return CODEC.parse(NbtOps.INSTANCE, data).result().orElse(Set.of());
            }
            catch (Exception e) {
                LOGGER.warn("[Fabric Resource Loader] Could not read {}", (Object)OLD_TRACKER_FILE_PATH.toAbsolutePath(), (Object)e);
            }
        }
        return Set.of();
    }

    private static void write(Set<String> values) {
        try {
            Files.writeString(TRACKER_FILE_PATH, (CharSequence)CODEC.encodeStart(JsonOps.INSTANCE, values).getOrThrow().toString(), new OpenOption[0]);
        }
        catch (Exception e) {
            LOGGER.warn("[Fabric Resource Loader] Could not read {}", (Object)TRACKER_FILE_PATH.toAbsolutePath(), (Object)e);
        }
    }
}

