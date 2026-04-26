/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.InternalApi;
import org.apache.logging.log4j.util.OsgiServiceLocator;
import org.apache.logging.log4j.util.StackLocatorUtil;

@InternalApi
public final class ServiceLoaderUtil {
    private static final int MAX_BROKEN_SERVICES = 8;

    private ServiceLoaderUtil() {
    }

    public static <S> Stream<S> safeStream(Class<S> serviceType, ServiceLoader<? extends S> serviceLoader, Logger logger) {
        Objects.requireNonNull(serviceLoader, "serviceLoader");
        HashSet classes = new HashSet();
        Stream services = StreamSupport.stream(new ServiceLoaderSpliterator(serviceType, serviceLoader, logger), false);
        Class<?> callerClass = StackLocatorUtil.getCallerClass(2);
        Stream allServices = OsgiServiceLocator.isAvailable() && callerClass != null ? Stream.concat(services, OsgiServiceLocator.loadServices(serviceType, callerClass, logger)) : services;
        return allServices.filter(service -> classes.add(service.getClass()));
    }

    private static final class ServiceLoaderSpliterator<S>
    extends Spliterators.AbstractSpliterator<S> {
        private final String serviceName;
        private final Iterator<? extends S> serviceIterator;
        private final Logger logger;

        private ServiceLoaderSpliterator(Class<S> serviceType, Iterable<? extends S> serviceLoader, Logger logger) {
            super(Long.MAX_VALUE, 1296);
            this.serviceName = serviceType.getName();
            this.serviceIterator = serviceLoader.iterator();
            this.logger = logger;
        }

        @Override
        public boolean tryAdvance(Consumer<? super S> action) {
            int i = 8;
            while (i-- > 0) {
                try {
                    if (!this.serviceIterator.hasNext()) continue;
                    action.accept(this.serviceIterator.next());
                    return true;
                }
                catch (LinkageError | ServiceConfigurationError e) {
                    this.logger.warn("Unable to load implementation for service {}", (Object)this.serviceName, (Object)e);
                }
                catch (Exception e) {
                    this.logger.warn("Unexpected exception  while loading implementation for service {}", (Object)this.serviceName, (Object)e);
                    throw e;
                }
            }
            return false;
        }
    }
}

