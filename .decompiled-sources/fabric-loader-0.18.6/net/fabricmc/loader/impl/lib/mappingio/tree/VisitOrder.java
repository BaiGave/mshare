/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.mappingio.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import net.fabricmc.loader.impl.lib.mappingio.tree.AlphanumericComparator;
import net.fabricmc.loader.impl.lib.mappingio.tree.MappingTreeView;

public final class VisitOrder {
    private static final AlphanumericComparator ALPHANUM = new AlphanumericComparator(Locale.ROOT);
    private Comparator<MappingTreeView.ClassMappingView> classComparator;
    private Comparator<MappingTreeView.FieldMappingView> fieldComparator;
    private Comparator<MappingTreeView.MethodMappingView> methodComparator;
    private Comparator<MappingTreeView.MethodArgMappingView> methodArgComparator;
    private Comparator<MappingTreeView.MethodVarMappingView> methodVarComparator;
    private boolean methodsFirst;
    private boolean methodVarsFirst;

    private VisitOrder() {
    }

    public static VisitOrder createByInputOrder() {
        return new VisitOrder();
    }

    public <T extends MappingTreeView.ClassMappingView> Collection<T> sortClasses(Collection<T> classes) {
        return VisitOrder.sort(classes, this.classComparator);
    }

    public <T extends MappingTreeView.FieldMappingView> Collection<T> sortFields(Collection<T> fields) {
        return VisitOrder.sort(fields, this.fieldComparator);
    }

    public <T extends MappingTreeView.MethodMappingView> Collection<T> sortMethods(Collection<T> methods) {
        return VisitOrder.sort(methods, this.methodComparator);
    }

    public <T extends MappingTreeView.MethodArgMappingView> Collection<T> sortMethodArgs(Collection<T> args) {
        return VisitOrder.sort(args, this.methodArgComparator);
    }

    public <T extends MappingTreeView.MethodVarMappingView> Collection<T> sortMethodVars(Collection<T> vars) {
        return VisitOrder.sort(vars, this.methodVarComparator);
    }

    private static <T> Collection<T> sort(Collection<T> inputs, Comparator<? super T> comparator) {
        if (comparator == null || inputs.size() < 2) {
            return inputs;
        }
        ArrayList<T> ret = new ArrayList<T>(inputs);
        ret.sort(comparator);
        return ret;
    }

    public boolean isMethodsFirst() {
        return this.methodsFirst;
    }

    public boolean isMethodVarsFirst() {
        return this.methodVarsFirst;
    }
}

