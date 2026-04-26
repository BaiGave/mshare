/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.mappingio;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import net.fabricmc.loader.impl.lib.mappingio.MappedElementKind;
import net.fabricmc.loader.impl.lib.mappingio.MappingFlag;
import org.jetbrains.annotations.Nullable;

public interface MappingVisitor {
    default public Set<MappingFlag> getFlags() {
        return MappingFlag.NONE;
    }

    default public void reset() {
        throw new UnsupportedOperationException();
    }

    default public boolean visitHeader() throws IOException {
        return true;
    }

    public void visitNamespaces(String var1, List<String> var2) throws IOException;

    default public void visitMetadata(String key, @Nullable String value) throws IOException {
    }

    default public boolean visitContent() throws IOException {
        return true;
    }

    public boolean visitClass(String var1) throws IOException;

    public boolean visitField(String var1, @Nullable String var2) throws IOException;

    public boolean visitMethod(String var1, @Nullable String var2) throws IOException;

    public boolean visitMethodArg(int var1, int var2, @Nullable String var3) throws IOException;

    public boolean visitMethodVar(int var1, int var2, int var3, int var4, @Nullable String var5) throws IOException;

    default public boolean visitEnd() throws IOException {
        return true;
    }

    public void visitDstName(MappedElementKind var1, int var2, String var3) throws IOException;

    default public void visitDstDesc(MappedElementKind targetKind, int namespace, String desc) throws IOException {
    }

    default public boolean visitElementContent(MappedElementKind targetKind) throws IOException {
        return true;
    }

    public void visitComment(MappedElementKind var1, String var2) throws IOException;
}

