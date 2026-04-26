/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.logging.log4j.core.config.MonitorResource;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

@Plugin(name="MonitorResources", category="Core", printObject=true)
public final class MonitorResources {
    private final Set<MonitorResource> resources;

    private MonitorResources(Set<MonitorResource> resources) {
        this.resources = Objects.requireNonNull(resources, "resources");
    }

    @PluginFactory
    public static MonitorResources createMonitorResources(@PluginElement(value="monitorResource") MonitorResource[] resources) {
        Objects.requireNonNull(resources, "resources");
        LinkedHashSet distinctResources = Arrays.stream(resources).collect(Collectors.toCollection(LinkedHashSet::new));
        return new MonitorResources(distinctResources);
    }

    public Set<MonitorResource> getResources() {
        return this.resources;
    }
}

