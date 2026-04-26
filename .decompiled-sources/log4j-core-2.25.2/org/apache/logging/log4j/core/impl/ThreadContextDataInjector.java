/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.impl;

import aQute.bnd.annotation.spi.ServiceConsumer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.ContextDataInjector;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.impl.ContextDataFactory;
import org.apache.logging.log4j.core.impl.JdkMapAdapterStringMap;
import org.apache.logging.log4j.core.util.ContextDataProvider;
import org.apache.logging.log4j.spi.ReadOnlyThreadContextMap;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.apache.logging.log4j.util.ServiceLoaderUtil;
import org.apache.logging.log4j.util.StringMap;

@ServiceConsumer(value=ContextDataProvider.class, resolution="optional", cardinality="multiple")
public class ThreadContextDataInjector {
    private static final Logger LOGGER = StatusLogger.getLogger();
    public static Collection<ContextDataProvider> contextDataProviders = new ConcurrentLinkedDeque<ContextDataProvider>();
    private static final List<ContextDataProvider> SERVICE_PROVIDERS = ThreadContextDataInjector.getServiceProviders();

    @Deprecated
    public static void initServiceProviders() {
    }

    private static List<ContextDataProvider> getServiceProviders() {
        ArrayList providers = new ArrayList();
        ServiceLoaderUtil.safeStream(ContextDataProvider.class, ServiceLoader.load(ContextDataProvider.class, ThreadContextDataInjector.class.getClassLoader()), LOGGER).forEach(providers::add);
        return Collections.unmodifiableList(providers);
    }

    public static void copyProperties(List<Property> properties, StringMap result) {
        if (properties != null) {
            for (int i = 0; i < properties.size(); ++i) {
                Property prop = properties.get(i);
                result.putValue(prop.getName(), prop.getValue());
            }
        }
    }

    private static List<ContextDataProvider> getProviders() {
        ArrayList<ContextDataProvider> providers = new ArrayList<ContextDataProvider>(contextDataProviders.size() + SERVICE_PROVIDERS.size());
        providers.addAll(contextDataProviders);
        providers.addAll(SERVICE_PROVIDERS);
        return providers;
    }

    static /* synthetic */ List access$000() {
        return ThreadContextDataInjector.getProviders();
    }

    public static class ForCopyOnWriteThreadContextMap
    extends AbstractContextDataInjector {
        @Override
        public StringMap injectContextData(List<Property> props, StringMap ignore) {
            if (this.providers.size() == 1 && (props == null || props.isEmpty())) {
                return ((ContextDataProvider)this.providers.get(0)).supplyStringMap();
            }
            int count = props == null ? 0 : props.size();
            StringMap[] maps = new StringMap[this.providers.size()];
            for (int i = 0; i < this.providers.size(); ++i) {
                maps[i] = ((ContextDataProvider)this.providers.get(i)).supplyStringMap();
                count += maps[i].size();
            }
            StringMap result = ContextDataFactory.createContextData(count);
            ThreadContextDataInjector.copyProperties(props, result);
            for (StringMap map : maps) {
                result.putAll(map);
            }
            return result;
        }

        @Override
        public ReadOnlyStringMap rawContextData() {
            return ThreadContext.getThreadContextMap().getReadOnlyContextData();
        }
    }

    public static class ForGarbageFreeThreadContextMap
    extends AbstractContextDataInjector {
        @Override
        public StringMap injectContextData(List<Property> props, StringMap reusable) {
            ThreadContextDataInjector.copyProperties(props, reusable);
            for (int i = 0; i < this.providers.size(); ++i) {
                reusable.putAll(((ContextDataProvider)this.providers.get(i)).supplyStringMap());
            }
            return reusable;
        }

        @Override
        public ReadOnlyStringMap rawContextData() {
            return ThreadContext.getThreadContextMap().getReadOnlyContextData();
        }
    }

    public static class ForDefaultThreadContextMap
    extends AbstractContextDataInjector {
        @Override
        public StringMap injectContextData(List<Property> props, StringMap ignore) {
            Map<Object, Object> copy;
            if (this.providers.size() == 1) {
                copy = ((ContextDataProvider)this.providers.get(0)).supplyContextData();
            } else {
                copy = new HashMap();
                for (ContextDataProvider provider : this.providers) {
                    copy.putAll(provider.supplyContextData());
                }
            }
            if (props == null || props.isEmpty()) {
                return copy.isEmpty() ? ContextDataFactory.emptyFrozenContextData() : ForDefaultThreadContextMap.frozenStringMap(copy);
            }
            JdkMapAdapterStringMap result = new JdkMapAdapterStringMap(new HashMap<Object, Object>(copy), false);
            for (int i = 0; i < props.size(); ++i) {
                Property prop = props.get(i);
                if (copy.containsKey(prop.getName())) continue;
                result.putValue(prop.getName(), prop.getValue());
            }
            result.freeze();
            return result;
        }

        private static JdkMapAdapterStringMap frozenStringMap(Map<String, String> copy) {
            return new JdkMapAdapterStringMap(copy, true);
        }

        @Override
        public ReadOnlyStringMap rawContextData() {
            ReadOnlyThreadContextMap map = ThreadContext.getThreadContextMap();
            if (map != null) {
                return map.getReadOnlyContextData();
            }
            Map<String, String> copy = ThreadContext.getImmutableContext();
            return copy.isEmpty() ? ContextDataFactory.emptyFrozenContextData() : new JdkMapAdapterStringMap(copy, true);
        }
    }

    private static abstract class AbstractContextDataInjector
    implements ContextDataInjector {
        final List<ContextDataProvider> providers = ThreadContextDataInjector.access$000();

        AbstractContextDataInjector() {
        }

        @Override
        public Object getValue(String key) {
            for (ContextDataProvider provider : this.providers) {
                Object value = provider.getValue(key);
                if (value == null) continue;
                return value;
            }
            return null;
        }
    }
}

