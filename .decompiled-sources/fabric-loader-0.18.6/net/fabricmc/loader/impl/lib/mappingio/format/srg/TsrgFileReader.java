/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.mappingio.format.srg;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.fabricmc.loader.impl.lib.mappingio.MappedElementKind;
import net.fabricmc.loader.impl.lib.mappingio.MappingFlag;
import net.fabricmc.loader.impl.lib.mappingio.MappingVisitor;
import net.fabricmc.loader.impl.lib.mappingio.format.ColumnFileReader;
import net.fabricmc.loader.impl.lib.mappingio.format.MappingFormat;

public final class TsrgFileReader {
    private TsrgFileReader() {
    }

    public static List<String> getNamespaces(Reader reader) throws IOException {
        return TsrgFileReader.getNamespaces(new ColumnFileReader(reader, '\t', ' '));
    }

    private static List<String> getNamespaces(ColumnFileReader reader) throws IOException {
        if (reader.nextCol("tsrg2")) {
            String ns;
            ArrayList<String> ret = new ArrayList<String>();
            while ((ns = reader.nextCol()) != null) {
                ret.add(ns);
            }
            return ret;
        }
        return Arrays.asList("source", "target");
    }

    public static void read(Reader reader, MappingVisitor visitor) throws IOException {
        TsrgFileReader.read(reader, "source", "target", visitor);
    }

    public static void read(Reader reader, String sourceNs, String targetNs, MappingVisitor visitor) throws IOException {
        TsrgFileReader.read(new ColumnFileReader(reader, '\t', ' '), sourceNs, targetNs, visitor);
    }

