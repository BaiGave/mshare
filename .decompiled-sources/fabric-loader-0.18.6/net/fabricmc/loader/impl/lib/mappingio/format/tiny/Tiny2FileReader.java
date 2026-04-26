/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.mappingio.format.tiny;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.impl.lib.mappingio.MappedElementKind;
import net.fabricmc.loader.impl.lib.mappingio.MappingFlag;
import net.fabricmc.loader.impl.lib.mappingio.MappingVisitor;
import net.fabricmc.loader.impl.lib.mappingio.format.ColumnFileReader;

public final class Tiny2FileReader {
    private Tiny2FileReader() {
    }

    public static List<String> getNamespaces(Reader reader) throws IOException {
        return Tiny2FileReader.getNamespaces(new ColumnFileReader(reader, '\t', '\t'));
    }

    private static List<String> getNamespaces(ColumnFileReader reader) throws IOException {
        String ns;
        if (!reader.nextCol("tiny") || reader.nextIntCol() != 2 || reader.nextIntCol() < 0) {
            throw new IOException("invalid/unsupported tiny file: no tiny 2 header");
        }
        ArrayList<String> ret = new ArrayList<String>();
        while ((ns = reader.nextCol()) != null) {
            ret.add(ns);
        }
        return ret;
    }

    public static void read(Reader reader, MappingVisitor visitor) throws IOException {
        Tiny2FileReader.read(new ColumnFileReader(reader, '\t', '\t'), visitor);
    }

    private static void read(ColumnFileReader reader, MappingVisitor visitor) throws IOException {
        block16: {
            int markIdx;
            if (!reader.nextCol("tiny") || reader.nextIntCol() != 2 || reader.nextIntCol() < 0) {
                throw new IOException("invalid/unsupported tiny file: no tiny 2 header");
            }
            String srcNamespace = reader.nextCol();
            if (srcNamespace == null || srcNamespace.isEmpty()) {
                throw new IOException("no source namespace in Tiny v2 header");
            }
            ArrayList<String> dstNamespaces = new ArrayList<String>();
            while (!reader.isAtEol()) {
                String dstNamespace = reader.nextCol();
                if (dstNamespace == null || dstNamespace.isEmpty()) {
                    throw new IOException("empty destination namespace in Tiny v2 header");
                }
                dstNamespaces.add(dstNamespace);
            }
            int dstNsCount = dstNamespaces.size();
            boolean readerMarked = false;
            if (visitor.getFlags().contains((Object)MappingFlag.NEEDS_MULTIPLE_PASSES)) {
                reader.mark();
                readerMarked = true;
            }
            boolean firstIteration = true;
            boolean escapeNames = false;
            while (true) {
                boolean visitHeader;
                if (visitHeader = visitor.visitHeader()) {
                    visitor.visitNamespaces(srcNamespace, dstNamespaces);
                }
                if (visitHeader || firstIteration) {
                    while (reader.nextLine(1)) {
                        if (!visitHeader) {
                            if (escapeNames || !reader.nextCol("escaped-names")) continue;
                            escapeNames = true;
                            continue;
                        }
                        String key = reader.nextCol();
                        if (key == null) {
                            throw new IOException("missing property key in line " + reader.getLineNumber());
                        }
                        String value = reader.nextCol(true);
                        if (key.equals("escaped-names")) {
                            escapeNames = true;
                        }
                        visitor.visitMetadata(key, value);
                    }
                }
                if (visitor.visitContent()) {
                    while (reader.nextLine(0)) {
                        if (!reader.nextCol("c")) continue;
                        String srcName = reader.nextCol(escapeNames);
                        if (srcName == null || srcName.isEmpty()) {
                            throw new IOException("missing class-name-a in line " + reader.getLineNumber());
                        }
                        if (!visitor.visitClass(srcName)) continue;
                        Tiny2FileReader.readClass(reader, dstNsCount, escapeNames, visitor);
                    }
                }
                if (visitor.visitEnd()) break block16;
                if (!readerMarked) {
                    throw new IllegalStateException("repeated visitation requested without NEEDS_MULTIPLE_PASSES");
                }
                firstIteration = false;
                markIdx = reader.reset();
                assert (markIdx == 1);
            }
        }
    }

