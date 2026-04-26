/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.classtweaker.api;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import net.fabricmc.loader.impl.lib.classtweaker.api.AccessWidener;
import net.fabricmc.loader.impl.lib.classtweaker.api.InjectedInterface;
import net.fabricmc.loader.impl.lib.classtweaker.api.visitor.ClassTweakerVisitor;
import net.fabricmc.loader.impl.lib.classtweaker.impl.ClassTweakerImpl;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassVisitor;

@ApiStatus.NonExtendable
public interface ClassTweaker
extends ClassTweakerVisitor {
    public static ClassTweaker newInstance() {
        return new ClassTweakerImpl();
    }

    public Set<String> getTargets();

    public AccessWidener getAccessWidener(String var1);

    public List<InjectedInterface> getInjectedInterfaces(String var1);

    public ClassVisitor createClassVisitor(int var1, @Nullable ClassVisitor var2, @Nullable BiConsumer<String, byte[]> var3);
}

