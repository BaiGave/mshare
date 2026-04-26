/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.mappingio.adapter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.fabricmc.loader.impl.lib.mappingio.MappedElementKind;
import net.fabricmc.loader.impl.lib.mappingio.MappingVisitor;
import net.fabricmc.loader.impl.lib.mappingio.adapter.ForwardingMappingVisitor;

public final class MappingDstNsReorder
extends ForwardingMappingVisitor {
    private final List<String> newDstNs;
    private int[] nsMap;

    public MappingDstNsReorder(MappingVisitor next, List<String> newDstNs) {
        super(next);
        Objects.requireNonNull(newDstNs, "null newDstNs list");
        this.newDstNs = newDstNs;
    }

    public MappingDstNsReorder(MappingVisitor next, String ... newDstNs) {
        this(next, Arrays.asList(newDstNs));
    }

    @Override
    public void visitNamespaces(String srcNamespace, List<String> dstNamespaces) throws IOException {
        this.nsMap = new int[dstNamespaces.size()];
        for (int i = 0; i < dstNamespaces.size(); ++i) {
            this.nsMap[i] = this.newDstNs.indexOf(dstNamespaces.get(i));
        }
        super.visitNamespaces(srcNamespace, this.newDstNs);
    }

    @Override
    public void visitDstName(MappedElementKind targetKind, int namespace, String name) throws IOException {
        if ((namespace = this.nsMap[namespace]) >= 0) {
            super.visitDstName(targetKind, namespace, name);
        }
    }

    @Override
    public void visitDstDesc(MappedElementKind targetKind, int namespace, String desc) throws IOException {
        if ((namespace = this.nsMap[namespace]) >= 0) {
            super.visitDstDesc(targetKind, namespace, desc);
        }
    }
}

