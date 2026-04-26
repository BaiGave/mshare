/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.mappingio.tree;

import java.util.Collection;
import net.fabricmc.loader.impl.lib.mappingio.tree.MappingTree;
import net.fabricmc.loader.impl.lib.mappingio.tree.MappingTreeView;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Experimental
public interface HierarchyInfoProvider<T> {
    public String getNamespace();

    @Nullable
    public T getMethodHierarchy(String var1, String var2, @Nullable String var3);

    @Nullable
    default public T getMethodHierarchy(MappingTreeView.MethodMappingView method) {
        int nsId = method.getTree().getNamespaceId(this.getNamespace());
        if (nsId == -2) {
            throw new IllegalArgumentException("disassociated namespace");
        }
        String owner = method.getOwner().getName(nsId);
        String name = method.getName(nsId);
        String desc = method.getDesc(nsId);
        if (owner == null || name == null) {
            return null;
        }
        return this.getMethodHierarchy(owner, name, desc);
    }

    public int getHierarchySize(T var1);

    public Collection<? extends MappingTreeView.MethodMappingView> getHierarchyMethods(T var1, MappingTreeView var2);

    default public Collection<? extends MappingTree.MethodMapping> getHierarchyMethods(T hierarchy, MappingTree tree) {
        return this.getHierarchyMethods(hierarchy, (MappingTreeView)tree);
    }
}

