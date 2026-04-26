/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.registry.sync;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.runtime.SwitchBootstraps;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryAttributeHolder;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.impl.networking.server.ServerNetworkingImpl;
import net.fabricmc.fabric.impl.registry.sync.RemappableRegistry;
import net.fabricmc.fabric.impl.registry.sync.packet.RegistrySyncPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.players.NameAndId;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RegistrySyncManager {
    public static final boolean DEBUG = Boolean.getBoolean("fabric.registry.debug");
    private static final Logger LOGGER = LoggerFactory.getLogger("FabricRegistrySync");
    private static final boolean DEBUG_WRITE_REGISTRY_DATA = Boolean.getBoolean("fabric.registry.debug.writeContentsAsCsv");
    public static boolean postBootstrap = false;

    private RegistrySyncManager() {
    }

    public static void configureClient(ServerConfigurationPacketListenerImpl handler, MinecraftServer server) {
        if (!DEBUG && server.isSingleplayerOwner(new NameAndId(handler.getOwner()))) {
            return;
        }
        Map<Identifier, Object2IntMap<Identifier>> map = RegistrySyncManager.createAndPopulateRegistryMap();
        if (map == null) {
            return;
        }
        if (!ServerConfigurationNetworking.canSend(handler, RegistrySyncPayload.ID)) {
            if (RegistrySyncManager.areAllRegistriesOptional(map)) {
                return;
            }
            Component message = RegistrySyncManager.getIncompatibleClientComponent(ServerNetworkingImpl.getAddon(handler).getClientBrand(), map);
            handler.disconnect(message);
            return;
        }
        handler.addTask(new SyncConfigurationTask(handler, map));
    }

    private static Component getIncompatibleClientComponent(@Nullable String brand, Map<Identifier, Object2IntMap<Identifier>> map) {
        String string = brand;
        int n = 0;
        String brandText = switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{"fabric"}, (String)string, n)) {
            case 0 -> "Fabric API";
            default -> "Fabric Loader and Fabric API";
        };
        int toDisplay = 4;
        List<String> namespaces = map.values().stream().map(Object2IntMap::keySet).flatMap(Collection::stream).map(Identifier::getNamespace).filter(s -> !s.equals("minecraft")).distinct().sorted().toList();
        MutableComponent component = Component.literal("The following registry entry namespaces may be related:\n\n");
        for (int i = 0; i < Math.min(namespaces.size(), 4); ++i) {
            component = component.append(Component.literal(namespaces.get(i)).withStyle(ChatFormatting.YELLOW));
            component = component.append(CommonComponents.NEW_LINE);
        }
        if (namespaces.size() > 4) {
            component = component.append(Component.literal("And %d more...".formatted(namespaces.size() - 4)));
        }
        return Component.literal("This server requires ").append(Component.literal(brandText).withStyle(ChatFormatting.GREEN)).append(" installed on your client!").append(CommonComponents.NEW_LINE).append(component).append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE).append(Component.literal("Contact the server's administrator for more information!").withStyle(ChatFormatting.GOLD));
    }

    private static boolean areAllRegistriesOptional(Map<Identifier, Object2IntMap<Identifier>> map) {
        return map.keySet().stream().map(BuiltInRegistries.REGISTRY::getValue).filter(Objects::nonNull).map(RegistryAttributeHolder::get).allMatch(attributes -> attributes.hasAttribute(RegistryAttribute.OPTIONAL));
    }

    public static @Nullable Map<Identifier, Object2IntMap<Identifier>> createAndPopulateRegistryMap() {
        LinkedHashMap<Identifier, Object2IntMap<Identifier>> map = new LinkedHashMap<Identifier, Object2IntMap<Identifier>>();
        for (Identifier registryId : BuiltInRegistries.REGISTRY.keySet()) {
            RegistryAttributeHolder attributeHolder;
            Registry<?> registry = BuiltInRegistries.REGISTRY.getValue(registryId);
            if (DEBUG_WRITE_REGISTRY_DATA) {
                File location = new File(".fabric" + File.separatorChar + "debug" + File.separatorChar + "registry");
                boolean c = true;
                if (!location.exists() && !location.mkdirs()) {
                    LOGGER.warn("[fabric-registry-sync debug] Could not create " + location.getAbsolutePath() + " directory!");
                    c = false;
                }
                if (c && registry != null) {
                    File file = new File(location, registryId.toString().replace(':', '.').replace('/', '.') + ".csv");
                    try (FileOutputStream stream = new FileOutputStream(file);){
                        StringBuilder builder = new StringBuilder("Raw ID,String ID,Class Type\n");
                        for (Object o : registry) {
                            String classType = o == null ? "null" : o.getClass().getName();
                            Identifier id = registry.getKey(o);
                            if (id == null) continue;
                            int rawId = registry.getId(o);
                            String stringId = id.toString();
                            builder.append("\"").append(rawId).append("\",\"").append(stringId).append("\",\"").append(classType).append("\"\n");
                        }
                        stream.write(builder.toString().getBytes(StandardCharsets.UTF_8));
                    }
                    catch (IOException e) {
                        LOGGER.warn("[fabric-registry-sync debug] Could not write to " + file.getAbsolutePath() + "!", e);
                    }
                }
            }
            if (!(attributeHolder = RegistryAttributeHolder.get(registry.key())).hasAttribute(RegistryAttribute.SYNCED)) {
                LOGGER.debug("Not syncing registry: {}", (Object)registryId);
                continue;
            }
            if (!attributeHolder.hasAttribute(RegistryAttribute.MODDED)) {
                LOGGER.debug("Skipping un-modded registry: " + String.valueOf(registryId));
                continue;
            }
            LOGGER.debug("Syncing registry: " + String.valueOf(registryId));
            if (!(registry instanceof RemappableRegistry)) continue;
            Object2IntLinkedOpenHashMap<Identifier> idMap = new Object2IntLinkedOpenHashMap<Identifier>();
            IntOpenHashSet rawIdsFound = DEBUG ? new IntOpenHashSet() : null;
            for (Object o : registry) {
                Identifier id = registry.getKey(o);
                if (id == null) continue;
                int rawId = registry.getId(o);
                if (DEBUG) {
                    if (registry.getValue(id) != o) {
                        LOGGER.error("[fabric-registry-sync] Inconsistency detected in " + String.valueOf(registryId) + ": object " + String.valueOf(o) + " -> string ID " + String.valueOf(id) + " -> object " + String.valueOf(registry.getValue(id)) + "!");
                    }
                    if (registry.byId(rawId) != o) {
                        LOGGER.error("[fabric-registry-sync] Inconsistency detected in " + String.valueOf(registryId) + ": object " + String.valueOf(o) + " -> integer ID " + rawId + " -> object " + String.valueOf(registry.byId(rawId)) + "!");
                    }
                    if (!rawIdsFound.add(rawId)) {
                        LOGGER.error("[fabric-registry-sync] Inconsistency detected in " + String.valueOf(registryId) + ": multiple objects hold the raw ID " + rawId + " (this one is " + String.valueOf(id) + ")");
                    }
                }
                idMap.put(id, rawId);
            }
            map.put(registryId, idMap);
        }
        if (map.isEmpty()) {
            return null;
        }
        return map;
    }

    public static void bootstrapRegistries() {
        postBootstrap = true;
    }

    public record SyncConfigurationTask(ServerConfigurationPacketListenerImpl handler, Map<Identifier, Object2IntMap<Identifier>> map) implements ConfigurationTask
    {
        public static final ConfigurationTask.Type KEY = new ConfigurationTask.Type("fabric:registry/sync");

        @Override
        public void start(Consumer<Packet<?>> sender) {
            sender.accept(ServerConfigurationNetworking.createClientboundPacket(new RegistrySyncPayload(this.map)));
        }

        @Override
        public ConfigurationTask.Type type() {
            return KEY;
        }
    }
}

