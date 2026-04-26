/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.mappingio.tree;

import java.util.Collection;
import net.fabricmc.loader.impl.lib.mappingio.tree.MappingTreeView;
import org.jetbrains.annotations.Nullable;

public interface MappingTree
extends MappingTreeView {
    public Collection<? extends ClassMapping> getClasses();

    @Override
    @Nullable
    public ClassMapping getClass(String var1);

    @Override
    @Nullable
    default public ClassMapping getClass(String name, int namespace) {
        return (ClassMapping)MappingTreeView.super.getClass(name, namespace);
    }

    @Override
    @Nullable
    default public FieldMapping getField(String clsName, String name, @Nullable String desc, int namespace) {
        return (FieldMapping)MappingTreeView.super.getField(clsName, name, desc, namespace);
    }

    @Override
    @Nullable
    default public MethodMapping getMethod(String clsName, String name, @Nullable String desc, int namespace) {
        return (MethodMapping)MappingTreeView.super.getMethod(clsName, name, desc, namespace);
    }

    public static interface ClassMapping
    extends ElementMapping,
    MappingTreeView.ClassMappingView {
        public Collection<? extends FieldMapping> getFields();

        @Override
        @Nullable
        public FieldMapping getField(String var1, @Nullable String var2);

        @Override
        @Nullable
        default public FieldMapping getField(String name, @Nullable String desc, int namespace) {
            return (FieldMapping)MappingTreeView.ClassMappingView.super.getField(name, desc, namespace);
        }

        public Collection<? extends MethodMapping> getMethods();

        @Override
        @Nullable
        public MethodMapping getMethod(String var1, @Nullable String var2);

        @Override
        @Nullable
        default public MethodMapping getMethod(String name, @Nullable String desc, int namespace) {
            return (MethodMapping)MappingTreeView.ClassMappingView.super.getMethod(name, desc, namespace);
        }
    }

    public static interface FieldMapping
    extends MemberMapping,
    MappingTreeView.FieldMappingView {
    }

    public static interface MethodMapping
    extends MemberMapping,
    MappingTreeView.MethodMappingView {
        public Collection<? extends MethodArgMapping> getArgs();

        public Collection<? extends MethodVarMapping> getVars();
    }

    public static interface MethodVarMapping
    extends ElementMapping,
    MappingTreeView.MethodVarMappingView {
        public MethodMapping getMethod();
    }

    public static interface MethodArgMapping
    extends ElementMapping,
    MappingTreeView.MethodArgMappingView {
        public MethodMapping getMethod();
    }

    public static interface MemberMapping
    extends ElementMapping,
    MappingTreeView.MemberMappingView {
        @Override
        public ClassMapping getOwner();
    }

    public static interface ElementMapping
    extends MappingTreeView.ElementMappingView {
        @Override
        public MappingTree getTree();

        public void setDstName(String var1, int var2);
    }

    public static interface MetadataEntry
    extends MappingTreeView.MetadataEntryView {
    }
}

