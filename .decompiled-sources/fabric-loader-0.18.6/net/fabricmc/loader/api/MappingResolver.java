/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.api;

import java.util.Collection;

public interface MappingResolver {
    public Collection<String> getNamespaces();

    public String getCurrentRuntimeNamespace();

    public String mapClassName(String var1, String var2);

    public String unmapClassName(String var1, String var2);

    public String mapFieldName(String var1, String var2, String var3, String var4);

    public String mapMethodName(String var1, String var2, String var3, String var4);
}

