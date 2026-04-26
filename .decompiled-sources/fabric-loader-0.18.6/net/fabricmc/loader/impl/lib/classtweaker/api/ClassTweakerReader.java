/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.classtweaker.api;

import java.io.BufferedReader;
import java.io.IOException;
import net.fabricmc.loader.impl.lib.classtweaker.api.visitor.ClassTweakerVisitor;
import net.fabricmc.loader.impl.lib.classtweaker.reader.ClassTweakerReaderImpl;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface ClassTweakerReader {
    public static ClassTweakerReader create(ClassTweakerVisitor visitor) {
        return new ClassTweakerReaderImpl(visitor);
    }

    public void read(byte[] var1, String var2);

    public void read(BufferedReader var1, String var2) throws IOException;
}

