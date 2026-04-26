/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.message2;

import com.ibm.icu.message2.FormatterFactory;
import com.ibm.icu.message2.SelectorFactory;
import com.ibm.icu.message2.StringUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Deprecated
public class MFFunctionRegistry {
    private final Map<String, FormatterFactory> formattersMap;
    private final Map<String, SelectorFactory> selectorsMap;
    private final Map<Class<?>, String> classToFormatter;

    private MFFunctionRegistry(Builder builder) {
        this.formattersMap = new HashMap<String, FormatterFactory>(builder.formattersMap);
        this.selectorsMap = new HashMap<String, SelectorFactory>(builder.selectorsMap);
        this.classToFormatter = new HashMap(builder.classToFormatter);
    }

    @Deprecated
    public static Builder builder() {
        return new Builder();
    }

    @Deprecated
    public FormatterFactory getFormatter(String formatterName) {
        return this.formattersMap.get(StringUtils.toNfc(formatterName));
    }

    @Deprecated
    public Set<String> getFormatterNames() {
        return this.formattersMap.keySet();
    }

    @Deprecated
    public String getDefaultFormatterNameForType(Class<?> clazz) {
        String result = this.classToFormatter.get(clazz);
        if (result != null) {
            return result;
        }
        for (Map.Entry<Class<?>, String> e : this.classToFormatter.entrySet()) {
            if (!e.getKey().isAssignableFrom(clazz)) continue;
            return e.getValue();
        }
        return null;
    }

    @Deprecated
    public Set<Class<?>> getDefaultFormatterTypes() {
        return this.classToFormatter.keySet();
    }

    @Deprecated
    public SelectorFactory getSelector(String selectorName) {
        return this.selectorsMap.get(StringUtils.toNfc(selectorName));
    }

    @Deprecated
    public Set<String> getSelectorNames() {
        return this.selectorsMap.keySet();
    }

    @Deprecated
    public static class Builder {
        private final Map<String, FormatterFactory> formattersMap = new HashMap<String, FormatterFactory>();
        private final Map<String, SelectorFactory> selectorsMap = new HashMap<String, SelectorFactory>();
        private final Map<Class<?>, String> classToFormatter = new HashMap();

        private Builder() {
        }

        @Deprecated
        public Builder addAll(MFFunctionRegistry functionRegistry) {
            this.formattersMap.putAll(functionRegistry.formattersMap);
            this.selectorsMap.putAll(functionRegistry.selectorsMap);
            this.classToFormatter.putAll(functionRegistry.classToFormatter);
            return this;
        }

        @Deprecated
        public Builder setFormatter(String formatterName, FormatterFactory formatterFactory) {
            this.formattersMap.put(StringUtils.toNfc(formatterName), formatterFactory);
            return this;
        }

        @Deprecated
        public Builder removeFormatter(String formatterName) {
            this.formattersMap.remove(StringUtils.toNfc(formatterName));
            return this;
        }

        @Deprecated
        public Builder clearFormatters() {
            this.formattersMap.clear();
            return this;
        }

        @Deprecated
        public Builder setDefaultFormatterNameForType(Class<?> clazz, String formatterName) {
            this.classToFormatter.put(clazz, StringUtils.toNfc(formatterName));
            return this;
        }

        @Deprecated
        public Builder removeDefaultFormatterNameForType(Class<?> clazz) {
            this.classToFormatter.remove(clazz);
            return this;
        }

        @Deprecated
        public Builder clearDefaultFormatterNames() {
            this.classToFormatter.clear();
            return this;
        }

        @Deprecated
        public Builder setSelector(String selectorName, SelectorFactory selectorFactory) {
            this.selectorsMap.put(StringUtils.toNfc(selectorName), selectorFactory);
            return this;
        }

        @Deprecated
        public Builder removeSelector(String selectorName) {
            this.selectorsMap.remove(StringUtils.toNfc(selectorName));
            return this;
        }

        @Deprecated
        public Builder clearSelectors() {
            this.selectorsMap.clear();
            return this;
        }

        @Deprecated
        public MFFunctionRegistry build() {
            return new MFFunctionRegistry(this);
        }
    }
}

