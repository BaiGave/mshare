/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.ThreadContext;

public class CloseableThreadContext {
    private CloseableThreadContext() {
    }

    public static Instance push(String message) {
        return new Instance().push(message);
    }

    public static Instance push(String message, Object ... args) {
        return new Instance().push(message, args);
    }

    public static Instance put(String key, String value) {
        return new Instance().put(key, value);
    }

    public static Instance pushAll(List<String> messages) {
        return new Instance().pushAll(messages);
    }

    public static Instance putAll(Map<String, String> values) {
        return new Instance().putAll(values);
    }

    public static class Instance
    implements AutoCloseable {
        private int pushCount = 0;
        private final Map<String, String> originalValues = new HashMap<String, String>();

        private Instance() {
        }

        public Instance push(String message) {
            ThreadContext.push(message);
            ++this.pushCount;
            return this;
        }

        public Instance push(String message, Object[] args) {
            ThreadContext.push(message, args);
            ++this.pushCount;
            return this;
        }

        public Instance put(String key, String value) {
            if (!this.originalValues.containsKey(key)) {
                this.originalValues.put(key, ThreadContext.get(key));
            }
            ThreadContext.put(key, value);
            return this;
        }

        public Instance putAll(Map<String, String> values) {
            Map<String, String> currentValues = ThreadContext.getContext();
            ThreadContext.putAll(values);
            for (String key : values.keySet()) {
                if (this.originalValues.containsKey(key)) continue;
                this.originalValues.put(key, currentValues.get(key));
            }
            return this;
        }

        public Instance pushAll(List<String> messages) {
            for (String message : messages) {
                this.push(message);
            }
            return this;
        }

        @Override
        public void close() {
            this.closeStack();
            this.closeMap();
        }

        private void closeMap() {
            HashMap<String, String> valuesToReplace = new HashMap<String, String>(this.originalValues.size());
            ArrayList<String> keysToRemove = new ArrayList<String>(this.originalValues.size());
            for (Map.Entry<String, String> entry : this.originalValues.entrySet()) {
                String key = entry.getKey();
                String originalValue = entry.getValue();
                if (null == originalValue) {
                    keysToRemove.add(key);
                    continue;
                }
                valuesToReplace.put(key, originalValue);
            }
            if (!valuesToReplace.isEmpty()) {
                ThreadContext.putAll(valuesToReplace);
            }
            if (!keysToRemove.isEmpty()) {
                ThreadContext.removeAll(keysToRemove);
            }
        }

        private void closeStack() {
            for (int i = 0; i < this.pushCount; ++i) {
                ThreadContext.pop();
            }
            this.pushCount = 0;
        }
    }
}

