/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.mappingio;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import net.fabricmc.loader.impl.lib.mappingio.MappingFlag;
import org.jetbrains.annotations.Nullable;

public interface FlatMappingVisitor {
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

    public boolean visitClass(String var1, @Nullable String[] var2) throws IOException;

    public void visitClassComment(String var1, @Nullable String[] var2, String var3) throws IOException;

    public boolean visitField(String var1, String var2, @Nullable String var3, @Nullable String[] var4, @Nullable String[] var5, @Nullable String[] var6) throws IOException;

    public void visitFieldComment(String var1, String var2, @Nullable String var3, @Nullable String[] var4, @Nullable String[] var5, @Nullable String[] var6, String var7) throws IOException;

    public boolean visitMethod(String var1, String var2, @Nullable String var3, @Nullable String[] var4, @Nullable String[] var5, @Nullable String[] var6) throws IOException;

    public void visitMethodComment(String var1, String var2, @Nullable String var3, @Nullable String[] var4, @Nullable String[] var5, @Nullable String[] var6, String var7) throws IOException;

    public boolean visitMethodArg(String var1, String var2, @Nullable String var3, int var4, int var5, @Nullable String var6, @Nullable String[] var7, @Nullable String[] var8, @Nullable String[] var9, String[] var10) throws IOException;

    public void visitMethodArgComment(String var1, String var2, @Nullable String var3, int var4, int var5, @Nullable String var6, @Nullable String[] var7, @Nullable String[] var8, @Nullable String[] var9, @Nullable String[] var10, String var11) throws IOException;

    public boolean visitMethodVar(String var1, String var2, @Nullable String var3, int var4, int var5, int var6, int var7, @Nullable String var8, @Nullable String[] var9, @Nullable String[] var10, @Nullable String[] var11, String[] var12) throws IOException;

    public void visitMethodVarComment(String var1, String var2, @Nullable String var3, int var4, int var5, int var6, int var7, @Nullable String var8, @Nullable String[] var9, @Nullable String[] var10, @Nullable String[] var11, @Nullable String[] var12, String var13) throws IOException;

    default public boolean visitEnd() throws IOException {
        return true;
    }
}

