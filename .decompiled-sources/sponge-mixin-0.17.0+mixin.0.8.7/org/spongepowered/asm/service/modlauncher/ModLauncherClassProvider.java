/*
 * Decompiled with CFR 0.152.
 */
package org.spongepowered.asm.service.modlauncher;

import cpw.mods.modlauncher.Launcher;
import java.lang.reflect.Method;
import java.net.URL;
import org.spongepowered.asm.service.IClassProvider;

class ModLauncherClassProvider
implements IClassProvider {
    ModLauncherClassProvider() {
    }

    @Override
    @Deprecated
    public URL[] getClassPath() {
        try {
            Class<?> clJava9ClassLoaderUtil = this.findClass("cpw.mods.gross.Java9ClassLoaderUtil");
            Method mdGetSystemClassPathURLs = clJava9ClassLoaderUtil.getDeclaredMethod("getSystemClassPathURLs", new Class[0]);
            return (URL[])mdGetSystemClassPathURLs.invoke(null, new Object[0]);
        }
        catch (ReflectiveOperationException ex) {
            return new URL[0];
        }
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        return Class.forName(name, true, Thread.currentThread().getContextClassLoader());
    }

    @Override
    public Class<?> findClass(String name, boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, Thread.currentThread().getContextClassLoader());
    }

    @Override
    public Class<?> findAgentClass(String name, boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, Launcher.class.getClassLoader());
    }
}

