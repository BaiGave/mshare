/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.mappingio;

import java.io.Closeable;
import java.io.IOException;
import net.fabricmc.loader.impl.lib.mappingio.MappingVisitor;

public interface MappingWriter
extends Closeable,
MappingVisitor {
    @Override
    default public boolean visitEnd() throws IOException {
        this.close();
        return true;
    }
}

