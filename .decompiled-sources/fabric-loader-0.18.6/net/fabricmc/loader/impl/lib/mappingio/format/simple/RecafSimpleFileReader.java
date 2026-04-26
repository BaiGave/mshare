/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.mappingio.format.simple;

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

public final class RecafSimpleFileReader {
    private RecafSimpleFileReader() {
    }

    public static void read(Reader reader, MappingVisitor visitor) throws IOException {
        RecafSimpleFileReader.read(reader, "source", "target", visitor);
    }

    public static void read(Reader reader, String sourceNs, String targetNs, MappingVisitor visitor) throws IOException {
        RecafSimpleFileReader.read(new ColumnFileReader(reader, '\t', ' '), sourceNs, targetNs, visitor);
    }

    private static void read(ColumnFileReader reader, String sourceNs, String targetNs, MappingVisitor visitor) throws IOException {
        MappingVisitor parentVisitor;
        block19: {
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
                    boolean visitClass = false;
                    do {
                        String clsSrcName;
                        String line;
                        if ((line = reader.nextCols(true)) == null || line.trim().isEmpty() || line.trim().startsWith("#")) continue;
                        String[] parts = line.split(" ");
                        if (parts.length < 2) {
                            RecafSimpleFileReader.insufficientColumnCount(reader);
                            continue;
                        }
                        int dotPos = parts[0].lastIndexOf(46);
                        String memberSrcName = null;
                        String memberSrcDesc = null;
                        boolean isMethod = false;
                        if (dotPos < 0) {
                            clsSrcName = parts[0];
                            String clsDstName = parts[1];
                            lastClass = clsSrcName;
                            visitClass = visitor.visitClass(clsSrcName);
                            if (!visitClass) continue;
                            visitor.visitDstName(MappedElementKind.CLASS, 0, clsDstName);
                            visitClass = visitor.visitElementContent(MappedElementKind.CLASS);
                            continue;
                        }
                        clsSrcName = parts[0].substring(0, dotPos);
                        if (!clsSrcName.equals(lastClass)) {
                            lastClass = clsSrcName;
                            boolean bl = visitClass = visitor.visitClass(clsSrcName) && visitor.visitElementContent(MappedElementKind.CLASS);
                        }
                        if (!visitClass) continue;
                        String memberIdentifier = parts[0].substring(dotPos + 1);
                        String memberDstName = parts[1];
                        if (parts.length >= 3) {
                            memberSrcName = memberIdentifier;
                            memberSrcDesc = parts[1];
                            memberDstName = parts[2];
                        } else if (parts.length == 2) {
                            int mthDescPos = memberIdentifier.lastIndexOf("(");
                            if (mthDescPos < 0) {
                                memberSrcName = memberIdentifier;
                            } else {
                                isMethod = true;
                                memberSrcName = memberIdentifier.substring(0, mthDescPos);
                                memberSrcDesc = memberIdentifier.substring(mthDescPos);
                            }
                        } else {
                            RecafSimpleFileReader.insufficientColumnCount(reader);
                        }
                        if (!isMethod && visitor.visitField(memberSrcName, memberSrcDesc)) {
                            visitor.visitDstName(MappedElementKind.FIELD, 0, memberDstName);
                            visitor.visitElementContent(MappedElementKind.FIELD);
                            continue;
                        }
                        if (!isMethod || !visitor.visitMethod(memberSrcName, memberSrcDesc)) continue;
                        visitor.visitDstName(MappedElementKind.METHOD, 0, memberDstName);
                        visitor.visitElementContent(MappedElementKind.METHOD);
                    } while (reader.nextLine(0));
                }
                if (visitor.visitEnd()) break block19;
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

    private static void insufficientColumnCount(ColumnFileReader reader) throws IOException {
        throw new IOException("Invalid Recaf Simple line " + reader.getLineNumber() + ": Insufficient column count!");
    }
}