    private static void read(ColumnFileReader reader, String sourceNs, String targetNs, MappingVisitor visitor) throws IOException {
        block22: {
            int markIdx;
            ArrayList<String> nameTmp;
            List<Object> dstNamespaces;
            String srcNamespace;
            MappingFormat format = reader.nextCol("tsrg2") ? MappingFormat.TSRG_2_FILE : MappingFormat.TSRG_FILE;
            boolean readerMarked = false;
            if (format == MappingFormat.TSRG_2_FILE) {
                srcNamespace = reader.nextCol();
                if (srcNamespace == null || srcNamespace.isEmpty()) {
                    throw new IOException("no source namespace in TSRG v2 header");
                }
                dstNamespaces = new ArrayList();
                while (!reader.isAtEol()) {
                    String dstNamespace = reader.nextCol();
                    if (dstNamespace == null || dstNamespace.isEmpty()) {
                        throw new IOException("empty destination namespace in TSRG v2 header");
                    }
                    dstNamespaces.add(dstNamespace);
                }
                reader.nextLine(0);
            } else {
                if (sourceNs == null || sourceNs.isEmpty()) {
                    throw new IllegalArgumentException("provided source namespace must not be null or empty");
                }
                srcNamespace = sourceNs;
                if (targetNs == null || targetNs.isEmpty()) {
                    throw new IllegalArgumentException("provided target namespace must not be null or empty");
                }
                dstNamespaces = Collections.singletonList(targetNs);
            }
            if (visitor.getFlags().contains((Object)MappingFlag.NEEDS_MULTIPLE_PASSES)) {
                reader.mark();
                readerMarked = true;
            }
            int dstNsCount = dstNamespaces.size();
            ArrayList<String> arrayList = nameTmp = dstNamespaces.size() > 1 ? new ArrayList<String>(dstNamespaces.size() - 1) : null;
            while (true) {
                if (visitor.visitHeader()) {
                    visitor.visitNamespaces(srcNamespace, dstNamespaces);
                }
                if (visitor.visitContent()) {
                    String lastClass = null;
                    boolean visitLastClass = false;
                    do {
                        if (reader.hasExtraIndents()) continue;
                        reader.mark();
                        String line = reader.nextCols(false);
                        if ((line == null || line.isEmpty()) && reader.isAtEof()) {
                            reader.discardMark();
                            continue;
                        }
                        reader.reset();
                        reader.discardMark();
                        String[] parts = line.split("((?<= )|(?= ))");
                        if (format != MappingFormat.TSRG_2_FILE && parts.length >= 4 && !parts[3].startsWith("#")) {
                            String dstName;
                            format = MappingFormat.CSRG_FILE;
                            String clsName = parts[0];
                            if (clsName.isEmpty()) {
                                throw new IOException("missing class-name-a in line " + reader.getLineNumber());
                            }
                            if (!clsName.equals(lastClass)) {
                                lastClass = clsName;
                                boolean bl = visitLastClass = visitor.visitClass(clsName) && visitor.visitElementContent(MappedElementKind.CLASS);
                            }
                            if (!visitLastClass) continue;
                            if (parts.length >= 6 && !parts[5].startsWith("#")) {
                                String string = dstName = parts.length == 6 ? null : parts[6];
                                if (dstName == null || dstName.isEmpty() || dstName.startsWith("#")) {
                                    throw new IOException("missing method-name-b in line " + reader.getLineNumber());
                                }
                                if (!visitor.visitMethod(parts[2], parts[4])) continue;
                                visitor.visitDstName(MappedElementKind.METHOD, 0, dstName);
                                visitor.visitElementContent(MappedElementKind.METHOD);
                                continue;
                            }
                            if (parts.length >= 4) {
                                String string = dstName = parts.length == 4 ? null : parts[4];
                                if (dstName == null || dstName.isEmpty() || dstName.startsWith("#")) {
                                    throw new IOException("missing field-name-b in line " + reader.getLineNumber());
                                }
                                if (!visitor.visitField(parts[2], null)) continue;
                                visitor.visitDstName(MappedElementKind.FIELD, 0, dstName);
                                visitor.visitElementContent(MappedElementKind.FIELD);
                                continue;
                            }
                            throw new IllegalStateException("invalid CSRG line: " + line);
                        }
                        String srcName = reader.nextCol();
                        if (srcName == null || srcName.endsWith("/")) continue;
                        if (srcName.isEmpty()) {
                            throw new IOException("missing class-name-a in line " + reader.getLineNumber());
                        }
                        lastClass = srcName;
                        visitLastClass = visitor.visitClass(srcName);
                        if (!visitLastClass) continue;
                        visitLastClass = TsrgFileReader.readClass(reader, format == MappingFormat.TSRG_2_FILE, dstNsCount, nameTmp, visitor);
                    } while (reader.nextLine(0));
                }
                if (visitor.visitEnd()) break block22;
                if (!readerMarked) {
                    throw new IllegalStateException("repeated visitation requested without NEEDS_MULTIPLE_PASSES");
                }
                markIdx = reader.reset();
                assert (markIdx == 1);
            }
        }
    }

