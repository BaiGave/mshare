/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.classtweaker.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import net.fabricmc.loader.impl.lib.classtweaker.api.AccessWidener;
import net.fabricmc.loader.impl.lib.classtweaker.api.ClassTweaker;
import net.fabricmc.loader.impl.lib.classtweaker.api.InjectedInterface;
import net.fabricmc.loader.impl.lib.classtweaker.api.visitor.AccessWidenerVisitor;
import net.fabricmc.loader.impl.lib.classtweaker.classvisitor.AccessWidenerClassVisitor;
import net.fabricmc.loader.impl.lib.classtweaker.classvisitor.InterfaceInjectionClassVisitor;
import net.fabricmc.loader.impl.lib.classtweaker.impl.AccessWidenerImpl;
import net.fabricmc.loader.impl.lib.classtweaker.impl.InjectedInterfaceImpl;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassVisitor;

public final class ClassTweakerImpl
implements ClassTweaker {
    String namespace;
    final Map<String, AccessWidenerImpl> accessWideners = new HashMap<String, AccessWidenerImpl>();
    final Map<String, List<InjectedInterfaceImpl>> injectedInterfaces = new HashMap<String, List<InjectedInterfaceImpl>>();
    final Set<String> targetClasses = new LinkedHashSet<String>();
    final Set<String> classes = new LinkedHashSet<String>();

    @Override
    public void visitHeader(String namespace) {
        if (this.namespace != null && !this.namespace.equals(namespace)) {
            throw new RuntimeException(String.format("Namespace mismatch, expected %s got %s", this.namespace, namespace));
        }
        this.namespace = namespace;
    }

    @Override
    public AccessWidenerVisitor visitAccessWidener(String owner) {
        AccessWidenerImpl accessWidener = this.accessWideners.get(owner);
        if (accessWidener == null) {
            accessWidener = new AccessWidenerImpl(owner);
            this.accessWideners.put(owner, accessWidener);
            this.addTargets(owner);
        }
        return accessWidener;
    }

    @Override
    public void visitInjectedInterface(String owner, String iface, boolean transitive) {
        List injectedInterfaces = this.injectedInterfaces.computeIfAbsent(owner, s -> new ArrayList());
        InjectedInterfaceImpl injectedInterface = new InjectedInterfaceImpl(iface);
        injectedInterfaces.add(injectedInterface);
        this.addTargets(owner);
    }

    private void addTargets(String clazz) {
        this.classes.add(clazz);
        this.targetClasses.add(clazz);
        while (clazz.contains("$")) {
            clazz = clazz.substring(0, clazz.lastIndexOf("$"));
            this.targetClasses.add(clazz);
        }
    }

    @Override
    public ClassVisitor createClassVisitor(int api, @Nullable ClassVisitor classVisitor, @Nullable BiConsumer<String, byte[]> generatedClassConsumer) {
        if (!this.accessWideners.isEmpty()) {
            classVisitor = new AccessWidenerClassVisitor(api, classVisitor, this);
        }
        if (!this.injectedInterfaces.isEmpty()) {
            classVisitor = new InterfaceInjectionClassVisitor(api, classVisitor, this);
        }
        return classVisitor;
    }

    @Override
    public Set<String> getTargets() {
        return Collections.unmodifiableSet(this.targetClasses);
    }

    @Override
    public AccessWidener getAccessWidener(String className) {
        AccessWidenerImpl accessWidener = this.accessWideners.get(className);
        if (accessWidener == null) {
            return AccessWidenerImpl.DEFAULT;
        }
        return accessWidener;
    }

    @Override
    public List<InjectedInterface> getInjectedInterfaces(String className) {
        return Collections.unmodifiableList(this.injectedInterfaces.getOrDefault(className, Collections.emptyList()));
    }

    public int hashCode() {
        return Objects.hash(this.namespace, this.accessWideners, this.targetClasses, this.classes);
    }
}

