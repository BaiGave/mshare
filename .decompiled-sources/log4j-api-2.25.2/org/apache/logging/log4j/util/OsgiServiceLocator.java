/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.stream.Stream;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.Supplier;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.wiring.BundleRevision;

public class OsgiServiceLocator {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static final boolean OSGI_AVAILABLE = OsgiServiceLocator.checkOsgiAvailable();

    private static boolean checkOsgiAvailable() {
        try {
            Class<?> clazz = Class.forName("org.osgi.framework.FrameworkUtil");
            return clazz.getMethod("getBundle", Class.class).invoke(null, OsgiServiceLocator.class) != null;
        }
        catch (ClassNotFoundException | LinkageError | NoSuchMethodException e) {
            return false;
        }
        catch (Throwable error) {
            LOGGER.error("Unknown error checking OSGI environment.", error);
            return false;
        }
    }

    public static boolean isAvailable() {
        return OSGI_AVAILABLE;
    }

    public static <T> Stream<T> loadServices(Class<T> serviceType, MethodHandles.Lookup lookup) {
        return OsgiServiceLocator.loadServices(serviceType, lookup, true);
    }

    public static <T> Stream<T> loadServices(Class<T> serviceType, MethodHandles.Lookup lookup, boolean verbose) {
        Class<?> lookupClass = Objects.requireNonNull(lookup, "lookup").lookupClass();
        return OsgiServiceLocator.loadServices(serviceType, lookupClass, StatusLogger.getLogger());
    }

    static <T> Stream<T> loadServices(Class<T> serviceType, Class<?> callerClass, Logger logger) {
        Bundle bundle = FrameworkUtil.getBundle(callerClass);
        if (bundle != null && !OsgiServiceLocator.isFragment(bundle)) {
            BundleContext ctx = bundle.getBundleContext();
            if (ctx == null) {
                Supplier[] supplierArray = new Supplier[3];
                supplierArray[0] = serviceType::getName;
                supplierArray[1] = () -> ((Bundle)bundle).getSymbolicName();
                supplierArray[2] = () -> {
                    switch (bundle.getState()) {
                        case 1: {
                            return "UNINSTALLED";
                        }
                        case 2: {
                            return "INSTALLED";
                        }
                        case 4: {
                            return "RESOLVED";
                        }
                        case 8: {
                            return "STARTING";
                        }
                        case 16: {
                            return "STOPPING";
                        }
                        case 32: {
                            return "ACTIVE";
                        }
                    }
                    return "UNKNOWN";
                };
                logger.warn("Unable to load OSGi services for service {}: bundle {} (state {}) does not have a valid BundleContext", supplierArray);
            } else {
                try {
                    return ctx.getServiceReferences(serviceType, null).stream().map(arg_0 -> ((BundleContext)ctx).getService(arg_0));
                }
                catch (Exception e) {
                    logger.error("Unable to load OSGI services for service {}", (Object)serviceType, (Object)e);
                }
            }
        }
        return Stream.empty();
    }

    private static boolean isFragment(Bundle bundle) {
        try {
            return (((BundleRevision)bundle.adapt(BundleRevision.class)).getTypes() & 1) != 0;
        }
        catch (SecurityException ignored) {
            return false;
        }
    }
}