    private static void readClass(ColumnFileReader reader, int dstNsCount, boolean escapeNames, MappingVisitor visitor) throws IOException {
        Tiny2FileReader.readDstNames(reader, MappedElementKind.CLASS, dstNsCount, escapeNames, visitor);
        if (!visitor.visitElementContent(MappedElementKind.CLASS)) {
            return;
        }
        while (reader.nextLine(1)) {
            String srcName;
            String srcDesc;
            if (reader.nextCol("f")) {
                srcDesc = reader.nextCol(escapeNames);
                if (srcDesc == null || srcDesc.isEmpty()) {
                    throw new IOException("missing field-desc-a in line " + reader.getLineNumber());
                }
                srcName = reader.nextCol(escapeNames);
                if (srcName == null || srcName.isEmpty()) {
                    throw new IOException("missing field-name-a in line " + reader.getLineNumber());
                }
                if (!visitor.visitField(srcName, srcDesc)) continue;
                Tiny2FileReader.readElement(reader, MappedElementKind.FIELD, dstNsCount, escapeNames, visitor);
                continue;
            }
            if (reader.nextCol("m")) {
                srcDesc = reader.nextCol(escapeNames);
                if (srcDesc == null || srcDesc.isEmpty()) {
                    throw new IOException("missing method-desc-a in line " + reader.getLineNumber());
                }
                srcName = reader.nextCol(escapeNames);
                if (srcName == null || srcName.isEmpty()) {
                    throw new IOException("missing method-name-a in line " + reader.getLineNumber());
                }
                if (!visitor.visitMethod(srcName, srcDesc)) continue;
                Tiny2FileReader.readMethod(reader, dstNsCount, escapeNames, visitor);
                continue;
            }
            if (!reader.nextCol("c")) continue;
            Tiny2FileReader.readComment(reader, MappedElementKind.CLASS, visitor);
        }
    }

    private static void readMethod(ColumnFileReader reader, int dstNsCount, boolean escapeNames, MappingVisitor visitor) throws IOException {
        Tiny2FileReader.readDstNames(reader, MappedElementKind.METHOD, dstNsCount, escapeNames, visitor);
        if (!visitor.visitElementContent(MappedElementKind.METHOD)) {
            return;
        }
        while (reader.nextLine(2)) {
            int lvIndex;
            if (reader.nextCol("p")) {
                lvIndex = reader.nextIntCol();
                if (lvIndex < 0) {
                    throw new IOException("missing/invalid parameter-lv-index in line " + reader.getLineNumber());
                }
                String srcName = reader.nextCol(escapeNames);
                if (srcName == null) {
                    throw new IOException("missing parameter-name-a column in line " + reader.getLineNumber());
                }
                if (srcName.isEmpty()) {
                    srcName = null;
                }
                if (!visitor.visitMethodArg(-1, lvIndex, srcName)) continue;
                Tiny2FileReader.readElement(reader, MappedElementKind.METHOD_ARG, dstNsCount, escapeNames, visitor);
                continue;
            }
            if (reader.nextCol("v")) {
                lvIndex = reader.nextIntCol();
                if (lvIndex < 0) {
                    throw new IOException("missing/invalid variable-lv-index in line " + reader.getLineNumber());
                }
                int startOpIdx = reader.nextIntCol();
                if (startOpIdx < 0) {
                    throw new IOException("missing/invalid variable-lv-start-offset in line " + reader.getLineNumber());
                }
                int lvtRowIndex = reader.nextIntCol();
                String srcName = reader.nextCol(escapeNames);
                if (srcName == null) {
                    throw new IOException("missing variable-name-a column in line " + reader.getLineNumber());
                }
                if (srcName.isEmpty()) {
                    srcName = null;
                }
                if (!visitor.visitMethodVar(lvtRowIndex, lvIndex, startOpIdx, -1, srcName)) continue;
                Tiny2FileReader.readElement(reader, MappedElementKind.METHOD_VAR, dstNsCount, escapeNames, visitor);
                continue;
            }
            if (!reader.nextCol("c")) continue;
            Tiny2FileReader.readComment(reader, MappedElementKind.METHOD, visitor);
        }
    }

    private static void readElement(ColumnFileReader reader, MappedElementKind kind, int dstNsCount, boolean escapeNames, MappingVisitor visitor) throws IOException {
        Tiny2FileReader.readDstNames(reader, kind, dstNsCount, escapeNames, visitor);
        if (!visitor.visitElementContent(kind)) {
            return;
        }
        while (reader.nextLine(kind.level + 1)) {
            if (!reader.nextCol("c")) continue;
            Tiny2FileReader.readComment(reader, kind, visitor);
        }
    }

    private static void readComment(ColumnFileReader reader, MappedElementKind subjectKind, MappingVisitor visitor) throws IOException {
        String comment = reader.nextCol(true);
        if (comment == null) {
            throw new IOException("missing comment in line " + reader.getLineNumber());
        }
        visitor.visitComment(subjectKind, comment);
    }

    private static void readDstNames(ColumnFileReader reader, MappedElementKind subjectKind, int dstNsCount, boolean escapeNames, MappingVisitor visitor) throws IOException {
        for (int dstNs = 0; dstNs < dstNsCount; ++dstNs) {
            String name = reader.nextCol(escapeNames);
            if (name == null) {
                throw new IOException("missing name columns in line " + reader.getLineNumber());
            }
            if (name.isEmpty()) continue;
            visitor.visitDstName(subjectKind, dstNs, name);
        }
    }
}

