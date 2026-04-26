/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.mappingio.format.srg;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.Set;
import net.fabricmc.loader.impl.lib.mappingio.MappedElementKind;
import net.fabricmc.loader.impl.lib.mappingio.MappingFlag;
import net.fabricmc.loader.impl.lib.mappingio.MappingVisitor;
import net.fabricmc.loader.impl.lib.mappingio.format.ColumnFileReader;
import net.fabricmc.loader.impl.lib.mappingio.format.MappingFormat;
import net.fabricmc.loader.impl.lib.mappingio.tree.MappingTree;
import net.fabricmc.loader.impl.lib.mappingio.tree.MemoryMappingTree;

public final class SrgFileReader {
    private SrgFileReader() {
    }

    public static void read(Reader reader, MappingVisitor visitor) throws IOException {
        SrgFileReader.read(reader, "source", "target", visitor);
    }

    public static void read(Reader reader, String sourceNs, String targetNs, MappingVisitor visitor) throws IOException {
        SrgFileReader.read(new ColumnFileReader(reader, '\t', ' '), sourceNs, targetNs, visitor);
    }

    private static void read(ColumnFileReader reader, String sourceNs, String targetNs, MappingVisitor visitor) throws IOException {
        MappingVisitor parentVisitor;
        block29: {
            int markIdx;
            MappingFormat format = MappingFormat.SRG_FILE;
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
                    String lastClassSrcName = null;
                    String lastClassDstName = null;
                    boolean classContentVisitPending = false;
                    do {
                        boolean classVisitRequired;
                        String dstDesc;
                        String dstName;
                        String srcDesc;
                        if (reader.nextCol("CL:")) {
                            String srcName = reader.nextCol();
                            if (srcName == null || srcName.isEmpty()) {
                                throw new IOException("missing class-name-a in line " + reader.getLineNumber());
                            }
                            if (classContentVisitPending) {
                                visitor.visitElementContent(MappedElementKind.CLASS);
                                classContentVisitPending = false;
                            }
                            lastClassSrcName = srcName;
                            if (!visitor.visitClass(srcName)) continue;
                            String dstName2 = reader.nextCol();
                            if (dstName2 == null || dstName2.isEmpty()) {
                                throw new IOException("missing class-name-b in line " + reader.getLineNumber());
                            }
                            lastClassDstName = dstName2;
                            visitor.visitDstName(MappedElementKind.CLASS, 0, dstName2);
                            classContentVisitPending = true;
                            continue;
                        }
                        boolean isMethod = reader.nextCol("MD:");
                        if (!isMethod && !reader.nextCol("FD:")) continue;
                        String src = reader.nextCol();
                        if (src == null) {
                            throw new IOException("missing class-/name-a in line " + reader.getLineNumber());
                        }
                        int srcSepPos = src.lastIndexOf(47);
                        if (srcSepPos <= 0 || srcSepPos == src.length() - 1) {
                            throw new IOException("invalid class-/name-a in line " + reader.getLineNumber());
                        }
                        String[] cols = new String[3];
                        for (int i = 0; i < 3; ++i) {
                            cols[i] = reader.nextCol();
                        }
                        if (!isMethod && cols[1] != null && cols[2] != null) {
                            format = MappingFormat.XSRG_FILE;
                        }
                        if (isMethod || format == MappingFormat.XSRG_FILE) {
                            srcDesc = cols[0];
                            if (srcDesc == null || srcDesc.isEmpty()) {
                                throw new IOException("missing desc-a in line " + reader.getLineNumber());
                            }
                            dstName = cols[1];
                            dstDesc = cols[2];
                            if (dstDesc == null || dstDesc.isEmpty()) {
                                throw new IOException("missing desc-b in line " + reader.getLineNumber());
                            }
                        } else {
                            srcDesc = null;
                            dstName = cols[0];
                            dstDesc = null;
                        }
                        if (dstName == null) {
                            throw new IOException("missing class-/name-b in line " + reader.getLineNumber());
                        }
                        int dstSepPos = dstName.lastIndexOf(47);
                        if (dstSepPos <= 0 || dstSepPos == dstName.length() - 1) {
                            throw new IOException("invalid class-/name-b in line " + reader.getLineNumber());
                        }
                        String srcOwner = src.substring(0, srcSepPos);
                        String dstOwner = dstName.substring(0, dstSepPos);
                        boolean bl = classVisitRequired = !srcOwner.equals(lastClassSrcName) || !dstOwner.equals(lastClassDstName);
                        if (classVisitRequired) {
                            if (classContentVisitPending) {
                                visitor.visitElementContent(MappedElementKind.CLASS);
                                classContentVisitPending = false;
                            }
                            if (!visitor.visitClass(srcOwner)) {
                                lastClassSrcName = srcOwner;
                                continue;
                            }
                            classContentVisitPending = true;
                        }
                        lastClassSrcName = srcOwner;
                        if (classVisitRequired) {
                            visitor.visitDstName(MappedElementKind.CLASS, 0, dstOwner);
                            lastClassDstName = dstOwner;
                        }
                        if (classContentVisitPending) {
                            classContentVisitPending = false;
                            if (!visitor.visitElementContent(MappedElementKind.CLASS)) continue;
                        }
                        String srcName = src.substring(srcSepPos + 1);
                        if ((!isMethod || !visitor.visitMethod(srcName, srcDesc)) && (isMethod || !visitor.visitField(srcName, srcDesc))) continue;
                        MappedElementKind kind = isMethod ? MappedElementKind.METHOD : MappedElementKind.FIELD;
                        visitor.visitDstName(kind, 0, dstName.substring(dstSepPos + 1));
                        visitor.visitDstDesc(kind, 0, dstDesc);
                        visitor.visitElementContent(kind);
                    } while (reader.nextLine(0));
                    if (classContentVisitPending) {
                        visitor.visitElementContent(MappedElementKind.CLASS);
                    }
                }
                if (visitor.visitEnd()) break block29;
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
}

