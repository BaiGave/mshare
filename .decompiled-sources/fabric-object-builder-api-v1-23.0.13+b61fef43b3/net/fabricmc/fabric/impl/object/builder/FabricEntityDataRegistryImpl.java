/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.object.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryIdRemapCallback;
import net.fabricmc.fabric.mixin.object.builder.EntityDataSerializersAccessor;
import net.minecraft.core.Registry;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FabricEntityDataRegistryImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(FabricEntityDataRegistryImpl.class);
    private static final Identifier HANDLER_REGISTRY_ID = Identifier.fromNamespaceAndPath("fabric-object-builder-api-v1", "tracked_data_handler");
    private static final ResourceKey<Registry<EntityDataSerializer<?>>> HANDLER_REGISTRY_KEY = ResourceKey.createRegistryKey(HANDLER_REGISTRY_ID);
    private static final List<EntityDataSerializer<?>> VANILLA_HANDLERS = new ArrayList();
    private static @Nullable Registry<EntityDataSerializer<?>> handlerRegistry = null;
    private static final List<EntityDataSerializer<?>> EXTERNAL_MODDED_HANDLERS = new ArrayList();

    private FabricEntityDataRegistryImpl() {
    }

    public static boolean hasStoredVanillaHandlers() {
        return !VANILLA_HANDLERS.isEmpty();
    }

    public static void storeVanillaHandlers() {
        if (FabricEntityDataRegistryImpl.hasStoredVanillaHandlers()) {
            throw new IllegalStateException("Already stored vanilla handlers!");
        }
        CrudeIncrementalIntIdentityHashBiMap<EntityDataSerializer<?>> dataHandlers = EntityDataSerializersAccessor.fabric_getDataHandlers();
        for (EntityDataSerializer<?> handler : dataHandlers) {
            VANILLA_HANDLERS.add(handler);
        }
        LOGGER.debug("Stored {} vanilla handlers", (Object)VANILLA_HANDLERS.size());
    }

    private static void storeExternalHandlers() {
        CrudeIncrementalIntIdentityHashBiMap<EntityDataSerializer<?>> dataHandlers = EntityDataSerializersAccessor.fabric_getDataHandlers();
        for (EntityDataSerializer<?> handler : dataHandlers) {
            if (VANILLA_HANDLERS.contains(handler) || handlerRegistry != null && handlerRegistry.getKey(handler) != null || EXTERNAL_MODDED_HANDLERS.contains(handler)) continue;
            EXTERNAL_MODDED_HANDLERS.add(handler);
            LOGGER.warn("Entity data serializer {} is not managed by vanilla or Fabric API; it may be prone to desynchronization!", (Object)handler);
        }
    }

    private static void reorderHandlers() {
        CrudeIncrementalIntIdentityHashBiMap<EntityDataSerializer<?>> dataHandlers = EntityDataSerializersAccessor.fabric_getDataHandlers();
        LOGGER.debug("Reordering entity data serializers containing {} entries", (Object)dataHandlers.size());
        dataHandlers.clear();
        for (EntityDataSerializer<Object> handler : VANILLA_HANDLERS) {
            dataHandlers.add(handler);
        }
        if (handlerRegistry != null) {
            for (EntityDataSerializer<Object> handler : handlerRegistry) {
                dataHandlers.add(handler);
            }
        }
        for (EntityDataSerializer<Object> handler : EXTERNAL_MODDED_HANDLERS) {
            dataHandlers.add(handler);
        }
        LOGGER.debug("Finished reordering entity data serializer containing {} entries", (Object)dataHandlers.size());
    }

    public static void register(Identifier id, EntityDataSerializer<?> handler) {
        Objects.requireNonNull(id, "Entity data serializer ID cannot be null!");
        Objects.requireNonNull(handler, "Entity data serializer cannot be null!");
        FabricEntityDataRegistryImpl.storeExternalHandlers();
        if (VANILLA_HANDLERS.contains(handler) || EXTERNAL_MODDED_HANDLERS.contains(handler)) {
            throw new IllegalArgumentException("Cannot register entity data serializer previously added via EntityDataSerializers.registerSerializer");
        }
        if (handlerRegistry == null) {
            handlerRegistry = FabricRegistryBuilder.create(HANDLER_REGISTRY_KEY).attribute(RegistryAttribute.SYNCED).buildAndRegister();
            RegistryIdRemapCallback.event(handlerRegistry).register(state -> {
                FabricEntityDataRegistryImpl.storeExternalHandlers();
                FabricEntityDataRegistryImpl.reorderHandlers();
            });
        }
        Registry.register(handlerRegistry, id, handler);
        FabricEntityDataRegistryImpl.reorderHandlers();
    }

    public static @Nullable EntityDataSerializer<?> get(Identifier id) {
        Objects.requireNonNull(id, "Entity data serializer ID cannot be null!");
        if (handlerRegistry == null) {
            return null;
        }
        return handlerRegistry.getValue(id);
    }

    public static @Nullable Identifier getId(EntityDataSerializer<?> handler) {
        Objects.requireNonNull(handler, "Entity data serializer cannot be null!");
        if (handlerRegistry == null) {
            return null;
        }
        return handlerRegistry.getKey(handler);
    }
}

