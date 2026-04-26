/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.logging;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;

public class LogListeners {
    private static final Map<String, Target> TARGETS = new ConcurrentHashMap<String, Target>();

    public static Target getOrCreateTarget(String target) {
        return TARGETS.computeIfAbsent(target, s -> new Target());
    }

    public static void addListener(String target, Listener listener) {
        LogListeners.getOrCreateTarget(target).addListener(listener);
    }

    public static class Target {
        private volatile List<Listener> listeners = List.of();

        private synchronized void addListener(Listener listener) {
            ArrayList<Listener> newListeners = new ArrayList<Listener>(this.listeners.size() + 1);
            newListeners.addAll(this.listeners);
            newListeners.add(listener);
            this.listeners = newListeners;
        }

        public void post(Layout<? extends Serializable> layout, LogEvent event) {
            if (this.listeners.isEmpty()) {
                return;
            }
            String message = layout.toSerializable(event).toString();
            org.slf4j.event.Level level = Target.log4jToSlf4jLevel(event.getLevel());
            for (Listener listener : this.listeners) {
                listener.accept(message, level);
            }
        }

        private static org.slf4j.event.Level log4jToSlf4jLevel(Level level) {
            if (level == Level.ERROR) {
                return org.slf4j.event.Level.ERROR;
            }
            if (level == Level.WARN) {
                return org.slf4j.event.Level.WARN;
            }
            if (level == Level.INFO) {
                return org.slf4j.event.Level.INFO;
            }
            if (level == Level.DEBUG) {
                return org.slf4j.event.Level.DEBUG;
            }
            if (level == Level.TRACE) {
                return org.slf4j.event.Level.TRACE;
            }
            return org.slf4j.event.Level.INFO;
        }
    }

    public static interface Listener {
        public void accept(String var1, org.slf4j.event.Level var2);
    }
}

