/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.mappingio.format.jobf;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.Set;
import net.fabricmc.loader.impl.lib.mappingio.MappedElementKind;
import net.fabricmc.loader.impl.lib.mappingio.MappingFlag;
import net.fabricmc.loader.impl.lib.mappingio.MappingVisitor;
import net.fabricmc.loader.impl.lib.mappingio.format.ColumnFileReader;
import net.fabricmc.loader.impl.lib.mappingio.tree.MappingTree;
import net.fabricmc.loader.impl.lib.mappingio.tree.MemoryMappingTree;

public class JobfFileReader {
    private JobfFileReader() {
    }

    public static void read(Reader reader, MappingVisitor visitor) throws IOException {
        JobfFileReader.read(reader, "source", "target", visitor);
    }

    public static void read(Reader reader, String sourceNs, String targetNs, MappingVisitor visitor) throws IOException {
        JobfFileReader.read(new ColumnFileReader(reader, '\t', ' '), sourceNs, targetNs, visitor);
    }

    private static void read(ColumnFileReader reader, String sourceNs, String targetNs, MappingVisitor visitor) throws IOException {
        MappingVisitor parentVisitor;
        block18: {
            int markIdx;
            Set<MappingFlag> flags = visitor.getFlags();
            parentVisitor = null;
            boolean readerMarked = false;
            if (flags.contains((Object)MappingFlag.NEEDS_ELEMENT_UNIQUENESS)) {
                parentVisitor = visitor;
                visitor = new MemoryMappingTree();
            } else if (flags.contains((Object)MappingFlag.NEEDS_MULTIPLE_PASSES)) {
                reader.mark();
                readerMarked = true;
            }
            while (true) {
                if (visitor.visitHeader()) {
                    visitor.visitNamespaces(sourceNs, Collections.singletonList(targetNs));
                }
                if (visitor.visitContent()) {
                    String lastClass = null;
                    boolean visitLastClass = false;
                    do {
                        if (reader.nextCol("c")) {
                            String srcName = reader.nextCol();
                            if (srcName == null || srcName.isEmpty()) {
                                throw new IOException("missing class-name-a in line " + reader.getLineNumber());
                            }
                            lastClass = srcName = srcName.replace('.', '/');
                            visitLastClass = visitor.visitClass(srcName);
                            if (!visitLastClass) continue;
                            JobfFileReader.readSeparator(reader);
                            String dstName = reader.nextCol();
                            if (dstName == null || dstName.isEmpty()) {
                                throw new IOException("missing class-name-b in line " + reader.getLineNumber());
                            }
                            String pkg = srcName.substring(0, srcName.lastIndexOf(47) + 1);
                            dstName = pkg + dstName;
                            visitor.visitDstName(MappedElementKind.CLASS, 0, dstName);
                            visitLastClass = visitor.visitElementContent(MappedElementKind.CLASS);
                            continue;
                        }
                        boolean isField = reader.nextCol("f");
                        if (isField || reader.nextCol("m")) {
                            String src = reader.nextCol();
                            if (src == null || src.isEmpty()) {
                                throw new IOException("missing class-/name-/desc-a in line " + reader.getLineNumber());
                            }
                            int nameSepPos = src.lastIndexOf(46);
                            if (nameSepPos <= 0 || nameSepPos == src.length() - 1) {
                                throw new IOException("invalid class-/name-/desc-a in line " + reader.getLineNumber());
                            }
                            int descSepPos = src.lastIndexOf(isField ? 58 : 40);
                            if (descSepPos <= 0 || descSepPos == src.length() - 1) {
                                throw new IOException("invalid name-/desc-a in line " + reader.getLineNumber());
                            }
                            JobfFileReader.readSeparator(reader);
                            String dstName = reader.nextCol();
                            if (dstName == null || dstName.isEmpty()) {
                                throw new IOException("missing name-b in line " + reader.getLineNumber());
                            }
                            String srcOwner = src.substring(0, nameSepPos).replace('.', '/');
                            if (!srcOwner.equals(lastClass)) {
                                lastClass = srcOwner;
                                boolean bl = visitLastClass = visitor.visitClass(srcOwner) && visitor.visitElementContent(MappedElementKind.CLASS);
                            }
                            if (!visitLastClass) continue;
                            String srcName = src.substring(nameSepPos + 1, descSepPos);
                            String srcDesc = src.substring(descSepPos + (isField ? 1 : 0));
                            if ((!isField || !visitor.visitField(srcName, srcDesc)) && (isField || !visitor.visitMethod(srcName, srcDesc))) continue;
                            MappedElementKind kind = isField ? MappedElementKind.FIELD : MappedElementKind.METHOD;
                            visitor.visitDstName(kind, 0, dstName);
                            visitor.visitElementContent(kind);
                            continue;
                        }
                        if (!reader.nextCol("p")) continue;
                    } while (reader.nextLine(0));
                }
                if (visitor.visitEnd()) break block18;
                if (!readerMarked) {
                    throw new IllegalStateException("repeated visitation requested without NEEDS_MULTIPLE_PASSES");
                }
                markIdx = reader.reset();
                assert (markIdx == 1);
            }
        }
        if (parentVisitor != null) {
            ((MappingTree)((Object)visitor)).accept(parentVisitor);
        }
    }

    private static void readSeparator(ColumnFileReader reader) throws IOException {
        if (!reader.nextCol("=")) {
            throw new IOException("missing separator in line " + reader.getLineNumber() + " (expected \" = \")");
        }
    }
}

