/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.tinyremapper.api;

import java.util.Collection;
import java.util.function.Predicate;
import net.fabricmc.loader.impl.lib.tinyremapper.api.TrEnvironment;
import net.fabricmc.loader.impl.lib.tinyremapper.api.TrField;
import net.fabricmc.loader.impl.lib.tinyremapper.api.TrMethod;

public interface TrClass {
    public TrEnvironment getEnvironment();

    public String getName();

    public int getAccess();

    public Collection<? extends TrMethod> getMethods();

    public Collection<TrField> getFields(String var1, String var2, boolean var3, Predicate<TrField> var4, Collection<TrField> var5);

    public Collection<TrMethod> getMethods(String var1, String var2, boolean var3, Predicate<TrMethod> var4, Collection<TrMethod> var5);

    public Collection<TrField> resolveFields(String var1, String var2, boolean var3, Predicate<TrField> var4, Collection<TrField> var5);

    public Collection<TrMethod> resolveMethods(String var1, String var2, boolean var3, Predicate<TrMethod> var4, Collection<TrMethod> var5);

    public boolean isAssignableFrom(TrClass var1);

    default public boolean isInterface() {
        return (this.getAccess() & 0x200) != 0;
    }

    default public boolean isRecord() {
        return (this.getAccess() & 0x10000) != 0;
    }

    public boolean isInput();
}

