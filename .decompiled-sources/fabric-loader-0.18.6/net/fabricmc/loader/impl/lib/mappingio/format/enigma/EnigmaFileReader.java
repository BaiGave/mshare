/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.mappingio.format.enigma;

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

public final class EnigmaFileReader {
    private EnigmaFileReader() {
    }

    public static void read(Reader reader, MappingVisitor visitor) throws IOException {
        EnigmaFileReader.read(reader, "source", "target", visitor);
    }

    public static void read(Reader reader, String sourceNs, String targetNs, MappingVisitor visitor) throws IOException {
        EnigmaFileReader.read(new ColumnFileReader(reader, '\t', ' '), sourceNs, targetNs, visitor);
    }

    private static void read(ColumnFileReader reader, String sourceNs, String targetNs, MappingVisitor visitor) throws IOException {
        MappingVisitor parentVisitor;
        block9: {
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
                    StringBuilder commentSb = new StringBuilder(200);
                    do {
                        if (!reader.nextCol("CLASS")) continue;
                        EnigmaFileReader.readClass(reader, 0, null, null, commentSb, visitor);
                    } while (reader.nextLine(0));
                }
                if (visitor.visitEnd()) break block9;
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

    private static void readClass(ColumnFileReader reader, int indent, String outerSrcClass, String outerDstClass, StringBuilder commentSb, MappingVisitor visitor) throws IOException {
        String dstInnerName;
        String srcInnerName = reader.nextCol();
        if (srcInnerName == null || srcInnerName.isEmpty()) {
            throw new IOException("missing class-name-a in line " + reader.getLineNumber());
        }
        String srcName = srcInnerName;
        if (outerSrcClass != null && srcInnerName.indexOf(36) < 0) {
            srcName = String.format("%s$%s", outerSrcClass, srcInnerName);
        }
        String dstName = dstInnerName = reader.nextCol();
        if (outerDstClass != null || dstInnerName != null && outerSrcClass != null) {
            if (dstInnerName == null) {
                dstInnerName = srcInnerName;
            }
            if (outerDstClass == null) {
                outerDstClass = outerSrcClass;
            }
            dstName = String.format("%s$%s", outerDstClass, dstInnerName);
        }
        EnigmaFileReader.readClassBody(reader, indent, srcName, dstName, commentSb, visitor);
    }

    private static void readClassBody(ColumnFileReader reader, int indent, String srcClass, String dstClass, StringBuilder commentSb, MappingVisitor visitor) throws IOException {
        boolean visited = false;
        int state = 0;
        while (reader.nextLine(indent + 1)) {
            String dstName;
            if (reader.nextCol("CLASS")) {
                if (!visited || commentSb.length() > 0) {
                    EnigmaFileReader.visitClass(srcClass, dstClass, state, commentSb, visitor);
                    visited = true;
                }
                EnigmaFileReader.readClass(reader, indent + 1, srcClass, dstClass, commentSb, visitor);
                state = 0;
                continue;
            }
            if (reader.nextCol("COMMENT")) {
                EnigmaFileReader.readComment(reader, commentSb);
                continue;
            }
            boolean isMethod = reader.nextCol("METHOD");
            if (!isMethod && !reader.nextCol("FIELD")) continue;
            state = EnigmaFileReader.visitClass(srcClass, dstClass, state, commentSb, visitor);
            visited = true;
            if (state < 0) continue;
            String srcName = reader.nextCol();
            if (srcName == null || srcName.isEmpty()) {
                throw new IOException("missing member-name-a in line " + reader.getLineNumber());
            }
            String dstNameOrSrcDesc = reader.nextCol();
            if (dstNameOrSrcDesc == null || dstNameOrSrcDesc.isEmpty()) {
                throw new IOException("missing member-name-b/member-desc-a in line " + reader.getLineNumber());
            }
            String srcDesc = reader.nextCol();
            if (srcDesc == null) {
                dstName = null;
                srcDesc = dstNameOrSrcDesc;
            } else {
                dstName = dstNameOrSrcDesc;
            }
            if (isMethod && visitor.visitMethod(srcName, srcDesc)) {
                if (dstName != null && !dstName.isEmpty()) {
                    visitor.visitDstName(MappedElementKind.METHOD, 0, dstName);
                }
                EnigmaFileReader.readMethod(reader, indent, commentSb, visitor);
                continue;
            }
            if (isMethod || !visitor.visitField(srcName, srcDesc)) continue;
            if (dstName != null && !dstName.isEmpty()) {
                visitor.visitDstName(MappedElementKind.FIELD, 0, dstName);
            }
            EnigmaFileReader.readElement(reader, MappedElementKind.FIELD, indent, commentSb, visitor);
        }
        if (!visited || commentSb.length() > 0) {
            EnigmaFileReader.visitClass(srcClass, dstClass, state, commentSb, visitor);
        }
    }

    private static int visitClass(String srcClass, String dstClass, int state, StringBuilder commentSb, MappingVisitor visitor) throws IOException {
        if (state == 0) {
            boolean visitContent = visitor.visitClass(srcClass);
            if (visitContent) {
                if (dstClass != null && !dstClass.isEmpty()) {
                    visitor.visitDstName(MappedElementKind.CLASS, 0, dstClass);
                }
                visitContent = visitor.visitElementContent(MappedElementKind.CLASS);
            }
            int n = state = visitContent ? 1 : -1;
            if (commentSb.length() > 0) {
                if (state > 0) {
                    visitor.visitComment(MappedElementKind.CLASS, commentSb.toString());
                }
                commentSb.setLength(0);
            }
        }
        return state;
    }

    private static void readMethod(ColumnFileReader reader, int indent, StringBuilder commentSb, MappingVisitor visitor) throws IOException {
        if (!visitor.visitElementContent(MappedElementKind.METHOD)) {
            return;
        }
        while (reader.nextLine(indent + 2)) {
            if (reader.nextCol("COMMENT")) {
                EnigmaFileReader.readComment(reader, commentSb);
                continue;
            }
            EnigmaFileReader.submitComment(MappedElementKind.METHOD, commentSb, visitor);
            if (!reader.nextCol("ARG")) continue;
            int lvIndex = reader.nextIntCol();
            if (lvIndex < 0) {
                throw new IOException("missing/invalid parameter-lv-index in line " + reader.getLineNumber());
            }
            if (!visitor.visitMethodArg(-1, lvIndex, null)) continue;
            String dstName = reader.nextCol();
            if (dstName != null && !dstName.isEmpty()) {
                visitor.visitDstName(MappedElementKind.METHOD_ARG, 0, dstName);
            }
            EnigmaFileReader.readElement(reader, MappedElementKind.METHOD_ARG, indent, commentSb, visitor);
        }
        EnigmaFileReader.submitComment(MappedElementKind.METHOD, commentSb, visitor);
    }

    private static void readElement(ColumnFileReader reader, MappedElementKind kind, int indent, StringBuilder commentSb, MappingVisitor visitor) throws IOException {
        if (!visitor.visitElementContent(kind)) {
            return;
        }
        while (reader.nextLine(indent + kind.level + 1)) {
            if (!reader.nextCol("COMMENT")) continue;
            EnigmaFileReader.readComment(reader, commentSb);
        }
        EnigmaFileReader.submitComment(kind, commentSb, visitor);
    }

    private static void readComment(ColumnFileReader reader, StringBuilder commentSb) throws IOException {
        String comment;
        if (commentSb.length() > 0) {
            commentSb.append('\n');
        }
        if ((comment = reader.nextCols(true)) != null) {
            commentSb.append(comment);
        }
    }

    private static void submitComment(MappedElementKind kind, StringBuilder commentSb, MappingVisitor visitor) throws IOException {
        if (commentSb.length() == 0) {
            return;
        }
        visitor.visitComment(kind, commentSb.toString());
        commentSb.setLength(0);
    }
}

