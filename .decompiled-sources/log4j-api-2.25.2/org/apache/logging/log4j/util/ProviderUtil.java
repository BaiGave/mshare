/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import aQute.bnd.annotation.spi.ServiceConsumer;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.simple.internal.SimpleProvider;
import org.apache.logging.log4j.spi.LoggerContextFactory;
import org.apache.logging.log4j.spi.Provider;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.InternalApi;
import org.apache.logging.log4j.util.LoaderUtil;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.log4j.util.ServiceLoaderUtil;

@InternalApi
@ServiceConsumer(value=Provider.class, resolution="optional", cardinality="multiple")
public final class ProviderUtil {
    static final String PROVIDER_RESOURCE = "META-INF/log4j-provider.properties";
    static final Collection<Provider> PROVIDERS = new HashSet<Provider>();
    static final Lock STARTUP_LOCK = new ReentrantLock();
    private static final String[] COMPATIBLE_API_VERSIONS = new String[]{"2.6.0"};
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static volatile Provider PROVIDER;

    private ProviderUtil() {
    }

    static void addProvider(Provider provider) {
        if (ProviderUtil.validVersion(provider.getVersions())) {
            PROVIDERS.add(provider);
            LOGGER.debug("Loaded provider:\n{}", (Object)provider);
        } else {
            LOGGER.warn("Ignoring provider for incompatible version {}:\n{}", (Object)provider.getVersions(), (Object)provider);
        }
    }

    @SuppressFBWarnings(value={"URLCONNECTION_SSRF_FD"}, justification="Uses a fixed URL that ends in 'META-INF/log4j-provider.properties'.")
    static void loadProvider(URL url, ClassLoader cl) {
        try {
            Properties props = PropertiesUtil.loadClose(url.openStream(), url);
            ProviderUtil.addProvider(new Provider(props, url, cl));
        }
        catch (IOException e) {
            LOGGER.error("Unable to open {}", (Object)url, (Object)e);
        }
    }

    @Deprecated
    static void loadProviders(Enumeration<URL> urls, ClassLoader cl) {
        if (urls != null) {
            while (urls.hasMoreElements()) {
                ProviderUtil.loadProvider(urls.nextElement(), cl);
            }
        }
    }

    public static Provider getProvider() {
        ProviderUtil.lazyInit();
        return PROVIDER;
    }

    public static Iterable<Provider> getProviders() {
        ProviderUtil.lazyInit();
        return PROVIDERS;
    }

    public static boolean hasProviders() {
        ProviderUtil.lazyInit();
        return !PROVIDERS.isEmpty();
    }

    static void lazyInit() {
        if (PROVIDER == null) {
            try {
                STARTUP_LOCK.lockInterruptibly();
                try {
                    if (PROVIDER == null) {
                        ServiceLoaderUtil.safeStream(Provider.class, ServiceLoader.load(Provider.class, ProviderUtil.class.getClassLoader()), LOGGER).filter(provider -> ProviderUtil.validVersion(provider.getVersions())).forEach(ProviderUtil::addProvider);
                        for (LoaderUtil.UrlResource resource : LoaderUtil.findUrlResources(PROVIDER_RESOURCE, false)) {
                            ProviderUtil.loadProvider(resource.getUrl(), resource.getClassLoader());
                        }
                        PROVIDER = ProviderUtil.selectProvider(PropertiesUtil.getProperties(), PROVIDERS, LOGGER);
                    }
                }
                finally {
                    STARTUP_LOCK.unlock();
                }
            }
            catch (InterruptedException e) {
                LOGGER.fatal("Interrupted before Log4j Providers could be loaded.", (Throwable)e);
                Thread.currentThread().interrupt();
            }
        }
    }

    static Provider selectProvider(PropertiesUtil properties, Collection<Provider> providers, Logger statusLogger) {
        String factoryClassName;
        Provider selected = null;
        String providerClass = properties.getStringProperty("log4j.provider");
        if (providerClass != null) {
            if (SimpleProvider.class.getName().equals(providerClass)) {
                selected = new SimpleProvider();
            } else {
                try {
                    selected = (Provider)LoaderUtil.newInstanceOf(providerClass);
                }
                catch (Exception e) {
                    statusLogger.error("Unable to create provider {}.\nFalling back to default selection process.", (Object)PROVIDER, (Object)e);
                }
            }
        }
        if ((factoryClassName = properties.getStringProperty("log4j2.loggerContextFactory")) != null) {
            if (selected != null) {
                statusLogger.warn("Ignoring {} system property, since {} was set.", (Object)"log4j2.loggerContextFactory", (Object)"log4j.provider");
            } else {
                statusLogger.warn("Usage of the {} property is deprecated. Use the {} property instead.", (Object)"log4j2.loggerContextFactory", (Object)"log4j.provider");
                for (Provider provider : providers) {
                    if (!factoryClassName.equals(provider.getClassName())) continue;
                    selected = provider;
                    break;
                }
            }
            if (selected == null) {
                statusLogger.warn("No provider found using {} as logger context factory. The factory will be instantiated directly.", (Object)factoryClassName);
                try {
                    Class<?> clazz = LoaderUtil.loadClass(factoryClassName);
                    if (LoggerContextFactory.class.isAssignableFrom(clazz)) {
                        selected = new Provider(null, "", clazz.asSubclass(LoggerContextFactory.class));
                    } else {
                        statusLogger.error("Class {} specified in the {} system property does not extend {}", (Object)factoryClassName, (Object)"log4j2.loggerContextFactory", (Object)LoggerContextFactory.class.getName());
                    }
                }
                catch (Exception e) {
                    statusLogger.error("Unable to create class {} specified in the {} system property", (Object)factoryClassName, (Object)"log4j2.loggerContextFactory", (Object)e);
                }
            }
        }
        if (selected == null) {
            Comparator<Provider> comparator = Comparator.comparing(Provider::getPriority);
            switch (providers.size()) {
                case 0: {
                    statusLogger.error("Log4j API could not find a logging provider.");
                    break;
                }
                case 1: {
                    break;
                }
                default: {
                    statusLogger.warn(providers.stream().sorted(comparator).map(Provider::toString).collect(Collectors.joining("\n", "Log4j API found multiple logging providers:\n", "")));
                }
            }
            selected = providers.stream().max(comparator).orElseGet(SimpleProvider::new);
        }
        statusLogger.info("Using provider:\n{}", (Object)selected);
        return selected;
    }

    public static ClassLoader findClassLoader() {
        return LoaderUtil.getThreadContextClassLoader();
    }

    private static boolean validVersion(String version) {
        for (String v : COMPATIBLE_API_VERSIONS) {
            if (!version.startsWith(v)) continue;
            return true;
        }
        return false;
    }
}

