/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config.plugins.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.plugins.util.PluginRegistry;
import org.apache.logging.log4j.core.config.plugins.util.PluginType;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.StackLocatorUtil;
import org.apache.logging.log4j.util.Strings;

public class PluginManager {
    private static final CopyOnWriteArrayList<String> PACKAGES = new CopyOnWriteArrayList();
    private static final String LOG4J_PACKAGES = "org.apache.logging.log4j.core";
    private static final String DEPRECATION_WARNING = "The use of package scanning to locate Log4j plugins is deprecated.\nPlease remove the deprecated `{}` method call from `{}`.\nSee https://logging.apache.org/log4j/2.x/faq.html#package-scanning for details.";
    private static final String PLUGIN_DESCRIPTOR_DOC = "See https://logging.apache.org/log4j/2.x/faq.html#plugin-descriptors for details.";
    private static final String PLUGIN_REGISTRY_DOC = "See https://logging.apache.org/log4j/2.x/manual/plugins.html#plugin-registry for details.";
    private static final Logger LOGGER = StatusLogger.getLogger();
    private Map<String, PluginType<?>> plugins = new HashMap();
    private final String category;

    public PluginManager(String category) {
        this.category = category;
    }

    @Deprecated
    public static void main(String[] args) {
        System.err.println("ERROR: this tool is superseded by the annotation processor included in log4j-core.");
        System.err.println("If the annotation processor does not work for you, please see the manual page:");
        System.err.println("https://logging.apache.org/log4j/2.x/manual/configuration.html#ConfigurationSyntax");
        System.exit(-1);
    }

    @Deprecated
    public static void addPackage(String p) {
        LOGGER.warn(DEPRECATION_WARNING, (Object)"PluginManager.addPackage()", (Object)StackLocatorUtil.getStackTraceElement(2));
        if (Strings.isBlank(p)) {
            return;
        }
        PACKAGES.addIfAbsent(p);
    }

    @Deprecated
    public static void addPackages(Collection<String> packages) {
        LOGGER.warn(DEPRECATION_WARNING, (Object)"PluginManager.addPackages()", (Object)StackLocatorUtil.getStackTraceElement(2));
        for (String pkg : packages) {
            if (!Strings.isNotBlank(pkg)) continue;
            PACKAGES.addIfAbsent(pkg);
        }
    }

    static void clearPackages() {
        PACKAGES.clear();
    }

    public PluginType<?> getPluginType(String name) {
        return this.plugins.get(Strings.toRootLowerCase(name));
    }

    public Map<String, PluginType<?>> getPlugins() {
        return this.plugins;
    }

    public void collectPlugins() {
        this.collectPlugins(null);
    }

    public void collectPlugins(List<String> packages) {
        String categoryLowerCase = Strings.toRootLowerCase(this.category);
        LinkedHashMap newPlugins = new LinkedHashMap();
        Map<String, List<PluginType<?>>> builtInPlugins = PluginRegistry.getInstance().loadFromMainClassLoader();
        if (builtInPlugins.isEmpty()) {
            LOGGER.warn("No Log4j plugin descriptor was found in the classpath.\nFalling back to scanning the `{}` package.\n{}", (Object)LOG4J_PACKAGES, (Object)PLUGIN_DESCRIPTOR_DOC);
            builtInPlugins = PluginRegistry.getInstance().loadFromPackage(LOG4J_PACKAGES);
        }
        PluginManager.mergeByName(newPlugins, builtInPlugins.get(categoryLowerCase), null);
        for (Map<String, List<PluginType<?>>> map : PluginRegistry.getInstance().getPluginsByCategoryByBundleId().values()) {
            PluginManager.mergeByName(newPlugins, map.get(categoryLowerCase), null);
        }
        ArrayList<String> scannedPluginClassNames = new ArrayList<String>();
        for (String pkg : PACKAGES) {
            PluginManager.mergeByName(newPlugins, PluginRegistry.getInstance().loadFromPackage(pkg).get(categoryLowerCase), scannedPluginClassNames);
        }
        if (packages != null) {
            for (String pkg : packages) {
                PluginManager.mergeByName(newPlugins, PluginRegistry.getInstance().loadFromPackage(pkg).get(categoryLowerCase), scannedPluginClassNames);
            }
        }
        if (!scannedPluginClassNames.isEmpty()) {
            String customPlugins;
            Predicate<String> predicate = PluginManager::isCustomPlugin;
            String standardPlugins = scannedPluginClassNames.stream().filter(predicate.negate()).collect(Collectors.joining("\n\t"));
            if (!standardPlugins.isEmpty()) {
                LOGGER.warn("The Log4j plugin descriptors for the following `{}` plugins are missing:\n\t{}\n{}", (Object)this.category, (Object)standardPlugins, (Object)PLUGIN_DESCRIPTOR_DOC);
            }
            if (!(customPlugins = scannedPluginClassNames.stream().filter(predicate).collect(Collectors.joining("\n\t"))).isEmpty()) {
                LOGGER.warn("Some custom `{}` Log4j plugins are not properly registered:\n\t{}\nPlease consider reporting this to the maintainers of these plugins.\n{}", (Object)this.category, (Object)customPlugins, (Object)PLUGIN_REGISTRY_DOC);
            }
        }
        LOGGER.debug("PluginManager '{}' found {} plugins", (Object)this.category, (Object)newPlugins.size());
        this.plugins = newPlugins;
    }

    private static boolean isCustomPlugin(String className) {
        return !className.startsWith("org.apache.logging.log4j") && !className.startsWith("org.apache.log4j");
    }

    private static void mergeByName(Map<String, PluginType<?>> newPlugins, List<PluginType<?>> plugins, List<String> mergedPluginClassNames) {
        if (plugins == null) {
            return;
        }
        for (PluginType<?> pluginType : plugins) {
            String key = pluginType.getKey();
            PluginType<?> existing = newPlugins.get(key);
            if (existing == null) {
                newPlugins.put(key, pluginType);
                if (mergedPluginClassNames == null) continue;
                mergedPluginClassNames.add(pluginType.getPluginClass().getName());
                continue;
            }
            if (existing.getPluginClass().equals(pluginType.getPluginClass())) continue;
            LOGGER.warn("Plugin [{}] is already mapped to {}, ignoring {}", (Object)key, (Object)existing.getPluginClass(), (Object)pluginType.getPluginClass());
        }
    }
}

