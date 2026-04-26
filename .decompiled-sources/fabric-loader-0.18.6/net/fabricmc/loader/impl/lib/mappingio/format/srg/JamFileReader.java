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
import net.fabricmc.loader.impl.lib.mappingio.tree.MappingTree;
import net.fabricmc.loader.impl.lib.mappingio.tree.MemoryMappingTree;

public final class JamFileReader {
    private JamFileReader() {
    }

    public static void read(Reader reader, MappingVisitor visitor) throws IOException {
        JamFileReader.read(reader, "source", "target", visitor);
    }

    public static void read(Reader reader, String sourceNs, String targetNs, MappingVisitor visitor) throws IOException {
        JamFileReader.read(new ColumnFileReader(reader, '\t', ' '), sourceNs, targetNs, visitor);
    }

    private static void read(ColumnFileReader reader, String sourceNs, String targetNs, MappingVisitor visitor) throws IOException {
        MappingVisitor parentVisitor;
        block28: {
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
                    String lastClassName = null;
                    boolean visitClass = false;
                    String lastMethodName = null;
                    String lastMethodDesc = null;
                    boolean visitMember = false;
                    boolean visitMethodContent = false;
                    do {
                        boolean isField;
                        String dstName;
                        boolean isArg = false;
                        if (reader.nextCol("CL")) {
                            String srcName = reader.nextCol();
                            if (srcName == null || srcName.isEmpty()) {
                                throw new IOException("missing class-name-a in line " + reader.getLineNumber());
                            }
                            lastClassName = srcName;
                            visitClass = visitor.visitClass(srcName);
                            if (!visitClass) continue;
                            String dstName2 = reader.nextCol();
                            if (dstName2 == null || dstName2.isEmpty()) {
                                throw new IOException("missing class-name-b in line " + reader.getLineNumber());
                            }
                            visitor.visitDstName(MappedElementKind.CLASS, 0, dstName2);
                            visitClass = visitor.visitElementContent(MappedElementKind.CLASS);
                            continue;
                        }
                        boolean isMethod = reader.nextCol("MD");
                        if (!isMethod && !reader.nextCol("FD") && !(isArg = reader.nextCol("MP"))) continue;
                        String clsSrcName = reader.nextCol();
                        if (clsSrcName == null) {
                            throw new IOException("missing class-name-a in line " + reader.getLineNumber());
                        }
                        String memberSrcName = reader.nextCol();
                        if (memberSrcName == null || memberSrcName.isEmpty()) {
                            throw new IOException("missing member-name-a in line " + reader.getLineNumber());
                        }
                        String memberSrcDesc = reader.nextCol();
                        if (memberSrcDesc == null || memberSrcDesc.isEmpty()) {
                            throw new IOException("missing member-desc-a in line " + reader.getLineNumber());
                        }
                        String col5 = reader.nextCol();
                        String col6 = reader.nextCol();
                        String col7 = reader.nextCol();
                        int argSrcPos = -1;
                        if (!isArg) {
                            dstName = col5;
                        } else {
                            argSrcPos = Integer.parseInt(col5);
                            if (col7 == null || col7.isEmpty()) {
                                dstName = col6;
                            } else {
                                String argSrcDesc = col6;
                                if (argSrcDesc == null || argSrcDesc.isEmpty()) {
                                    throw new IOException("missing parameter-desc-a in line " + reader.getLineNumber());
                                }
                                dstName = col7;
                            }
                        }
                        if (dstName == null || dstName.isEmpty()) {
                            throw new IOException("missing name-b in line " + reader.getLineNumber());
                        }
                        if (!clsSrcName.equals(lastClassName)) {
                            lastClassName = clsSrcName;
                            lastMethodName = null;
                            lastMethodDesc = null;
                            boolean bl = visitClass = visitor.visitClass(clsSrcName) && visitor.visitElementContent(MappedElementKind.CLASS);
                        }
                        if (!visitClass) continue;
                        boolean newMethod = false;
                        boolean bl = isField = !isMethod && !isArg;
                        if (isField) {
                            visitMember = visitor.visitField(memberSrcName, memberSrcDesc);
                        } else if (!isArg || (newMethod = !memberSrcName.equals(lastMethodName) || !memberSrcDesc.equals(lastMethodDesc))) {
                            lastMethodName = memberSrcName;
                            lastMethodDesc = memberSrcDesc;
                            visitMember = visitor.visitMethod(memberSrcName, memberSrcDesc);
                            visitMethodContent = false;
                        }
                        if (!visitMember) continue;
                        if (isField) {
                            visitor.visitDstName(MappedElementKind.FIELD, 0, dstName);
                            visitor.visitElementContent(MappedElementKind.FIELD);
                            continue;
                        }
                        if (isMethod) {
                            visitor.visitDstName(MappedElementKind.METHOD, 0, dstName);
                        }
                        if (isMethod || newMethod) {
                            visitMethodContent = visitor.visitElementContent(MappedElementKind.METHOD);
                        }
                        if (!isArg || !visitMethodContent || !visitor.visitMethodArg(argSrcPos, -1, null)) continue;
                        visitor.visitDstName(MappedElementKind.METHOD_ARG, 0, dstName);
                        visitor.visitElementContent(MappedElementKind.METHOD_ARG);
                    } while (reader.nextLine(0));
                }
                if (visitor.visitEnd()) break block28;
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

