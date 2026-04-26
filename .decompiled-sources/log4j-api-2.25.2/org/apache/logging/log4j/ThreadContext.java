/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.spi.CleanableThreadContextMap;
import org.apache.logging.log4j.spi.DefaultThreadContextMap;
import org.apache.logging.log4j.spi.DefaultThreadContextStack;
import org.apache.logging.log4j.spi.MutableThreadContextStack;
import org.apache.logging.log4j.spi.ReadOnlyThreadContextMap;
import org.apache.logging.log4j.spi.ThreadContextMap;
import org.apache.logging.log4j.spi.ThreadContextMap2;
import org.apache.logging.log4j.spi.ThreadContextMapFactory;
import org.apache.logging.log4j.spi.ThreadContextStack;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.log4j.util.ProviderUtil;

public final class ThreadContext {
    public static final Map<String, String> EMPTY_MAP = Collections.emptyMap();
    public static final ThreadContextStack EMPTY_STACK = new EmptyThreadContextStack();
    private static final String DISABLE_STACK = "disableThreadContextStack";
    private static final String DISABLE_ALL = "disableThreadContext";
    private static ThreadContextStack contextStack;
    private static ThreadContextMap contextMap;
    private static ReadOnlyThreadContextMap readOnlyContextMap;

    private ThreadContext() {
    }

    public static void init() {
        PropertiesUtil properties = PropertiesUtil.getProperties();
        contextStack = properties.getBooleanProperty(DISABLE_STACK) || properties.getBooleanProperty(DISABLE_ALL) ? new NoOpThreadContextStack() : new DefaultThreadContextStack();
        ThreadContextMapFactory.init();
        contextMap = ProviderUtil.getProvider().getThreadContextMapInstance();
        readOnlyContextMap = contextMap instanceof ReadOnlyThreadContextMap ? (ReadOnlyThreadContextMap)((Object)contextMap) : null;
    }

    public static void put(String key, String value) {
        contextMap.put(key, value);
    }

    public static void putIfNull(String key, String value) {
        if (!contextMap.containsKey(key)) {
            contextMap.put(key, value);
        }
    }

    public static void putAll(Map<String, String> m) {
        if (contextMap instanceof ThreadContextMap2) {
            ((ThreadContextMap2)contextMap).putAll(m);
        } else if (contextMap instanceof DefaultThreadContextMap) {
            ((DefaultThreadContextMap)contextMap).putAll(m);
        } else {
            for (Map.Entry<String, String> entry : m.entrySet()) {
                contextMap.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public static String get(String key) {
        return contextMap.get(key);
    }

    public static void remove(String key) {
        contextMap.remove(key);
    }

    public static void removeAll(Iterable<String> keys) {
        if (contextMap instanceof CleanableThreadContextMap) {
            ((CleanableThreadContextMap)contextMap).removeAll(keys);
        } else if (contextMap instanceof DefaultThreadContextMap) {
            ((DefaultThreadContextMap)contextMap).removeAll(keys);
        } else {
            for (String key : keys) {
                contextMap.remove(key);
            }
        }
    }

    public static void clearMap() {
        contextMap.clear();
    }

    public static void clearAll() {
        ThreadContext.clearMap();
        ThreadContext.clearStack();
    }

    public static boolean containsKey(String key) {
        return contextMap.containsKey(key);
    }

    public static Map<String, String> getContext() {
        return contextMap.getCopy();
    }

    public static Map<String, String> getImmutableContext() {
        Map<String, String> map = contextMap.getImmutableMapOrNull();
        return map == null ? EMPTY_MAP : map;
    }

    public static ReadOnlyThreadContextMap getThreadContextMap() {
        return readOnlyContextMap;
    }

    public static boolean isEmpty() {
        return contextMap.isEmpty();
    }

    public static void clearStack() {
        contextStack.clear();
    }

    public static ContextStack cloneStack() {
        return contextStack.copy();
    }

    public static ContextStack getImmutableStack() {
        ContextStack result = contextStack.getImmutableStackOrNull();
        return result == null ? EMPTY_STACK : result;
    }

    public static void setStack(Collection<String> stack) {
        if (stack.isEmpty()) {
            return;
        }
        contextStack.clear();
        contextStack.addAll(stack);
    }

    public static int getDepth() {
        return contextStack.getDepth();
    }

    public static String pop() {
        return contextStack.pop();
    }

    public static String peek() {
        return contextStack.peek();
    }

    public static void push(String message) {
        contextStack.push(message);
    }

    public static void push(String message, Object ... args) {
        contextStack.push(ParameterizedMessage.format(message, args));
    }

    public static void removeStack() {
        contextStack.clear();
    }

    public static void trim(int depth) {
        contextStack.trim(depth);
    }

    static {
        ThreadContext.init();
    }

    private static final class NoOpThreadContextStack
    extends EmptyThreadContextStack {
        private NoOpThreadContextStack() {
        }

        @Override
        public boolean add(String ignored) {
            return false;
        }

        @Override
        public boolean addAll(Collection<? extends String> ignored) {
            return false;
        }

        @Override
        public void push(String ignored) {
        }

        @Override
        public boolean remove(Object ignored) {
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> ignored) {
            return false;
        }

        @Override
        public boolean retainAll(Collection<?> ignored) {
            return false;
        }
    }

    public static interface ContextStack
    extends Serializable,
    Collection<String> {
        public String pop();

        public String peek();

        public void push(String var1);

        public int getDepth();

        public List<String> asList();

        public void trim(int var1);

        public ContextStack copy();

        public ContextStack getImmutableStackOrNull();
    }

    private static class EmptyThreadContextStack
    extends AbstractCollection<String>
    implements ThreadContextStack {
        private static final long serialVersionUID = 1L;

        private EmptyThreadContextStack() {
        }

        @Override
        public String pop() {
            return null;
        }

        @Override
        public String peek() {
            return null;
        }

        @Override
        public void push(String message) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getDepth() {
            return 0;
        }

        @Override
        public List<String> asList() {
            return Collections.emptyList();
        }

        @Override
        public void trim(int depth) {
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Collection && ((Collection)o).isEmpty();
        }

        @Override
        public int hashCode() {
            return 1;
        }

        @Override
        public ContextStack copy() {
            return new MutableThreadContextStack();
        }

        @Override
        public <T> T[] toArray(T[] ignored) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean add(String ignored) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
        }

        @Override
        public boolean containsAll(Collection<?> ignored) {
            return false;
        }

        @Override
        public boolean addAll(Collection<? extends String> ignored) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection<?> ignored) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> ignored) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Iterator<String> iterator() {
            return Collections.emptyIterator();
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public ContextStack getImmutableStackOrNull() {
            return this;
        }
    }
}

