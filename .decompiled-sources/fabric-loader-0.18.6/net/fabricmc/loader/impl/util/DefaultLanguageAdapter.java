/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.api.LanguageAdapterException;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;

public final class DefaultLanguageAdapter
implements LanguageAdapter {
    public static final DefaultLanguageAdapter INSTANCE = new DefaultLanguageAdapter();

    private DefaultLanguageAdapter() {
    }

    @Override
    public <T> T create(ModContainer mod, String value, Class<T> type) throws LanguageAdapterException {
        MethodHandle handle;
        Class<?> c;
        String[] methodSplit = value.split("::");
        if (methodSplit.length >= 3) {
            throw new LanguageAdapterException("Invalid handle format: " + value);
        }
        try {
            c = Class.forName(methodSplit[0], true, FabricLauncherBase.getLauncher().getTargetClassLoader());
        }
        catch (ClassNotFoundException e) {
            throw new LanguageAdapterException(e);
        }
        if (methodSplit.length == 1) {
            if (type.isAssignableFrom(c)) {
                try {
                    return (T)c.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
                }
                catch (Exception e) {
                    throw new LanguageAdapterException(e);
                }
            }
            throw new LanguageAdapterException("Class " + c.getName() + " cannot be cast to " + type.getName() + "!");
        }
        ArrayList<Method> methodList = new ArrayList<Method>();
        for (Method m : c.getDeclaredMethods()) {
            if (!m.getName().equals(methodSplit[1])) continue;
            methodList.add(m);
        }
        try {
            Field field = c.getDeclaredField(methodSplit[1]);
            Class<?> fType = field.getType();
            if ((field.getModifiers() & 8) == 0) {
                throw new LanguageAdapterException("Field " + value + " must be static!");
            }
            if (!methodList.isEmpty()) {
                throw new LanguageAdapterException("Ambiguous " + value + " - refers to both field and method!");
            }
            if (!type.isAssignableFrom(fType)) {
                throw new LanguageAdapterException("Field " + value + " cannot be cast to " + type.getName() + "!");
            }
            return (T)field.get(null);
        }
        catch (NoSuchFieldException field) {
        }
        catch (IllegalAccessException e) {
            throw new LanguageAdapterException("Field " + value + " cannot be accessed!", e);
        }
        if (!type.isInterface()) {
            throw new LanguageAdapterException("Cannot proxy method " + value + " to non-interface type " + type.getName() + "!");
        }
        if (methodList.isEmpty()) {
            throw new LanguageAdapterException("Could not find " + value + "!");
        }
        if (methodList.size() >= 2) {
            throw new LanguageAdapterException("Found multiple method entries of name " + value + "!");
        }
        Method targetMethod = (Method)methodList.get(0);
        Object object = null;
        if ((targetMethod.getModifiers() & 8) == 0) {
            try {
                object = c.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
            }
            catch (Exception e) {
                throw new LanguageAdapterException(e);
            }
        }
        try {
            handle = MethodHandles.lookup().unreflect(targetMethod);
        }
        catch (Exception ex) {
            throw new LanguageAdapterException(ex);
        }
        if (object != null) {
            handle = handle.bindTo(object);
        }
        try {
            return MethodHandleProxies.asInterfaceInstance(type, handle);
        }
        catch (Exception ex) {
            throw new LanguageAdapterException(ex);
        }
    }
}

