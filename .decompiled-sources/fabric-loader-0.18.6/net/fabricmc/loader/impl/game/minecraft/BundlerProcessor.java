/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.game.minecraft;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import net.fabricmc.loader.impl.game.LibClassifier;
import net.fabricmc.loader.impl.game.minecraft.BundlerClassPathCapture;
import net.fabricmc.loader.impl.game.minecraft.McLibrary;
import net.fabricmc.loader.impl.game.minecraft.MinecraftGameProvider;
import net.fabricmc.loader.impl.util.LoaderUtil;

final class BundlerProcessor {
    private static final String MAIN_CLASS_PROPERTY = "bundlerMainClass";

    BundlerProcessor() {
    }

    /*
     * Loose catch block
     */
    static void process(LibClassifier<McLibrary> classifier) throws IOException {
        URL[] urls;
        Path bundlerOrigin;
        block19: {
            block20: {
                bundlerOrigin = classifier.getOrigin(McLibrary.MC_BUNDLER);
                String prevProperty = null;
                ClassLoader prevCl = null;
                boolean restorePrev = false;
                try {
                    try (URLClassLoader bundlerCl = new URLClassLoader(new URL[]{bundlerOrigin.toUri().toURL()}, MinecraftGameProvider.class.getClassLoader()){

                        /*
                         * WARNING - Removed try catching itself - possible behaviour change.
                         */
                        @Override
                        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                            Object object = this.getClassLoadingLock(name);
                            synchronized (object) {
                                Class<?> c = this.findLoadedClass(name);
                                if (c == null) {
                                    URL url;
                                    if (name.startsWith("net.minecraft.") && (url = this.getResource(LoaderUtil.getClassFileName(name))) != null) {
                                        try (InputStream is = url.openConnection().getInputStream();){
                                            int len;
                                            byte[] data = new byte[Math.max(is.available() + 1, 1000)];
                                            int offset = 0;
                                            while ((len = is.read(data, offset, data.length - offset)) >= 0) {
                                                if ((offset += len) != data.length) continue;
                                                data = Arrays.copyOf(data, data.length * 2);
                                            }
                                            c = this.defineClass(name, data, 0, offset);
                                        }
                                        catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                    if (c == null) {
                                        c = this.getParent().loadClass(name);
                                    }
                                }
                                if (resolve) {
                                    this.resolveClass(c);
                                }
                                return c;
                            }
                        }
                    };){
                        Class<?> cls = Class.forName(classifier.getClassName(McLibrary.MC_BUNDLER), true, bundlerCl);
                        Method method = cls.getMethod("main", String[].class);
                        prevProperty = System.getProperty(MAIN_CLASS_PROPERTY);
                        prevCl = Thread.currentThread().getContextClassLoader();
                        restorePrev = true;
                        System.setProperty(MAIN_CLASS_PROPERTY, BundlerClassPathCapture.class.getName());
                        Thread.currentThread().setContextClassLoader(bundlerCl);
                        method.invoke(null, new Object[]{new String[0]});
                        urls = BundlerClassPathCapture.FUTURE.get(10L, TimeUnit.SECONDS);
                    }
                    if (!restorePrev) break block19;
                    Thread.currentThread().setContextClassLoader(prevCl);
                    if (prevProperty == null) break block20;
                }
                catch (ClassNotFoundException e) {
                    if (restorePrev) {
                        Thread.currentThread().setContextClassLoader(prevCl);
                        if (prevProperty != null) {
                            System.setProperty(MAIN_CLASS_PROPERTY, prevProperty);
                        } else {
                            System.clearProperty(MAIN_CLASS_PROPERTY);
                        }
                    }
                    return;
                }
                catch (Throwable t) {
                    throw new RuntimeException("Error invoking MC server bundler: " + t, t);
                    {
                        catch (Throwable throwable) {
                            if (restorePrev) {
                                Thread.currentThread().setContextClassLoader(prevCl);
                                if (prevProperty != null) {
                                    System.setProperty(MAIN_CLASS_PROPERTY, prevProperty);
                                } else {
                                    System.clearProperty(MAIN_CLASS_PROPERTY);
                                }
                            }
                            throw throwable;
                        }
                    }
                }
                System.setProperty(MAIN_CLASS_PROPERTY, prevProperty);
                break block19;
            }
            System.clearProperty(MAIN_CLASS_PROPERTY);
        }
        classifier.remove(bundlerOrigin);
        for (URL url : urls) {
            classifier.process(url);
        }
    }
}

