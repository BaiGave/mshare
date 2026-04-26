/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.mappingio.format.proguard;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import net.fabricmc.loader.impl.lib.mappingio.MappedElementKind;
import net.fabricmc.loader.impl.lib.mappingio.MappingFlag;
import net.fabricmc.loader.impl.lib.mappingio.MappingVisitor;
import net.fabricmc.loader.impl.lib.mappingio.format.ColumnFileReader;

public final class ProGuardFileReader {
    private ProGuardFileReader() {
    }

    public static void read(Reader reader, MappingVisitor visitor) throws IOException {
        ProGuardFileReader.read(reader, "source", "target", visitor);
    }

    public static void read(Reader reader, String sourceNs, String targetNs, MappingVisitor visitor) throws IOException {
        ProGuardFileReader.read(new ColumnFileReader(reader, ';', ' '), sourceNs, targetNs, visitor);
    }

    private static void read(ColumnFileReader reader, String sourceNs, String targetNs, MappingVisitor visitor) throws IOException {
        block21: {
            int markIdx;
            boolean readerMarked = false;
            if (visitor.getFlags().contains((Object)MappingFlag.NEEDS_MULTIPLE_PASSES)) {
                reader.mark();
                readerMarked = true;
            }
            StringBuilder descSb = null;
            while (true) {
                if (visitor.visitHeader()) {
                    visitor.visitNamespaces(sourceNs, Collections.singletonList(targetNs));
                }
                if (visitor.visitContent()) {
                    if (descSb == null) {
                        descSb = new StringBuilder();
                    }
                    boolean visitClass = false;
                    do {
                        String argDesc;
                        String desc;
                        String name;
                        String retType;
                        String name2;
                        String line;
                        if ((line = reader.nextCols(false)) == null || (line = line.trim()).isEmpty() || line.startsWith("#")) continue;
                        if (line.endsWith(":")) {
                            int pos = line.indexOf(" -> ");
                            if (pos < 0) {
                                throw new IOException("invalid proguard line (invalid separator): " + line);
                            }
                            if (pos == 0) {
                                throw new IOException("invalid proguard line (empty src class): " + line);
                            }
                            if (pos + 4 + 1 >= line.length()) {
                                throw new IOException("invalid proguard line (empty dst class): " + line);
                            }
                            name2 = line.substring(0, pos).replace('.', '/');
                            visitClass = visitor.visitClass(name2);
                            if (!visitClass) continue;
                            String mappedName = line.substring(pos + 4, line.length() - 1).replace('.', '/');
                            visitor.visitDstName(MappedElementKind.CLASS, 0, mappedName);
                            visitClass = visitor.visitElementContent(MappedElementKind.CLASS);
                            continue;
                        }
                        if (!visitClass) continue;
                        String[] parts = line.split(" ");
                        if (parts.length != 4) {
                            throw new IOException("invalid proguard line (extra columns): " + line);
                        }
                        if (parts[0].isEmpty()) {
                            throw new IOException("invalid proguard line (empty type): " + line);
                        }
                        if (parts[1].isEmpty()) {
                            throw new IOException("invalid proguard line (empty src member): " + line);
                        }
                        if (!parts[2].equals("->")) {
                            throw new IOException("invalid proguard line (invalid separator): " + line);
                        }
                        if (parts[3].isEmpty()) {
                            throw new IOException("invalid proguard line (empty dst member): " + line);
                        }
                        if (parts[1].indexOf(40) < 0) {
                            name2 = parts[1];
                            String desc2 = ProGuardFileReader.pgTypeToAsm(parts[0], descSb);
                            if (!visitor.visitField(name2, desc2)) continue;
                            String mappedName = parts[3];
                            visitor.visitDstName(MappedElementKind.FIELD, 0, mappedName);
                            visitor.visitElementContent(MappedElementKind.FIELD);
                            continue;
                        }
                        String part0 = parts[0];
                        int pos = part0.indexOf(58);
                        if (pos == -1) {
                            retType = part0;
                        } else {
                            int pos2 = part0.indexOf(58, pos + 1);
                            assert (pos2 != -1);
                            retType = part0.substring(pos2 + 1);
                        }
                        String part1 = parts[1];
                        pos = part1.indexOf(40);
                        int pos3 = part1.indexOf(41, pos + 1);
                        assert (pos3 != -1);
                        if (part1.lastIndexOf(46, pos - 1) >= 0 || part1.length() != pos3 + 1 || !visitor.visitMethod(name = part1.substring(0, pos), desc = ProGuardFileReader.pgDescToAsm(argDesc = part1.substring(pos, pos3 + 1), retType, descSb))) continue;
                        String mappedName = parts[3];
                        visitor.visitDstName(MappedElementKind.METHOD, 0, mappedName);
                        visitor.visitElementContent(MappedElementKind.METHOD);
                    } while (reader.nextLine(0));
                }
                if (visitor.visitEnd()) break block21;
                if (!readerMarked) {
                    throw new IllegalStateException("repeated visitation requested without NEEDS_MULTIPLE_PASSES");
                }
                markIdx = reader.reset();
                assert (markIdx == 1);
            }
        }
    }

    private static String pgDescToAsm(String pgArgDesc, String pgRetType, StringBuilder tmp) {
        tmp.setLength(0);
        tmp.append('(');
        if (pgArgDesc.length() > 2) {
            int startPos = 1;
            boolean abort = false;
            do {
                int endPos;
                if ((endPos = pgArgDesc.indexOf(44, startPos)) < 0) {
                    endPos = pgArgDesc.length() - 1;
                    abort = true;
                }
                ProGuardFileReader.appendPgTypeToAsm(pgArgDesc.substring(startPos, endPos), tmp);
                startPos = endPos + 1;
            } while (!abort);
        }
        tmp.append(')');
        if (pgRetType != null) {
            ProGuardFileReader.appendPgTypeToAsm(pgRetType, tmp);
        }
        return tmp.toString();
    }

    private static String pgTypeToAsm(String type, StringBuilder tmp) {
        tmp.setLength(0);
        ProGuardFileReader.appendPgTypeToAsm(type, tmp);
        return tmp.toString();
    }

    private static void appendPgTypeToAsm(String type, StringBuilder out) {
        assert (!type.isEmpty());
        int arrayStart = type.indexOf(91);
        if (arrayStart != -1) {
            assert (type.substring(arrayStart).matches("(\\[])+"));
            int arrayDimensions = (type.length() - arrayStart) / 2;
            for (int i = 0; i < arrayDimensions; ++i) {
                out.append('[');
            }
            type = type.substring(0, arrayStart);
        }
        switch (type) {
            case "void": {
                out.append('V');
                break;
            }
            case "boolean": {
                out.append('Z');
                break;
            }
            case "char": {
                out.append('C');
                break;
            }
            case "byte": {
                out.append('B');
                break;
            }
            case "short": {
                out.append('S');
                break;
            }
            case "int": {
                out.append('I');
                break;
            }
            case "float": {
                out.append('F');
                break;
            }
            case "long": {
                out.append('J');
                break;
            }
            case "double": {
                out.append('D');
                break;
            }
            default: {
                out.append('L');
                out.append(type.replace('.', '/'));
                out.append(';');
            }
        }
    }
}

