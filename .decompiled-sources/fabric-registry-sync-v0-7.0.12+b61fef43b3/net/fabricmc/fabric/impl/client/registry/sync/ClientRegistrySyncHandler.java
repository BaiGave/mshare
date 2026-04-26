/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.registry.sync;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.fabricmc.fabric.impl.registry.sync.RemapException;
import net.fabricmc.fabric.impl.registry.sync.RemappableRegistry;
import net.fabricmc.fabric.impl.registry.sync.SyncCompletePayload;
import net.fabricmc.fabric.impl.registry.sync.packet.RegistrySyncPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ClientRegistrySyncHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRegistrySyncHandler.class);

    private ClientRegistrySyncHandler() {
    }

    public static void receivePacket(RegistrySyncPayload payload, ClientConfigurationNetworking.Context context) {
        if (!RegistrySyncManager.DEBUG && context.client().isLocalServer()) {
            context.responseSender().sendPacket(SyncCompletePayload.INSTANCE);
            return;
        }
        context.client().execute(() -> {
            try {
                ClientRegistrySyncHandler.apply(payload);
                context.responseSender().sendPacket(SyncCompletePayload.INSTANCE);
            }
            catch (Throwable e) {
                LOGGER.error("Registry remapping failed!", e);
                context.responseSender().disconnect(ClientRegistrySyncHandler.getComponent(e));
                return;
            }
        });
    }

    @VisibleForTesting
    public static void apply(RegistrySyncPayload data) throws RemapException {
        ClientRegistrySyncHandler.checkRemoteRemap(data);
        for (Map.Entry<Identifier, Object2IntMap<Identifier>> entry : data.registryMap().entrySet()) {
            Identifier registryId = entry.getKey();
            Registry<?> registry = BuiltInRegistries.REGISTRY.getValue(registryId);
            if (registry == null && ClientRegistrySyncHandler.isRegistryOptional(registryId, data)) {
                LOGGER.info("Received registry data for unknown optional registry: {}", (Object)registryId);
                continue;
            }
            if (!(registry instanceof RemappableRegistry)) {
                throw new RemapException("Registry " + String.valueOf(registryId) + " is not remappable");
            }
            RemappableRegistry remappableRegistry = (RemappableRegistry)((Object)registry);
            remappableRegistry.remap(entry.getValue(), RemappableRegistry.RemapMode.REMOTE);
        }
    }

    @VisibleForTesting
    public static void checkRemoteRemap(RegistrySyncPayload data) throws RemapException {
        Map<Identifier, Object2IntMap<Identifier>> map = data.registryMap();
        ArrayList<Identifier> missingRegistries = new ArrayList<Identifier>();
        HashMap<Identifier, List<Identifier>> missingEntries = new HashMap<Identifier, List<Identifier>>();
        for (Identifier identifier : map.keySet()) {
            Object2IntMap<Identifier> remoteRegistry = map.get(identifier);
            Registry<?> registry = BuiltInRegistries.REGISTRY.getValue(identifier);
            if (registry == null) {
                if (ClientRegistrySyncHandler.isRegistryOptional(identifier, data)) continue;
                missingRegistries.add(identifier);
                continue;
            }
            for (Identifier remoteId : remoteRegistry.keySet()) {
                if (registry.containsKey(remoteId)) continue;
                missingEntries.computeIfAbsent(identifier, i -> new ArrayList()).add(remoteId);
            }
        }
        if (missingRegistries.isEmpty() && missingEntries.isEmpty()) {
            return;
        }
        if (!missingRegistries.isEmpty()) {
            LOGGER.error("Received unknown remote registries from server");
            for (Identifier identifier : missingRegistries) {
                LOGGER.error("Received unknown remote registry ({}) from server", (Object)identifier);
            }
        }
        if (!missingEntries.isEmpty()) {
            LOGGER.error("Received unknown remote registry entries from server");
            for (Map.Entry entry : missingEntries.entrySet()) {
                for (Identifier identifier : (List)entry.getValue()) {
                    LOGGER.error("Registry entry ({}) is missing from local registry ({})", (Object)identifier, entry.getKey());
                }
            }
        }
        if (!missingRegistries.isEmpty()) {
            throw new RemapException(ClientRegistrySyncHandler.missingRegistriesError(missingRegistries));
        }
        throw new RemapException(ClientRegistrySyncHandler.missingEntriesError(missingEntries));
    }

    private static Component missingRegistriesError(List<Identifier> missingRegistries) {
        MutableComponent component = Component.empty();
        int count = missingRegistries.size();
        component = count == 1 ? component.append(Component.translatable("fabric-registry-sync-v0.unknown-registry.title.singular")) : component.append(Component.translatable("fabric-registry-sync-v0.unknown-registry.title.plural", count));
        component = component.append(Component.translatable("fabric-registry-sync-v0.unknown-registry.subtitle.1").withStyle(ChatFormatting.GREEN));
        component = component.append(Component.translatable("fabric-registry-sync-v0.unknown-registry.subtitle.2"));
        int toDisplay = 4;
        for (int i = 0; i < Math.min(missingRegistries.size(), 4); ++i) {
            component = component.append(Component.literal(missingRegistries.get(i).toString()).withStyle(ChatFormatting.YELLOW));
            component = component.append(CommonComponents.NEW_LINE);
        }
        if (missingRegistries.size() > 4) {
            component = component.append(Component.translatable("fabric-registry-sync-v0.unknown-registry.footer", missingRegistries.size() - 4));
        }
        return component;
    }

    private static Component missingEntriesError(Map<Identifier, List<Identifier>> missingEntries) {
        MutableComponent component = Component.empty();
        int count = missingEntries.values().stream().mapToInt(List::size).sum();
        component = count == 1 ? component.append(Component.translatable("fabric-registry-sync-v0.unknown-remote.title.singular")) : component.append(Component.translatable("fabric-registry-sync-v0.unknown-remote.title.plural", count));
        component = component.append(Component.translatable("fabric-registry-sync-v0.unknown-remote.subtitle.1").withStyle(ChatFormatting.GREEN));
        component = component.append(Component.translatable("fabric-registry-sync-v0.unknown-remote.subtitle.2"));
        int toDisplay = 4;
        List<String> namespaces = missingEntries.values().stream().flatMap(Collection::stream).map(Identifier::getNamespace).distinct().sorted().toList();
        for (int i = 0; i < Math.min(namespaces.size(), 4); ++i) {
            component = component.append(Component.literal(namespaces.get(i)).withStyle(ChatFormatting.YELLOW));
            component = component.append(CommonComponents.NEW_LINE);
        }
        if (namespaces.size() > 4) {
            component = component.append(Component.translatable("fabric-registry-sync-v0.unknown-remote.footer", namespaces.size() - 4));
        }
        return component;
    }

    private static boolean isRegistryOptional(Identifier registryId, RegistrySyncPayload data) {
        EnumSet<RegistryAttribute> registryAttributes = data.registryAttributes().get(registryId);
        return registryAttributes.contains((Object)RegistryAttribute.OPTIONAL);
    }

    private static Component getComponent(Throwable e) {
        if (e instanceof RemapException) {
            RemapException remapException = (RemapException)e;
            Component component = remapException.getComponent();
            if (component != null) {
                return component;
            }
        } else if (e instanceof CompletionException) {
            CompletionException completionException = (CompletionException)e;
            return ClientRegistrySyncHandler.getComponent(completionException.getCause());
        }
        return Component.literal("Registry remapping failed: " + e.getMessage());
    }
}

