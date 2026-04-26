/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.resource.pack;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.fabricmc.fabric.api.resource.v1.pack.ModPackResources;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.fabric.impl.resource.pack.FabricPack;
import net.fabricmc.fabric.impl.resource.pack.ModNioPackResources;
import net.fabricmc.fabric.impl.resource.pack.ModPackResourcesSorter;
import net.fabricmc.fabric.impl.resource.pack.ModResourcePackCreator;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.util.InclusiveRange;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.validation.DirectoryValidator;
import org.apache.commons.io.IOUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ModPackResourcesUtil {
    public static final Gson GSON = new Gson();
    private static final Logger LOGGER = LoggerFactory.getLogger(ModPackResourcesUtil.class);
    private static final String LOAD_ORDER_KEY = "fabric:resource_load_order";

    private ModPackResourcesUtil() {
    }

    public static List<ModPackResources> getModResourcePacks(FabricLoader fabricLoader, PackType type, @Nullable String subPath) {
        ModPackResourcesSorter sorter = new ModPackResourcesSorter();
        Collection<ModContainer> containers = fabricLoader.getAllMods();
        List<String> allIds = containers.stream().map(ModContainer::getMetadata).map(ModMetadata::getId).toList();
        for (ModContainer container : containers) {
            ModNioPackResources pack;
            ModMetadata metadata = container.getMetadata();
            String id = metadata.getId();
            if (metadata.getType().equals("builtin") || (pack = ModNioPackResources.create(id, container, subPath, type, PackActivationType.ALWAYS_ENABLED, true)) == null) continue;
            sorter.addPack(pack);
            CustomValue loadOrder = metadata.getCustomValue(LOAD_ORDER_KEY);
            if (loadOrder == null) continue;
            if (loadOrder.getType() == CustomValue.CvType.OBJECT) {
                CustomValue.CvObject object = loadOrder.getAsObject();
                ModPackResourcesUtil.addLoadOrdering(object, allIds, sorter, Order.BEFORE, id);
                ModPackResourcesUtil.addLoadOrdering(object, allIds, sorter, Order.AFTER, id);
                continue;
            }
            LOGGER.error("[Fabric] Resource load order should be an object");
        }
        return sorter.getPacks();
    }

    public static void addLoadOrdering(CustomValue.CvObject object, List<String> allIds, ModPackResourcesSorter sorter, Order order, String currentId) {
        ArrayList<String> modIds = new ArrayList<String>();
        CustomValue array = object.get(order.jsonKey);
        if (array == null) {
            return;
        }
        switch (array.getType()) {
            case STRING: {
                modIds.add(array.getAsString());
                break;
            }
            case ARRAY: {
                for (CustomValue id : array.getAsArray()) {
                    if (id.getType() != CustomValue.CvType.STRING) continue;
                    modIds.add(id.getAsString());
                }
                break;
            }
            default: {
                LOGGER.error("[Fabric] {} should be a string or an array", (Object)order.jsonKey);
                return;
            }
        }
        modIds.stream().filter(allIds::contains).forEach(modId -> sorter.addLoadOrdering((String)modId, currentId, order));
    }

    public static void refreshAutoEnabledPacks(List<Pack> enabledProfiles, Map<String, Pack> allProfiles) {
        LOGGER.debug("[Fabric] Starting internal pack sorting with: {}", (Object)enabledProfiles.stream().map(Pack::getId).toList());
        enabledProfiles.removeIf(profile -> ((FabricPack)((Object)profile)).fabric$isHidden());
        LOGGER.debug("[Fabric] Removed all internal packs, result: {}", (Object)enabledProfiles.stream().map(Pack::getId).toList());
        ListIterator<Pack> it = enabledProfiles.listIterator();
        LinkedHashSet<String> seen = new LinkedHashSet<String>();
        while (it.hasNext()) {
            Pack profile2 = it.next();
            seen.add(profile2.getId());
            for (Pack p : allProfiles.values()) {
                FabricPack fp = (FabricPack)((Object)p);
                if (!fp.fabric$isHidden() || !fp.fabric$parentsEnabled(seen) || !seen.add(p.getId())) continue;
                it.add(p);
                LOGGER.debug("[Fabric] cur @ {}, auto-enabled {}, currently enabled: {}", profile2.getId(), p.getId(), seen);
            }
        }
        LOGGER.debug("[Fabric] Final sorting result: {}", (Object)enabledProfiles.stream().map(Pack::getId).toList());
    }

    public static boolean containsDefault(String filename, boolean modBundled) {
        return "pack.mcmeta".equals(filename) || modBundled && "pack.png".equals(filename);
    }

    public static InputStream getDefaultIcon() throws IOException {
        Optional loaderIconPath = FabricLoader.getInstance().getModContainer("fabric-resource-loader-v1").flatMap(resourceLoaderContainer -> resourceLoaderContainer.getMetadata().getIconPath(512).flatMap(resourceLoaderContainer::findPath));
        if (loaderIconPath.isPresent()) {
            return Files.newInputStream((Path)loaderIconPath.get(), new OpenOption[0]);
        }
        return null;
    }

    public static InputStream openDefault(ModContainer container, PackType type, String filename) throws IOException {
        switch (filename) {
            case "pack.mcmeta": {
                String description = Objects.requireNonNullElse(container.getMetadata().getId(), "");
                String metadata = ModPackResourcesUtil.serializeMetadata(SharedConstants.getCurrentVersion().packVersion(type), description, type);
                return IOUtils.toInputStream(metadata, StandardCharsets.UTF_8);
            }
            case "pack.png": {
                Optional path = container.getMetadata().getIconPath(512).flatMap(container::findPath);
                if (path.isPresent()) {
                    return Files.newInputStream((Path)path.get(), new OpenOption[0]);
                }
                return ModPackResourcesUtil.getDefaultIcon();
            }
        }
        return null;
    }

    public static PackMetadataSection getMetadataPack(PackFormat packFormat, Component description) {
        return new PackMetadataSection(description, new InclusiveRange<PackFormat>(packFormat));
    }

    public static JsonObject getMetadataPackJson(PackFormat packFormat, Component description, PackType type) {
        return PackMetadataSection.codecForPackType(type).encodeStart(JsonOps.INSTANCE, ModPackResourcesUtil.getMetadataPack(packFormat, description)).getOrThrow().getAsJsonObject();
    }

    public static String serializeMetadata(PackFormat packFormat, String description, PackType type) {
        JsonObject pack = ModPackResourcesUtil.getMetadataPackJson(packFormat, Component.literal(description), type);
        JsonObject metadata = new JsonObject();
        metadata.add("pack", pack);
        return GSON.toJson(metadata);
    }

    public static Component getName(ModMetadata info) {
        if (info.getId() != null) {
            return Component.literal(info.getId());
        }
        return Component.translatable("pack.name.fabricMod", info.getId());
    }

    public static WorldDataConfiguration createDefaultDataConfiguration() {
        ModResourcePackCreator modResourcePackCreator = new ModResourcePackCreator(PackType.SERVER_DATA);
        ArrayList moddedResourcePacks = new ArrayList();
        modResourcePackCreator.loadPacks(moddedResourcePacks::add);
        ArrayList<String> enabled = new ArrayList<String>(DataPackConfig.DEFAULT.getEnabled());
        ArrayList<String> disabled = new ArrayList<String>(DataPackConfig.DEFAULT.getDisabled());
        for (Pack profile : moddedResourcePacks) {
            if (profile.getPackSource() == ModResourcePackCreator.RESOURCE_PACK_SOURCE) {
                enabled.add(profile.getId());
                continue;
            }
            PackResources pack = profile.open();
            try {
                ModNioPackResources nioPack;
                if (pack instanceof ModNioPackResources && (nioPack = (ModNioPackResources)pack).getActivationType().isEnabledByDefault()) {
                    enabled.add(profile.getId());
                    continue;
                }
                disabled.add(profile.getId());
            }
            finally {
                if (pack == null) continue;
                pack.close();
            }
        }
        return new WorldDataConfiguration(new DataPackConfig(enabled, disabled), FeatureFlags.DEFAULT_FLAGS);
    }

    public static DataPackConfig createTestServerSettings(List<String> enabled, List<String> disabled) {
        HashSet moddedProfiles = new HashSet();
        ModResourcePackCreator modResourcePackCreator = new ModResourcePackCreator(PackType.SERVER_DATA);
        modResourcePackCreator.loadPacks(profile -> moddedProfiles.add(profile.getId()));
        ArrayList<String> moveToTheEnd = new ArrayList<String>();
        Iterator<String> it = enabled.iterator();
        while (it.hasNext()) {
            String profile2 = it.next();
            if (!moddedProfiles.contains(profile2)) continue;
            moveToTheEnd.add(profile2);
            it.remove();
        }
        enabled.addAll(moveToTheEnd);
        return new DataPackConfig(enabled, disabled);
    }

    public static PackRepository createModdedRepository() {
        return new PackRepository(new ServerPacksSource(new DirectoryValidator(path -> true)), new ModResourcePackCreator(PackType.SERVER_DATA, true));
    }

    public static enum Order {
        BEFORE("before"),
        AFTER("after");

        private final String jsonKey;

        private Order(String jsonKey) {
            this.jsonKey = jsonKey;
        }
    }
}

