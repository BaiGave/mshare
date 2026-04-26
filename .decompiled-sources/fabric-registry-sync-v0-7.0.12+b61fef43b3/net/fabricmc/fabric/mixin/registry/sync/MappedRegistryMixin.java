/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.registry.sync;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import java.lang.invoke.CallSite;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.registry.FabricRegistry;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryAttributeHolder;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.event.registry.RegistryIdRemapCallback;
import net.fabricmc.fabric.impl.registry.sync.ListenableRegistry;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.fabricmc.fabric.impl.registry.sync.RemapException;
import net.fabricmc.fabric.impl.registry.sync.RemapStateImpl;
import net.fabricmc.fabric.impl.registry.sync.RemappableRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={MappedRegistry.class})
public abstract class MappedRegistryMixin<T>
implements WritableRegistry<T>,
RemappableRegistry,
ListenableRegistry<T>,
FabricRegistry {
    @Unique
    private static final Set<String> VANILLA_NAMESPACES = Set.of("minecraft", "brigadier");
    @Shadow
    @Final
    private ObjectList<Holder.Reference<T>> byId;
    @Shadow
    @Final
    private Reference2IntMap<T> toId;
    @Shadow
    @Final
    private Map<Identifier, Holder.Reference<T>> byLocation;
    @Shadow
    @Final
    private Map<ResourceKey<T>, Holder.Reference<T>> byKey;
    @Unique
    private static final Logger FABRIC_LOGGER = LoggerFactory.getLogger(MappedRegistryMixin.class);
    @Unique
    private Event<RegistryEntryAddedCallback<T>> fabric_addObjectEvent;
    @Unique
    private Event<RegistryIdRemapCallback<T>> fabric_postRemapEvent;
    @Unique
    private Object2IntMap<Identifier> fabric_prevIndexedEntries;
    @Unique
    private BiMap<Identifier, Holder.Reference<T>> fabric_prevEntries;
    @Unique
    private Map<Identifier, Identifier> aliases = new HashMap<Identifier, Identifier>();
    @Shadow
    @Final
    private ResourceKey<? extends Registry<T>> key;

    @Override
    @Shadow
    public abstract Optional<ResourceKey<T>> getResourceKey(T var1);

    @Override
    @Shadow
    public abstract @Nullable T getValue(@Nullable Identifier var1);

    @Override
    @Shadow
    public abstract ResourceKey<? extends Registry<T>> key();

    @Override
    @Shadow
    public abstract boolean containsKey(Identifier var1);

    @Shadow
    public abstract String toString();

    @Shadow
    protected abstract void validateWrite();

    @Override
    public Event<RegistryEntryAddedCallback<T>> fabric_getAddObjectEvent() {
        return this.fabric_addObjectEvent;
    }

    @Override
    public Event<RegistryIdRemapCallback<T>> fabric_getRemapEvent() {
        return this.fabric_postRemapEvent;
    }

    @Inject(method={"<init>(Lnet/minecraft/resources/ResourceKey;Lcom/mojang/serialization/Lifecycle;Z)V"}, at={@At(value="RETURN")})
    private void init(ResourceKey<?> key, Lifecycle lifecycle, boolean intrusive, CallbackInfo ci) {
        this.fabric_addObjectEvent = EventFactory.createArrayBacked(RegistryEntryAddedCallback.class, callbacks -> (rawId, id, object) -> {
            for (RegistryEntryAddedCallback callback : callbacks) {
                callback.onEntryAdded(rawId, id, object);
            }
        });
        this.fabric_addObjectEvent.register((rawId, id, object) -> {
            if (this.aliases.containsKey(id)) {
                throw new IllegalArgumentException("Tried registering %s to registry %s, but it is already an alias (for %s)".formatted(id, this.key, this.aliases.get(id)));
            }
        });
        this.fabric_postRemapEvent = EventFactory.createArrayBacked(RegistryIdRemapCallback.class, callbacks -> a -> {
            for (RegistryIdRemapCallback callback : callbacks) {
                callback.onRemap(a);
            }
        });
    }

    @Unique
    private void onChange(ResourceKey<T> resourceKey) {
        RegistryAttributeHolder holder;
        if (!(!RegistrySyncManager.postBootstrap && VANILLA_NAMESPACES.contains(resourceKey.identifier().getNamespace()) || (holder = RegistryAttributeHolder.get(this.key())).hasAttribute(RegistryAttribute.MODDED))) {
            Identifier id = this.key().identifier();
            FABRIC_LOGGER.debug("Registry {} has been marked as modded, holder {} was changed", (Object)id, (Object)resourceKey.identifier());
            RegistryAttributeHolder.get(this.key()).addAttribute(RegistryAttribute.MODDED);
        }
    }

    @Inject(method={"register"}, at={@At(value="RETURN")})
    private void set(ResourceKey<T> key, T entry, RegistrationInfo arg, CallbackInfoReturnable<Holder.Reference<T>> info) {
        info.getReturnValue().bindValue(entry);
        this.fabric_addObjectEvent.invoker().onEntryAdded(this.toId.getInt(entry), key.identifier(), entry);
        this.onChange(key);
    }

    @Override
    public void remap(Object2IntMap<Identifier> remoteIndexedEntries, RemappableRegistry.RemapMode mode) throws RemapException {
        switch (mode) {
            case AUTHORITATIVE: {
                break;
            }
            case REMOTE: {
                Object remoteId2;
                Object strings = null;
                for (Object remoteId2 : remoteIndexedEntries.keySet()) {
                    if (this.containsKey((Identifier)remoteId2)) continue;
                    if (strings == null) {
                        strings = new ArrayList();
                    }
                    strings.add((CallSite)((Object)(" - " + String.valueOf(remoteId2))));
                }
                if (strings == null) break;
                StringBuilder builder = new StringBuilder("Received ID map for " + String.valueOf(this.key()) + " contains IDs unknown to the receiver!");
                remoteId2 = strings.iterator();
                while (remoteId2.hasNext()) {
                    String s = (String)remoteId2.next();
                    builder.append('\n').append(s);
                }
                throw new RemapException(builder.toString());
            }
        }
        if (this.fabric_prevIndexedEntries == null) {
            this.fabric_prevIndexedEntries = new Object2IntOpenHashMap<Identifier>();
            this.fabric_prevEntries = HashBiMap.create(this.byLocation);
            for (Iterator o : this) {
                this.fabric_prevIndexedEntries.put(this.getKey(o), this.getId(o));
            }
        }
        Int2ObjectOpenHashMap<Identifier> oldIdMap = new Int2ObjectOpenHashMap<Identifier>();
        for (Object o : this) {
            oldIdMap.put(this.getId(o), this.getKey(o));
        }
        switch (mode) {
            case AUTHORITATIVE: {
                int maxValue = 0;
                Object2IntMap<Identifier> oldRemoteIndexedEntries = remoteIndexedEntries;
                remoteIndexedEntries = new Object2IntOpenHashMap<Identifier>();
                for (Identifier id : oldRemoteIndexedEntries.keySet()) {
                    int v = oldRemoteIndexedEntries.getInt(id);
                    remoteIndexedEntries.put(id, v);
                    if (v <= maxValue) continue;
                    maxValue = v;
                }
                for (Identifier id : this.keySet()) {
                    if (remoteIndexedEntries.containsKey(id)) continue;
                    FABRIC_LOGGER.warn("Adding " + String.valueOf(id) + " to saved/remote registry.");
                    remoteIndexedEntries.put(id, ++maxValue);
                }
                break;
            }
            case REMOTE: {
                int maxId = -1;
                for (Identifier id : this.keySet()) {
                    if (remoteIndexedEntries.containsKey(id)) continue;
                    if (maxId < 0) {
                        maxId = remoteIndexedEntries.values().intStream().max().orElseThrow(() -> new RemapException("Failed to assign new id to client only registry entry"));
                    }
                    FABRIC_LOGGER.debug("An ID for {} was not sent by the server, assuming client only registry entry and assigning a new id ({}) in {}", id.toString(), ++maxId, this.key().identifier().toString());
                    remoteIndexedEntries.put(id, maxId);
                }
                break;
            }
        }
        Int2IntOpenHashMap idMap = new Int2IntOpenHashMap();
        for (int i = 0; i < this.byId.size(); ++i) {
            Identifier id;
            Holder.Reference reference = (Holder.Reference)this.byId.get(i);
            if (reference == null) {
                throw new RemapException("Unused id " + i + " in registry " + String.valueOf(this.key().identifier()));
            }
            id = reference.key().identifier();
            if (!remoteIndexedEntries.containsKey(id)) continue;
            idMap.put(i, remoteIndexedEntries.getInt(id));
        }
        this.byId.clear();
        this.toId.clear();
        ArrayList<Identifier> orderedRemoteEntries = new ArrayList<Identifier>(remoteIndexedEntries.keySet());
        orderedRemoteEntries.sort(Comparator.comparingInt(remoteIndexedEntries::getInt));
        for (Identifier identifier : orderedRemoteEntries) {
            int id = remoteIndexedEntries.getInt(identifier);
            Holder.Reference<T> object = this.byLocation.get(identifier);
            if (object == null) {
                if (mode != RemappableRegistry.RemapMode.AUTHORITATIVE) {
                    throw new RemapException(String.valueOf(identifier) + " missing from registry, but requested!");
                }
                FABRIC_LOGGER.warn(String.valueOf(identifier) + " missing from registry, but requested!");
                continue;
            }
            this.byId.size(Math.max(this.byId.size(), id + 1));
            if (this.byId.get(id) != null) {
                throw new IllegalStateException("Raw ID already populated");
            }
            this.byId.set(id, object);
            this.toId.put(object.value(), id);
        }
        this.fabric_getRemapEvent().invoker().onRemap(new RemapStateImpl(this, oldIdMap, idMap));
    }

    @Override
    public void unmap() throws RemapException {
        if (this.fabric_prevIndexedEntries != null) {
            ArrayList<Identifier> addedIds = new ArrayList<Identifier>();
            for (Identifier identifier : this.fabric_prevEntries.keySet()) {
                if (this.byLocation.containsKey(identifier)) continue;
                if (!this.fabric_prevIndexedEntries.containsKey(identifier)) {
                    throw new IllegalStateException("id missing from previous indexed entries");
                }
                addedIds.add(identifier);
            }
            this.byLocation.clear();
            this.byKey.clear();
            this.byLocation.putAll(this.fabric_prevEntries);
            for (Map.Entry entry : this.fabric_prevEntries.entrySet()) {
                ResourceKey<T> entryKey = ResourceKey.create(this.key(), (Identifier)entry.getKey());
                this.byKey.put(entryKey, (Holder.Reference)entry.getValue());
            }
            this.remap(this.fabric_prevIndexedEntries, RemappableRegistry.RemapMode.AUTHORITATIVE);
            for (Identifier identifier : addedIds) {
                this.fabric_getAddObjectEvent().invoker().onEntryAdded(this.toId.getInt(this.byLocation.get(identifier)), identifier, this.getValue(identifier));
            }
            this.fabric_prevIndexedEntries = null;
            this.fabric_prevEntries = null;
        }
    }

    @Override
    public void addAlias(Identifier old, Identifier newId) {
        Objects.requireNonNull(old, "alias cannot be null");
        Objects.requireNonNull(newId, "aliased id cannot be null");
        if (this.aliases.containsKey(old)) {
            throw new IllegalArgumentException("Tried adding %s as an alias for %s, but it is already an alias (for %s) in registry %s".formatted(old, newId, this.aliases.get(old), this.key));
        }
        if (this.byLocation.containsKey(old)) {
            throw new IllegalArgumentException("Tried adding %s as an alias, but it is already present in registry %s".formatted(old, this.key));
        }
        if (old.equals(this.aliases.get(newId))) {
            throw new IllegalArgumentException("Making %1$s an alias of %2$s would create a cycle, as %2$s is already an alias of %1$s (registry %3$s)".formatted(old, newId, this.key));
        }
        if (!this.byLocation.containsKey(newId)) {
            FABRIC_LOGGER.warn("Adding {} as an alias for {}, but the latter doesn't exist in registry {}", old, newId, this.key);
        }
        this.validateWrite();
        Identifier deepest = this.aliases.getOrDefault(newId, newId);
        for (Map.Entry<Identifier, Identifier> entry : this.aliases.entrySet()) {
            if (!old.equals(entry.getValue())) continue;
            entry.setValue(deepest);
        }
        this.aliases.put(old, deepest);
        FABRIC_LOGGER.debug("Adding alias {} for {} in registry {}", old, newId, this.key);
    }

    @ModifyVariable(method={"get(Lnet/minecraft/resources/Identifier;)Ljava/util/Optional;", "getValue(Lnet/minecraft/resources/Identifier;)Ljava/lang/Object;", "containsKey(Lnet/minecraft/resources/Identifier;)Z"}, at=@At(value="HEAD"), argsOnly=true)
    private Identifier aliasIdentifierParameter(Identifier original) {
        return this.aliases.getOrDefault(original, original);
    }

    @ModifyVariable(method={"getValue(Lnet/minecraft/resources/ResourceKey;)Ljava/lang/Object;", "get(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Optional;", "getOrCreateHolderOrThrow", "containsKey(Lnet/minecraft/resources/ResourceKey;)Z", "registrationInfo"}, at=@At(value="HEAD"), argsOnly=true)
    private ResourceKey<T> aliasResourceKeyParameter(ResourceKey<T> original) {
        if (original == null) {
            return null;
        }
        Identifier aliased = this.aliases.get(original.identifier());
        return aliased == null ? original : ResourceKey.create(original.registryKey(), aliased);
    }
}

