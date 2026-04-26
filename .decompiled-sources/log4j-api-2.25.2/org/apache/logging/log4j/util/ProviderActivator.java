/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import java.util.Hashtable;
import org.apache.logging.log4j.spi.Provider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public abstract class ProviderActivator
implements BundleActivator {
    public static final String API_VERSION = "APIVersion";
    private final Provider provider;
    private ServiceRegistration<Provider> providerRegistration = null;

    protected ProviderActivator(Provider provider) {
        this.provider = provider;
    }

    public void start(BundleContext context) throws Exception {
        Hashtable<String, String> props = new Hashtable<String, String>();
        props.put(API_VERSION, this.provider.getVersions());
        this.providerRegistration = context.registerService(Provider.class, (Object)this.provider, props);
    }

    public void stop(BundleContext context) throws Exception {
        if (this.providerRegistration != null) {
            this.providerRegistration.unregister();
        }
    }
}

