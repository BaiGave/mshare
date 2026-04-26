/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.osgi;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.plugins.util.PluginRegistry;
import org.apache.logging.log4j.core.impl.Log4jProvider;
import org.apache.logging.log4j.core.impl.ThreadContextDataInjector;
import org.apache.logging.log4j.core.impl.ThreadContextDataProvider;
import org.apache.logging.log4j.core.osgi.BundleContextSelector;
import org.apache.logging.log4j.core.util.ContextDataProvider;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.log4j.util.ProviderActivator;
import org.osgi.annotation.bundle.Header;
import org.osgi.annotation.bundle.Headers;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.framework.wiring.BundleWiring;

@Headers(value={@Header(name="Bundle-Activator", value="${@class}"), @Header(name="Bundle-ActivationPolicy", value="lazy")})
public final class Activator
extends ProviderActivator
implements SynchronousBundleListener {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private final AtomicReference<BundleContext> contextRef = new AtomicReference();
    private ServiceRegistration<ContextDataProvider> contextDataRegistration = null;

    public Activator() {
        super(new Log4jProvider());
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        ThreadContextDataProvider threadContextProvider = new ThreadContextDataProvider();
        this.contextDataRegistration = context.registerService(ContextDataProvider.class, (Object)threadContextProvider, null);
        Activator.loadContextProviders(context);
        if (PropertiesUtil.getProperties().getStringProperty("Log4jContextSelector") == null) {
            System.setProperty("Log4jContextSelector", BundleContextSelector.class.getName());
        }
        if (this.contextRef.compareAndSet(null, context)) {
            context.addBundleListener((BundleListener)this);
            Activator.scanInstalledBundlesForPlugins(context);
        }
    }

    private static void scanInstalledBundlesForPlugins(BundleContext context) {
        Bundle[] bundles;
        for (Bundle bundle : bundles = context.getBundles()) {
            Activator.scanBundleForPlugins(bundle);
        }
    }

    private static void scanBundleForPlugins(Bundle bundle) {
        long bundleId = bundle.getBundleId();
        if (bundle.getState() == 32 && bundleId != 0L) {
            LOGGER.trace("Scanning bundle [{}, id=%d] for plugins.", (Object)bundle.getSymbolicName(), (Object)bundleId);
            PluginRegistry.getInstance().loadFromBundle(bundleId, ((BundleWiring)bundle.adapt(BundleWiring.class)).getClassLoader());
        }
    }

    private static void loadContextProviders(BundleContext bundleContext) {
        try {
            Collection serviceReferences = bundleContext.getServiceReferences(ContextDataProvider.class, null);
            for (ServiceReference serviceReference : serviceReferences) {
                ContextDataProvider provider = (ContextDataProvider)bundleContext.getService(serviceReference);
                ThreadContextDataInjector.contextDataProviders.add(provider);
            }
        }
        catch (InvalidSyntaxException ex) {
            LOGGER.error("Error accessing context data provider", (Throwable)ex);
        }
    }

    private static void stopBundlePlugins(Bundle bundle) {
        LOGGER.trace("Stopping bundle [{}] plugins.", (Object)bundle.getSymbolicName());
        PluginRegistry.getInstance().clearBundlePlugins(bundle.getBundleId());
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        this.contextDataRegistration.unregister();
        this.contextRef.compareAndSet(context, null);
        LogManager.shutdown();
        super.stop(context);
    }

    public void bundleChanged(BundleEvent event) {
        switch (event.getType()) {
            case 2: {
                Activator.scanBundleForPlugins(event.getBundle());
                break;
            }
            case 256: {
                Activator.stopBundlePlugins(event.getBundle());
                break;
            }
        }
    }
}

