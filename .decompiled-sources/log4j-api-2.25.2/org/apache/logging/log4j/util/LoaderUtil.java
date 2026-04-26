/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.function.Supplier;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.Cast;
import org.apache.logging.log4j.util.InternalApi;
import org.apache.logging.log4j.util.InternalException;
import org.apache.logging.log4j.util.LazyBoolean;
import org.apache.logging.log4j.util.PropertiesUtil;

@InternalApi
public final class LoaderUtil {
    private static final Logger LOGGER = StatusLogger.getLogger();
    public static final String IGNORE_TCCL_PROPERTY = "log4j.ignoreTCL";
    private static Boolean ignoreTCCL;
    static final RuntimePermission GET_CLASS_LOADER;
    static final LazyBoolean GET_CLASS_LOADER_DISABLED;
    private static final PrivilegedAction<ClassLoader> TCCL_GETTER;

    private LoaderUtil() {
    }

    public static ClassLoader getClassLoader() {
        return LoaderUtil.getClassLoader(LoaderUtil.class, null);
    }

    public static ClassLoader getClassLoader(Class<?> class1, Class<?> class2) {
        PrivilegedAction<ClassLoader> action = () -> {
            ClassLoader referenceLoader;
            ClassLoader loader1 = class1 == null ? null : class1.getClassLoader();
            ClassLoader loader2 = class2 == null ? null : class2.getClassLoader();
            ClassLoader classLoader = referenceLoader = GET_CLASS_LOADER_DISABLED.getAsBoolean() ? LoaderUtil.getThisClassLoader() : Thread.currentThread().getContextClassLoader();
            if (LoaderUtil.isChild(referenceLoader, loader1)) {
                return LoaderUtil.isChild(referenceLoader, loader2) ? referenceLoader : loader2;
            }
            return LoaderUtil.isChild(loader1, loader2) ? loader1 : loader2;
        };
        return LoaderUtil.runPrivileged(action);
    }

    private static boolean isChild(ClassLoader loader1, ClassLoader loader2) {
        if (loader1 != null && loader2 != null) {
            ClassLoader parent;
            for (parent = loader1.getParent(); parent != null && parent != loader2; parent = parent.getParent()) {
            }
            return parent != null;
        }
        return loader1 != null;
    }

    public static ClassLoader getThreadContextClassLoader() {
        try {
            return GET_CLASS_LOADER_DISABLED.getAsBoolean() ? LoaderUtil.getThisClassLoader() : LoaderUtil.runPrivileged(TCCL_GETTER);
        }
        catch (SecurityException ignored) {
            return null;
        }
    }

    private static ClassLoader getThisClassLoader() {
        return LoaderUtil.class.getClassLoader();
    }

    private static <T> T runPrivileged(PrivilegedAction<T> action) {
        return System.getSecurityManager() != null ? AccessController.doPrivileged(action) : action.run();
    }

    public static boolean isClassAvailable(String className) {
        try {
            LoaderUtil.loadClass(className);
            return true;
        }
        catch (ClassNotFoundException | LinkageError e) {
            return false;
        }
        catch (Throwable error) {
            LOGGER.error("Unknown error while checking existence of class `{}`", (Object)className, (Object)error);
            return false;
        }
    }

    public static Class<?> loadClass(String className) throws ClassNotFoundException {
        ClassLoader classLoader;
        ClassLoader classLoader2 = classLoader = LoaderUtil.isIgnoreTccl() ? LoaderUtil.getThisClassLoader() : LoaderUtil.getThreadContextClassLoader();
        if (classLoader == null) {
            classLoader = LoaderUtil.getThisClassLoader();
        }
        return Class.forName(className, true, classLoader);
    }

    public static Class<?> loadClassUnchecked(String className) {
        try {
            return LoaderUtil.loadClass(className);
        }
        catch (ClassNotFoundException e) {
            NoClassDefFoundError error = new NoClassDefFoundError(e.getMessage());
            error.initCause(e);
            throw error;
        }
    }

