/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.mappingio.tree;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import net.fabricmc.loader.impl.lib.mappingio.MappingVisitor;
import net.fabricmc.loader.impl.lib.mappingio.tree.VisitOrder;
import org.jetbrains.annotations.Nullable;

public interface MappingTreeView {
    @Nullable
    public String getSrcNamespace();

    public List<String> getDstNamespaces();

    default public int getNamespaceId(String namespace) {
        if (namespace.equals(this.getSrcNamespace())) {
            return -1;
        }
        int ret = this.getDstNamespaces().indexOf(namespace);
        return ret >= 0 ? ret : -2;
    }

    public Collection<? extends ClassMappingView> getClasses();

    @Nullable
    public ClassMappingView getClass(String var1);

    @Nullable
    default public ClassMappingView getClass(String name, int namespace) {
        if (namespace < 0) {
            return this.getClass(name);
        }
        for (ClassMappingView classMappingView : this.getClasses()) {
            if (!name.equals(classMappingView.getDstName(namespace))) continue;
            return classMappingView;
        }
        return null;
    }

    @Nullable
    default public FieldMappingView getField(String clsName, String name, @Nullable String desc, int namespace) {
        ClassMappingView owner = this.getClass(clsName, namespace);
        return owner != null ? owner.getField(name, desc, namespace) : null;
    }

    @Nullable
    default public MethodMappingView getMethod(String clsName, String name, @Nullable String desc, int namespace) {
        ClassMappingView owner = this.getClass(clsName, namespace);
        return owner != null ? owner.getMethod(name, desc, namespace) : null;
    }

    default public void accept(MappingVisitor visitor) throws IOException {
        this.accept(visitor, VisitOrder.createByInputOrder());
    }

    public void accept(MappingVisitor var1, VisitOrder var2) throws IOException;

    default public String mapClassName(String name, int srcNamespace, int dstNamespace) {
        if (!1.$assertionsDisabled && name.indexOf(46) >= 0) {
            throw new AssertionError();
        }
        if (srcNamespace == dstNamespace) {
            return name;
        }
        ClassMappingView cls = this.getClass(name, srcNamespace);
        if (cls == null) {
            return name;
        }
        String ret = cls.getName(dstNamespace);
        return ret != null ? ret : name;
    }

    default public String mapDesc(CharSequence desc, int namespace) {
        return this.mapDesc(desc, 0, desc.length(), -1, namespace);
    }

    default public String mapDesc(CharSequence desc, int srcNamespace, int dstNamespace) {
        return this.mapDesc(desc, 0, desc.length(), srcNamespace, dstNamespace);
    }

    default public String mapDesc(CharSequence desc, int start, int end, int srcNamespace, int dstNamespace) {
        if (srcNamespace == dstNamespace) {
            return desc.subSequence(start, end).toString();
        }
        StringBuilder ret = null;
        int copyOffset = start;
        int offset = start;
        while (offset < end) {
            int idEnd;
            char c;
            if ((c = desc.charAt(offset++)) != 'L') continue;
            for (idEnd = offset; idEnd < end && (c = desc.charAt(idEnd)) != ';'; ++idEnd) {
            }
            if (idEnd >= end) {
                throw new IllegalArgumentException("invalid descriptor: " + desc.subSequence(start, end));
            }
            String cls = desc.subSequence(offset, idEnd).toString();
            String mappedCls = this.mapClassName(cls, srcNamespace, dstNamespace);
            if (mappedCls != null && !mappedCls.equals(cls)) {
                if (ret == null) {
                    ret = new StringBuilder(end - start);
                }
                ret.append(desc, copyOffset, offset);
                ret.append(mappedCls);
                copyOffset = idEnd;
            }
            offset = idEnd + 1;
        }
        if (ret == null) {
            return desc.subSequence(start, end).toString();
        }
        ret.append(desc, copyOffset, end);
        return ret.toString();
    }

    static {
        if (1.$assertionsDisabled) {
            // empty if block
        }
    }

    public static interface ClassMappingView
    extends ElementMappingView {
        public Collection<? extends FieldMappingView> getFields();

        @Nullable
        public FieldMappingView getField(String var1, @Nullable String var2);

        @Nullable
        default public FieldMappingView getField(String name, @Nullable String desc, int namespace) {
            if (namespace < 0) {
                return this.getField(name, desc);
            }
            for (FieldMappingView fieldMappingView : this.getFields()) {
                String mDesc;
                if (!name.equals(fieldMappingView.getDstName(namespace)) || desc != null && (mDesc = fieldMappingView.getDesc(namespace)) != null && !desc.equals(mDesc)) continue;
                return fieldMappingView;
            }
            return null;
        }

        public Collection<? extends MethodMappingView> getMethods();

        @Nullable
        public MethodMappingView getMethod(String var1, @Nullable String var2);

        @Nullable
        default public MethodMappingView getMethod(String name, @Nullable String desc, int namespace) {
            if (namespace < 0) {
                return this.getMethod(name, desc);
            }
            for (MethodMappingView methodMappingView : this.getMethods()) {
                String mDesc;
                if (!name.equals(methodMappingView.getDstName(namespace)) || desc != null && (mDesc = methodMappingView.getDesc(namespace)) != null && !desc.equals(mDesc) && (!desc.endsWith(")") || !mDesc.startsWith(desc))) continue;
                return methodMappingView;
            }
            return null;
        }
    }

    public static interface FieldMappingView
    extends MemberMappingView {
    }

    public static interface MethodMappingView
    extends MemberMappingView {
    }

    public static interface MethodVarMappingView
    extends ElementMappingView {
        public int getLvtRowIndex();

        public int getLvIndex();

        public int getStartOpIdx();

        public int getEndOpIdx();
    }

    public static interface MethodArgMappingView
    extends ElementMappingView {
        public int getArgPosition();

        public int getLvIndex();
    }

    public static interface MemberMappingView
    extends ElementMappingView {
        public ClassMappingView getOwner();

        @Nullable
        public String getSrcDesc();

        @Nullable
        default public String getDesc(int namespace) {
            String srcDesc = this.getSrcDesc();
            if (namespace < 0 || srcDesc == null) {
                return srcDesc;
            }
            return this.getTree().mapDesc(srcDesc, namespace);
        }
    }

    public static interface ElementMappingView {
        public MappingTreeView getTree();

        public String getSrcName();

        @Nullable
        public String getDstName(int var1);

        @Nullable
        default public String getName(int namespace) {
            if (namespace < 0) {
                return this.getSrcName();
            }
            return this.getDstName(namespace);
        }

        @Nullable
        default public String getName(String namespace) {
            int nsId = this.getTree().getNamespaceId(namespace);
            if (nsId == -2) {
                return null;
            }
            return this.getName(nsId);
        }

        @Nullable
        public String getComment();
    }

    public static interface MetadataEntryView {
        public String getKey();

        @Nullable
        public String getValue();
    }
}