    private static boolean readClass(ColumnFileReader reader, boolean isTsrg2, int dstNsCount, List<String> nameTmp, MappingVisitor visitor) throws IOException {
        TsrgFileReader.readDstNames(reader, MappedElementKind.CLASS, 0, dstNsCount, visitor);
        if (!visitor.visitElementContent(MappedElementKind.CLASS)) {
            return false;
        }
        while (reader.nextLine(1)) {
            String desc;
            int offset;
            if (reader.hasExtraIndents()) continue;
            String srcName = reader.nextCol();
            if (srcName == null || srcName.isEmpty()) {
                throw new IOException("missing member-name-a in line " + reader.getLineNumber());
            }
            String arg = reader.nextCol();
            if (arg == null) {
                throw new IOException("missing member-desc-a/member-name-b in line " + reader.getLineNumber());
            }
            if (arg.startsWith("(")) {
                if (!visitor.visitMethod(srcName, arg)) continue;
                TsrgFileReader.readMethod(reader, dstNsCount, visitor);
                continue;
            }
            if (!isTsrg2) {
                if (!visitor.visitField(srcName, null)) continue;
                if (arg.isEmpty()) {
                    throw new IOException("missing field-name-b in line " + reader.getLineNumber());
                }
                visitor.visitDstName(MappedElementKind.FIELD, 0, arg);
                TsrgFileReader.readElement(reader, MappedElementKind.FIELD, 1, dstNsCount, visitor);
                continue;
            }
            for (int i = 0; i < dstNsCount - 1; ++i) {
                String name = reader.nextCol();
                if (name == null) {
                    throw new IOException("missing name columns in line " + reader.getLineNumber());
                }
                if (name.isEmpty()) {
                    throw new IOException("missing field-name-b in line " + reader.getLineNumber());
                }
                nameTmp.add(name);
            }
            String lastName = reader.nextCol();
            if (lastName == null) {
                offset = 1;
                desc = null;
            } else {
                offset = 0;
                desc = arg;
                if (desc.isEmpty()) {
                    throw new IOException("empty field-desc-a in line " + reader.getLineNumber());
                }
            }
            if (visitor.visitField(srcName, desc)) {
                if (lastName == null && !arg.isEmpty()) {
                    visitor.visitDstName(MappedElementKind.FIELD, 0, arg);
                }
                for (int i = 0; i < dstNsCount - 1; ++i) {
                    String name = nameTmp.get(i);
                    if (name.isEmpty()) continue;
                    visitor.visitDstName(MappedElementKind.FIELD, i + offset, name);
                }
                if (lastName != null && !lastName.isEmpty()) {
                    visitor.visitDstName(MappedElementKind.FIELD, dstNsCount - 1, lastName);
                }
                visitor.visitElementContent(MappedElementKind.FIELD);
            }
            if (nameTmp == null) continue;
            nameTmp.clear();
        }
        return true;
    }

    private static void readMethod(ColumnFileReader reader, int dstNsCount, MappingVisitor visitor) throws IOException {
        TsrgFileReader.readDstNames(reader, MappedElementKind.METHOD, 0, dstNsCount, visitor);
        if (!visitor.visitElementContent(MappedElementKind.METHOD)) {
            return;
        }
        while (reader.nextLine(2)) {
            if (reader.hasExtraIndents() || reader.nextCol("static")) continue;
            int lvIndex = reader.nextIntCol();
            if (lvIndex < 0) {
                throw new IOException("missing/invalid parameter-lv-index in line " + reader.getLineNumber());
            }
            String srcName = reader.nextCol();
            if (srcName == null) {
                throw new IOException("missing parameter-name-a column in line " + reader.getLineNumber());
            }
            if (srcName.isEmpty()) {
                srcName = null;
            }
            if (!visitor.visitMethodArg(-1, lvIndex, srcName)) continue;
            TsrgFileReader.readElement(reader, MappedElementKind.METHOD_ARG, 0, dstNsCount, visitor);
        }
    }

    private static void readElement(ColumnFileReader reader, MappedElementKind kind, int dstNsOffset, int dstNsCount, MappingVisitor visitor) throws IOException {
        TsrgFileReader.readDstNames(reader, kind, dstNsOffset, dstNsCount, visitor);
        visitor.visitElementContent(kind);
    }

    private static void readDstNames(ColumnFileReader reader, MappedElementKind subjectKind, int dstNsOffset, int dstNsCount, MappingVisitor visitor) throws IOException {
        for (int dstNs = dstNsOffset; dstNs < dstNsCount; ++dstNs) {
            String name = reader.nextCol();
            if (name == null) {
                throw new IOException("missing name columns in line " + reader.getLineNumber());
            }
            if (name.isEmpty()) {
                throw new IOException("missing destination name in line " + reader.getLineNumber());
            }
            visitor.visitDstName(subjectKind, dstNs, name);
        }
    }
}