    public static <T> T newInstanceOf(Class<T> clazz) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Constructor<T> constructor = clazz.getDeclaredConstructor(new Class[0]);
        return constructor.newInstance(new Object[0]);
    }

    public static <T> T newInstanceOfUnchecked(Class<T> clazz) {
        try {
            return LoaderUtil.newInstanceOf(clazz);
        }
        catch (NoSuchMethodException e) {
            NoSuchMethodError error = new NoSuchMethodError(e.getMessage());
            error.initCause(e);
            throw error;
        }
        catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            throw new InternalException(cause);
        }
        catch (InstantiationException e) {
            InstantiationError error = new InstantiationError(e.getMessage());
            error.initCause(e);
            throw error;
        }
        catch (IllegalAccessException e) {
            IllegalAccessError error = new IllegalAccessError(e.getMessage());
            error.initCause(e);
            throw error;
        }
    }

    public static <T> T newInstanceOf(String className) throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        Class clazz = (Class)Cast.cast(LoaderUtil.loadClass(className));
        return LoaderUtil.newInstanceOf(clazz);
    }

    public static <T> T newCheckedInstanceOfProperty(String propertyName, Class<T> clazz) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        return (T)LoaderUtil.newCheckedInstanceOfProperty(propertyName, clazz, () -> null);
    }

    public static <T> T newCheckedInstanceOfProperty(String propertyName, Class<T> clazz, Supplier<T> defaultSupplier) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        String className = PropertiesUtil.getProperties().getStringProperty(propertyName);
        if (className == null) {
            return defaultSupplier.get();
        }
        return LoaderUtil.newCheckedInstanceOf(className, clazz);
    }

    public static <T> T newInstanceOfUnchecked(String className) {
        Class clazz = (Class)Cast.cast(LoaderUtil.loadClassUnchecked(className));
        return LoaderUtil.newInstanceOfUnchecked(clazz);
    }

    public static <T> T newCheckedInstanceOf(String className, Class<T> clazz) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        return LoaderUtil.newInstanceOf(LoaderUtil.loadClass(className).asSubclass(clazz));
    }

    public static <T> T newInstanceOfUnchecked(String className, Class<T> supertype) {
        Class<T> clazz = LoaderUtil.loadClassUnchecked(className).asSubclass(supertype);
        return LoaderUtil.newInstanceOfUnchecked(clazz);
    }

    private static boolean isIgnoreTccl() {
        if (ignoreTCCL == null) {
            String ignoreTccl = PropertiesUtil.getProperties().getStringProperty(IGNORE_TCCL_PROPERTY, null);
            ignoreTCCL = ignoreTccl != null && !"false".equalsIgnoreCase(ignoreTccl.trim());
        }
        return ignoreTCCL;
    }

    public static Collection<URL> findResources(String resource) {
        return LoaderUtil.findResources(resource, true);
    }

    static Collection<URL> findResources(String resource, boolean useTccl) {
        Collection<UrlResource> urlResources = LoaderUtil.findUrlResources(resource, useTccl);
        LinkedHashSet<URL> resources = new LinkedHashSet<URL>(urlResources.size());
        for (UrlResource urlResource : urlResources) {
            resources.add(urlResource.getUrl());
        }
        return resources;
    }

    static Collection<UrlResource> findUrlResources(String resource, boolean useTccl) {
        ClassLoader[] candidates = new ClassLoader[]{useTccl ? LoaderUtil.getThreadContextClassLoader() : null, LoaderUtil.class.getClassLoader(), GET_CLASS_LOADER_DISABLED.getAsBoolean() ? null : ClassLoader.getSystemClassLoader()};
        LinkedHashSet<UrlResource> resources = new LinkedHashSet<UrlResource>();
        for (ClassLoader cl : candidates) {
            if (cl == null) continue;
            try {
                Enumeration<URL> resourceEnum = cl.getResources(resource);
                while (resourceEnum.hasMoreElements()) {
                    resources.add(new UrlResource(cl, resourceEnum.nextElement()));
                }
            }
            catch (IOException error) {
                LOGGER.error("failed to collect resources of name `{}`", (Object)resource, (Object)error);
            }
        }
        return resources;
    }

    static {
        GET_CLASS_LOADER = new RuntimePermission("getClassLoader");
        GET_CLASS_LOADER_DISABLED = new LazyBoolean(() -> {
            if (System.getSecurityManager() == null) {
                return false;
            }
            try {
                AccessController.checkPermission(GET_CLASS_LOADER);
                return false;
            }
            catch (SecurityException ignored) {
                try {
                    AccessController.doPrivileged(() -> {
                        AccessController.checkPermission(GET_CLASS_LOADER);
                        return null;
                    });
                    return false;
                }
                catch (SecurityException ignore) {
                    return true;
                }
            }
        });
        TCCL_GETTER = new ThreadContextClassLoaderGetter();
    }

    static class UrlResource {
        private final ClassLoader classLoader;
        private final URL url;

        UrlResource(ClassLoader classLoader, URL url) {
            this.classLoader = classLoader;
            this.url = url;
        }

        public ClassLoader getClassLoader() {
            return this.classLoader;
        }

        public URL getUrl() {
            return this.url;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof UrlResource)) {
                return false;
            }
            UrlResource that = (UrlResource)o;
            return Objects.equals(this.classLoader, that.classLoader) && Objects.equals(this.url, that.url);
        }

        public int hashCode() {
            return Objects.hashCode(this.classLoader) + Objects.hashCode(this.url);
        }
    }

    private static class ThreadContextClassLoaderGetter
    implements PrivilegedAction<ClassLoader> {
        private ThreadContextClassLoaderGetter() {
        }

        @Override
        public ClassLoader run() {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            if (contextClassLoader != null) {
                return contextClassLoader;
            }
            ClassLoader thisClassLoader = LoaderUtil.getThisClassLoader();
            if (thisClassLoader != null || GET_CLASS_LOADER_DISABLED.getAsBoolean()) {
                return thisClassLoader;
            }
            return ClassLoader.getSystemClassLoader();
        }
    }
}

