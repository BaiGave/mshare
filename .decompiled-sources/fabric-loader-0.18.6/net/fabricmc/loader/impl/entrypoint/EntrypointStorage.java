/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.entrypoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.loader.api.EntrypointException;
import net.fabricmc.loader.api.LanguageAdapterException;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.impl.ModContainerImpl;
import net.fabricmc.loader.impl.entrypoint.EntrypointContainerImpl;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.fabricmc.loader.impl.metadata.EntrypointMetadata;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.fabricmc.loader.language.LanguageAdapter;

public final class EntrypointStorage {
    private final Map<String, List<Entry>> entryMap = new HashMap<String, List<Entry>>();

    private List<Entry> getOrCreateEntries(String key) {
        return this.entryMap.computeIfAbsent(key, z -> new ArrayList());
    }

    public void addDeprecated(ModContainerImpl modContainer, String adapter, String value) throws ClassNotFoundException, LanguageAdapterException {
        Log.debug(LogCategory.ENTRYPOINT, "Registering 0.3.x old-style initializer %s for mod %s", value, modContainer.getMetadata().getId());
        OldEntry oe = new OldEntry(modContainer, adapter, value);
        this.getOrCreateEntries("main").add(oe);
        this.getOrCreateEntries("client").add(oe);
        this.getOrCreateEntries("server").add(oe);
    }

    public void add(ModContainerImpl modContainer, String key, EntrypointMetadata metadata, Map<String, net.fabricmc.loader.api.LanguageAdapter> adapterMap) throws Exception {
        if (!adapterMap.containsKey(metadata.getAdapter())) {
            throw new Exception("Could not find adapter '" + metadata.getAdapter() + "' (mod " + modContainer.getMetadata().getId() + "!)");
        }
        Log.debug(LogCategory.ENTRYPOINT, "Registering new-style initializer %s for mod %s (key %s)", metadata.getValue(), modContainer.getMetadata().getId(), key);
        this.getOrCreateEntries(key).add(new NewEntry(modContainer, adapterMap.get(metadata.getAdapter()), metadata.getValue()));
    }

    public boolean hasEntrypoints(String key) {
        return this.entryMap.containsKey(key);
    }

    public <T> List<T> getEntrypoints(String key, Class<T> type) {
        List<Entry> entries = this.entryMap.get(key);
        if (entries == null) {
            return Collections.emptyList();
        }
        Throwable exception = null;
        ArrayList<T> results = new ArrayList<T>(entries.size());
        for (Entry entry : entries) {
            try {
                T result = entry.getOrCreate(type);
                if (result == null) continue;
                results.add(result);
            }
            catch (Throwable t) {
                if (exception == null) {
                    exception = new EntrypointException(key, entry.getModContainer().getMetadata().getId(), t);
                    continue;
                }
                exception.addSuppressed(t);
            }
        }
        if (exception != null) {
            throw exception;
        }
        return results;
    }

    public <T> List<EntrypointContainer<T>> getEntrypointContainers(String key, Class<T> type) {
        List<Entry> entries = this.entryMap.get(key);
        if (entries == null) {
            return Collections.emptyList();
        }
        ArrayList<EntrypointContainer<T>> results = new ArrayList<EntrypointContainer<T>>(entries.size());
        Throwable exc = null;
        for (Entry entry : entries) {
            EntrypointContainerImpl<T> container;
            block7: {
                if (entry.isOptional()) {
                    try {
                        T instance = entry.getOrCreate(type);
                        if (instance == null) continue;
                        container = new EntrypointContainerImpl<T>(entry, instance);
                        break block7;
                    }
                    catch (Throwable t) {
                        if (exc == null) {
                            exc = new EntrypointException(key, entry.getModContainer().getMetadata().getId(), t);
                            continue;
                        }
                        exc.addSuppressed(t);
                        continue;
                    }
                }
                container = new EntrypointContainerImpl<T>(key, type, entry);
            }
            results.add(container);
        }
        if (exc != null) {
            throw exc;
        }
        return results;
    }

    static <E extends Throwable> RuntimeException sneakyThrows(Throwable ex) throws E {
        throw ex;
    }

    private static class OldEntry
    implements Entry {
        private static final LanguageAdapter.Options options = LanguageAdapter.Options.Builder.create().missingSuperclassBehaviour(LanguageAdapter.MissingSuperclassBehavior.RETURN_NULL).build();
        private final ModContainerImpl mod;
        private final String languageAdapter;
        private final String value;
        private Object object;

        private OldEntry(ModContainerImpl mod, String languageAdapter, String value) {
            this.mod = mod;
            this.languageAdapter = languageAdapter;
            this.value = value;
        }

        public String toString() {
            return this.mod.getInfo().getId() + "->" + this.value;
        }

        @Override
        public synchronized <T> T getOrCreate(Class<T> type) throws Exception {
            if (this.object == null) {
                LanguageAdapter adapter = (LanguageAdapter)Class.forName(this.languageAdapter, true, FabricLauncherBase.getLauncher().getTargetClassLoader()).getConstructor(new Class[0]).newInstance(new Object[0]);
                this.object = adapter.createInstance(this.value, options);
            }
            if (this.object == null || !type.isAssignableFrom(this.object.getClass())) {
                return null;
            }
            return (T)this.object;
        }

        @Override
        public boolean isOptional() {
            return true;
        }

        @Override
        public ModContainerImpl getModContainer() {
            return this.mod;
        }

        @Override
        public String getDefinition() {
            return this.value;
        }
    }

    private static final class NewEntry
    implements Entry {
        private final ModContainerImpl mod;
        private final net.fabricmc.loader.api.LanguageAdapter adapter;
        private final String value;
        private final Map<Class<?>, Object> instanceMap;

        NewEntry(ModContainerImpl mod, net.fabricmc.loader.api.LanguageAdapter adapter, String value) {
            this.mod = mod;
            this.adapter = adapter;
            this.value = value;
            this.instanceMap = new IdentityHashMap(1);
        }

        public String toString() {
            return this.mod.getMetadata().getId() + "->(0.3.x)" + this.value;
        }

        @Override
        public synchronized <T> T getOrCreate(Class<T> type) throws Exception {
            Object ret = this.instanceMap.get(type);
            if (ret == null) {
                ret = this.adapter.create(this.mod, this.value, type);
                assert (ret != null);
                Object prev = this.instanceMap.putIfAbsent(type, ret);
                if (prev != null) {
                    ret = prev;
                }
            }
            return (T)ret;
        }

        @Override
        public boolean isOptional() {
            return false;
        }

        @Override
        public ModContainerImpl getModContainer() {
            return this.mod;
        }

        @Override
        public String getDefinition() {
            return this.value;
        }
    }

    static interface Entry {
        public <T> T getOrCreate(Class<T> var1) throws Exception;

        public boolean isOptional();

        public ModContainerImpl getModContainer();

        public String getDefinition();
    }
}

