/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.slf4j;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.status.StatusLogger;
import org.slf4j.spi.MDCAdapter;

public class Log4jMDCAdapter
implements MDCAdapter {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private final ThreadLocalMapOfStacks mapOfStacks = new ThreadLocalMapOfStacks();

    @Override
    public void put(String key, String val) {
        ThreadContext.put(key, val);
    }

    @Override
    public String get(String key) {
        return ThreadContext.get(key);
    }

    @Override
    public void remove(String key) {
        ThreadContext.remove(key);
    }

    @Override
    public void clear() {
        ThreadContext.clearMap();
    }

    @Override
    public Map<String, String> getCopyOfContextMap() {
        return ThreadContext.getContext();
    }

    @Override
    public void setContextMap(Map<String, String> map) {
        ThreadContext.clearMap();
        ThreadContext.putAll(map);
    }

    @Override
    public void pushByKey(String key, String value) {
        if (key == null) {
            ThreadContext.push(value);
        } else {
            String oldValue = this.mapOfStacks.peekByKey(key);
            if (!Objects.equals(ThreadContext.get(key), oldValue)) {
                LOGGER.warn("The key {} was used in both the string and stack-valued MDC.", (Object)key);
            }
            this.mapOfStacks.pushByKey(key, value);
            ThreadContext.put(key, value);
        }
    }

    @Override
    public String popByKey(String key) {
        if (key == null) {
            return ThreadContext.getDepth() > 0 ? ThreadContext.pop() : null;
        }
        String value = this.mapOfStacks.popByKey(key);
        if (!Objects.equals(ThreadContext.get(key), value)) {
            LOGGER.warn("The key {} was used in both the string and stack-valued MDC.", (Object)key);
        }
        ThreadContext.put(key, this.mapOfStacks.peekByKey(key));
        return value;
    }

    @Override
    public Deque<String> getCopyOfDequeByKey(String key) {
        if (key == null) {
            ThreadContext.ContextStack stack = ThreadContext.getImmutableStack();
            ArrayDeque<String> copy = new ArrayDeque<String>(stack.size());
            stack.forEach(copy::push);
            return copy;
        }
        return this.mapOfStacks.getCopyOfDequeByKey(key);
    }

    @Override
    public void clearDequeByKey(String key) {
        if (key == null) {
            ThreadContext.clearStack();
        } else {
            this.mapOfStacks.clearByKey(key);
            ThreadContext.put(key, null);
        }
    }

    private static class ThreadLocalMapOfStacks {
        private final ThreadLocal<Map<String, Deque<String>>> tlMapOfStacks = ThreadLocal.withInitial(HashMap::new);

        private ThreadLocalMapOfStacks() {
        }

        public void pushByKey(String key, String value) {
            this.tlMapOfStacks.get().computeIfAbsent(key, ignored -> new ArrayDeque()).push(value);
        }

        public String popByKey(String key) {
            Deque<String> deque = this.tlMapOfStacks.get().get(key);
            return deque != null ? deque.poll() : null;
        }

        public Deque<String> getCopyOfDequeByKey(String key) {
            Deque<String> deque = this.tlMapOfStacks.get().get(key);
            return deque != null ? new ArrayDeque<String>(deque) : null;
        }

        public void clearByKey(String key) {
            Deque<String> deque = this.tlMapOfStacks.get().get(key);
            if (deque != null) {
                deque.clear();
            }
        }

        public String peekByKey(String key) {
            Deque<String> deque = this.tlMapOfStacks.get().get(key);
            return deque != null ? deque.peek() : null;
        }
    }
}

