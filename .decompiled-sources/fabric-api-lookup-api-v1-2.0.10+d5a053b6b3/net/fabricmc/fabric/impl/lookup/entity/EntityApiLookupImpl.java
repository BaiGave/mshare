/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.lookup.entity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import net.fabricmc.fabric.api.lookup.v1.custom.ApiLookupMap;
import net.fabricmc.fabric.api.lookup.v1.custom.ApiProviderMap;
import net.fabricmc.fabric.api.lookup.v1.entity.EntityApiLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityApiLookupImpl<A, C>
implements EntityApiLookup<A, C> {
    private static final Logger LOGGER = LoggerFactory.getLogger("fabric-api-lookup-api-v1/entity");
    private static final ApiLookupMap<EntityApiLookup<?, ?>> LOOKUPS = ApiLookupMap.create(EntityApiLookupImpl::new);
    private static final Map<Class<?>, Set<EntityType<?>>> REGISTERED_SELVES = new HashMap();
    private static boolean checkEntityLookup = true;
    private final Identifier identifier;
    private final Class<A> apiClass;
    private final Class<C> contextClass;
    private final ApiProviderMap<EntityType<?>, EntityApiLookup.EntityApiProvider<A, C>> providerMap = ApiProviderMap.create();
    private final List<EntityApiLookup.EntityApiProvider<A, C>> fallbackProviders = new CopyOnWriteArrayList<EntityApiLookup.EntityApiProvider<A, C>>();

    private EntityApiLookupImpl(Identifier identifier, Class<A> apiClass, Class<C> contextClass) {
        this.identifier = identifier;
        this.apiClass = apiClass;
        this.contextClass = contextClass;
    }

    public static <A, C> EntityApiLookup<A, C> get(Identifier lookupId, Class<A> apiClass, Class<C> contextClass) {
        return LOOKUPS.getLookup(lookupId, apiClass, contextClass);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void checkSelfImplementingTypes(MinecraftServer server) {
        if (checkEntityLookup) {
            checkEntityLookup = false;
            Map<Class<?>, Set<EntityType<?>>> map = REGISTERED_SELVES;
            synchronized (map) {
                REGISTERED_SELVES.forEach((apiClass, entityTypes) -> {
                    for (EntityType entityType : entityTypes) {
                        Object entity = entityType.create(server.overworld(), EntitySpawnReason.LOAD);
                        if (entity == null) {
                            String errorMessage = String.format("Failed to register self-implementing entities for API class %s. Can not create entity of type %s.", apiClass.getCanonicalName(), BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
                            throw new NullPointerException(errorMessage);
                        }
                        if (apiClass.isInstance(entity)) continue;
                        String errorMessage = String.format("Failed to register self-implementing entities. API class %s is not assignable from entity class %s.", apiClass.getCanonicalName(), entity.getClass().getCanonicalName());
                        throw new IllegalArgumentException(errorMessage);
                    }
                });
            }
        }
    }

    @Override
    public @Nullable A find(Entity entity, C context) {
        Objects.requireNonNull(entity, "Entity may not be null.");
        if (EntitySelector.ENTITY_STILL_ALIVE.test(entity)) {
            A instance;
            EntityApiLookup.EntityApiProvider<A, C> provider = this.providerMap.get(entity.getType());
            if (provider != null && (instance = provider.find(entity, context)) != null) {
                return instance;
            }
            for (EntityApiLookup.EntityApiProvider<A, C> fallback : this.fallbackProviders) {
                A instance2 = fallback.find(entity, context);
                if (instance2 == null) continue;
                return instance2;
            }
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void registerSelf(EntityType<?> ... entityTypes) {
        Map<Class<?>, Set<EntityType<?>>> map = REGISTERED_SELVES;
        synchronized (map) {
            REGISTERED_SELVES.computeIfAbsent(this.apiClass, c -> new LinkedHashSet()).addAll(Arrays.asList(entityTypes));
        }
        this.registerForTypes((entity, context) -> entity, entityTypes);
    }

    @Override
    public void registerForTypes(EntityApiLookup.EntityApiProvider<A, C> provider, EntityType<?> ... entityTypes) {
        Objects.requireNonNull(provider, "EntityApiProvider may not be null.");
        if (entityTypes.length == 0) {
            throw new IllegalArgumentException("Must register at least one EntityType instance with an EntityApiProvider.");
        }
        for (EntityType<?> entityType : entityTypes) {
            if (this.providerMap.putIfAbsent(entityType, provider) == null) continue;
            LOGGER.warn("Encountered duplicate API provider registration for entity type: " + String.valueOf(BuiltInRegistries.ENTITY_TYPE.getKey(entityType)));
        }
    }

    @Override
    public void registerFallback(EntityApiLookup.EntityApiProvider<A, C> fallbackProvider) {
        Objects.requireNonNull(fallbackProvider, "EntityApiProvider may not be null.");
        this.fallbackProviders.add(fallbackProvider);
    }

    @Override
    public Identifier getId() {
        return this.identifier;
    }

    @Override
    public Class<A> apiClass() {
        return this.apiClass;
    }

    @Override
    public Class<C> contextClass() {
        return this.contextClass;
    }

    @Override
    public @Nullable EntityApiLookup.EntityApiProvider<A, C> getProvider(EntityType<?> entityType) {
        return this.providerMap.get(entityType);
    }
}

