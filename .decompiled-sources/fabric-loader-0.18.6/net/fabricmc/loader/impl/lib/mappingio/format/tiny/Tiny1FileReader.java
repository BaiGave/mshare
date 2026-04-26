/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.mappingio.format.tiny;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.fabricmc.loader.impl.lib.mappingio.MappedElementKind;
import net.fabricmc.loader.impl.lib.mappingio.MappingFlag;
import net.fabricmc.loader.impl.lib.mappingio.MappingVisitor;
import net.fabricmc.loader.impl.lib.mappingio.format.ColumnFileReader;
import net.fabricmc.loader.impl.lib.mappingio.tree.MappingTree;
import net.fabricmc.loader.impl.lib.mappingio.tree.MemoryMappingTree;

public final class Tiny1FileReader {
    private Tiny1FileReader() {
    }

    public static List<String> getNamespaces(Reader reader) throws IOException {
        return Tiny1FileReader.getNamespaces(new ColumnFileReader(reader, '\t', '\t'));
    }

    private static List<String> getNamespaces(ColumnFileReader reader) throws IOException {
        String ns;
        if (!reader.nextCol("v1")) {
            throw new IOException("invalid/unsupported tiny file: no tiny 1 header");
        }
        ArrayList<String> ret = new ArrayList<String>();
        while ((ns = reader.nextCol()) != null) {
            ret.add(ns);
        }
        return ret;
    }

    public static void read(Reader reader, MappingVisitor visitor) throws IOException {
        Tiny1FileReader.read(new ColumnFileReader(reader, '\t', '\t'), visitor);
    }

    private static void read(ColumnFileReader reader, MappingVisitor visitor) throws IOException {
        MappingVisitor parentVisitor;
        block30: {
            int markIdx;
            if (!reader.nextCol("v1")) {
                throw new IOException("invalid/unsupported tiny file: no tiny 1 header");
            }
            String srcNamespace = reader.nextCol();
            if (srcNamespace == null || srcNamespace.isEmpty()) {
                throw new IOException("no source namespace in Tiny v1 header");
            }
            ArrayList<String> dstNamespaces = new ArrayList<String>();
            while (!reader.isAtEol()) {
                String dstNamespace = reader.nextCol();
                if (dstNamespace == null || dstNamespace.isEmpty()) {
                    throw new IOException("empty destination namespace in Tiny v1 header");
                }
                dstNamespaces.add(dstNamespace);
            }
            int dstNsCount = dstNamespaces.size();
            Set<MappingFlag> flags = visitor.getFlags();
            parentVisitor = null;
            boolean readerMarked = false;
            if (flags.contains((Object)MappingFlag.NEEDS_ELEMENT_UNIQUENESS) || flags.contains((Object)MappingFlag.NEEDS_HEADER_METADATA)) {
                parentVisitor = visitor;
                visitor = new MemoryMappingTree();
            } else if (flags.contains((Object)MappingFlag.NEEDS_MULTIPLE_PASSES)) {
                reader.mark();
                readerMarked = true;
            }
            while (true) {
                if (visitor.visitHeader()) {
                    visitor.visitNamespaces(srcNamespace, dstNamespaces);
                }
                if (visitor.visitContent()) {
                    String lastClass = null;
                    boolean visitLastClass = false;
                    while (reader.nextLine(0)) {
                        String[] parts;
                        if (reader.nextCol("CLASS")) {
                            String srcName = reader.nextCol();
                            if (srcName == null || srcName.isEmpty()) {
                                throw new IOException("missing class-name-a in line " + reader.getLineNumber());
                            }
                            lastClass = srcName;
                            visitLastClass = visitor.visitClass(srcName);
                            if (!visitLastClass) continue;
                            Tiny1FileReader.readDstNames(reader, MappedElementKind.CLASS, dstNsCount, visitor);
                            visitLastClass = visitor.visitElementContent(MappedElementKind.CLASS);
                            continue;
                        }
                        boolean isMethod = reader.nextCol("METHOD");
                        if (isMethod || reader.nextCol("FIELD")) {
                            String srcOwner = reader.nextCol();
                            if (srcOwner == null || srcOwner.isEmpty()) {
                                throw new IOException("missing class-name-a in line " + reader.getLineNumber());
                            }
                            if (!srcOwner.equals(lastClass)) {
                                lastClass = srcOwner;
                                boolean bl = visitLastClass = visitor.visitClass(srcOwner) && visitor.visitElementContent(MappedElementKind.CLASS);
                            }
                            if (!visitLastClass) continue;
                            String srcDesc = reader.nextCol();
                            if (srcDesc == null || srcDesc.isEmpty()) {
                                throw new IOException("missing member-desc-a in line " + reader.getLineNumber());
                            }
                            String srcName = reader.nextCol();
                            if (srcName == null || srcName.isEmpty()) {
                                throw new IOException("missing member-name-a in line " + reader.getLineNumber());
                            }
                            if ((!isMethod || !visitor.visitMethod(srcName, srcDesc)) && (isMethod || !visitor.visitField(srcName, srcDesc))) continue;
                            MappedElementKind kind = isMethod ? MappedElementKind.METHOD : MappedElementKind.FIELD;
                            Tiny1FileReader.readDstNames(reader, kind, dstNsCount, visitor);
                            visitor.visitElementContent(kind);
                            continue;
                        }
                        String line = reader.nextCol();
                        String prefix = "# INTERMEDIARY-COUNTER ";
                        if (!line.startsWith("# INTERMEDIARY-COUNTER ") || (parts = line.substring("# INTERMEDIARY-COUNTER ".length()).split(" ")).length != 2) continue;
                        String property = null;
                        switch (parts[0]) {
                            case "class": {
                                property = "next-intermediary-class";
                                break;
                            }
                            case "field": {
                                property = "next-intermediary-field";
                                break;
                            }
                            case "method": {
                                property = "next-intermediary-method";
                            }
                        }
                        if (property == null) continue;
                        visitor.visitMetadata(property, parts[1]);
                    }
                }
                if (visitor.visitEnd()) break block30;
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

    private static void readDstNames(ColumnFileReader reader, MappedElementKind subjectKind, int dstNsCount, MappingVisitor visitor) throws IOException {
        for (int dstNs = 0; dstNs < dstNsCount; ++dstNs) {
            String name = reader.nextCol();
            if (name == null) {
                throw new IOException("missing name columns in line " + reader.getLineNumber());
            }
            if (name.isEmpty()) continue;
            visitor.visitDstName(subjectKind, dstNs, name);
        }
    }
}

