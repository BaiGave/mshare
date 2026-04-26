/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.mappingio.format.intellij;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import net.fabricmc.loader.impl.lib.mappingio.MappedElementKind;
import net.fabricmc.loader.impl.lib.mappingio.MappingFlag;
import net.fabricmc.loader.impl.lib.mappingio.MappingVisitor;

public final class MigrationMapFileReader {
    public static void read(Reader reader, MappingVisitor visitor) throws IOException {
        MigrationMapFileReader.read(reader, "source", "target", visitor);
    }

    public static void read(Reader reader, String sourceNs, String targetNs, MappingVisitor visitor) throws IOException {
        BufferedReader br = reader instanceof BufferedReader ? (BufferedReader)reader : new BufferedReader(reader);
        MigrationMapFileReader.read(br, sourceNs, targetNs, visitor);
    }

    private static void read(BufferedReader reader, String sourceNs, String targetNs, MappingVisitor visitor) throws IOException {
        try {
            MigrationMapFileReader.read0(reader, sourceNs, targetNs, visitor);
        }
        catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    private static void read0(BufferedReader reader, String sourceNs, String targetNs, MappingVisitor visitor) throws IOException, XMLStreamException {
        CharArrayReader parentReader = null;
        if (visitor.getFlags().contains((Object)MappingFlag.NEEDS_MULTIPLE_PASSES)) {
            int len;
            char[] buffer = new char[100000];
            int pos = 0;
            while ((len = reader.read(buffer, pos, buffer.length - pos)) >= 0) {
                if ((pos += len) != buffer.length) continue;
                buffer = Arrays.copyOf(buffer, buffer.length * 2);
            }
            parentReader = new CharArrayReader(buffer, 0, pos);
            reader = new CustomBufferedReader((Reader)parentReader);
        }
        XMLInputFactory factory = XMLInputFactory.newInstance();
        while (true) {
            block29: {
                XMLStreamReader xmlReader = factory.createXMLStreamReader(reader);
                boolean visitHeader = visitor.visitHeader();
                if (visitHeader) {
                    visitor.visitNamespaces(sourceNs, Collections.singletonList(targetNs));
                }
                if (!visitor.visitContent()) break block29;
                int depth = 0;
                while (xmlReader.hasNext()) {
                    block30: {
                        int event = xmlReader.next();
                        switch (event) {
                            case 1: {
                                String name = xmlReader.getLocalName();
                                if (depth != (name.equals("migrationMap") ? 0 : 1)) {
                                    throw new IOException("unexpected depth at line " + xmlReader.getLocation().getLineNumber());
                                }
                                ++depth;
                                switch (name) {
                                    case "name": 
                                    case "order": 
                                    case "description": {
                                        if (visitHeader) {
                                            String value = xmlReader.getAttributeValue(null, "value");
                                            if (name.equals("order")) {
                                                name = "migrationmap:order";
                                            }
                                            if (name.equals("name") && value.equals("Unnamed migration map")) break;
                                            visitor.visitMetadata(name, value);
                                            break;
                                        }
                                        break block30;
                                    }
                                    case "entry": {
                                        String type = xmlReader.getAttributeValue(null, "type");
                                        if (type == null || type.isEmpty()) {
                                            throw new IOException("missing/empty type attribute at line " + xmlReader.getLocation().getLineNumber());
                                        }
                                        if (type.equals("package")) break;
                                        if (!type.equals("class")) {
                                            throw new IOException("unexpected entry type " + type + " at line " + xmlReader.getLocation().getLineNumber());
                                        }
                                        String srcName = xmlReader.getAttributeValue(null, "oldName");
                                        String dstName = xmlReader.getAttributeValue(null, "newName");
                                        if (srcName == null || srcName.isEmpty()) {
                                            throw new IOException("missing/empty oldName attribute at line " + xmlReader.getLocation().getLineNumber());
                                        }
                                        if (dstName == null || dstName.isEmpty()) {
                                            throw new IOException("missing/empty newName attribute at line " + xmlReader.getLocation().getLineNumber());
                                        }
                                        srcName = srcName.replace('.', '/');
                                        dstName = dstName.replace('.', '/');
                                        if (visitor.visitClass(srcName)) {
                                            visitor.visitDstName(MappedElementKind.CLASS, 0, dstName);
                                            visitor.visitElementContent(MappedElementKind.CLASS);
                                        } else {
                                            break;
                                        }
                                    }
                                }
                                break;
                            }
                            case 2: {
                                --depth;
                            }
                        }
                    }
                }
            }
            if (visitor.visitEnd()) {
                if (parentReader == null) break;
                ((CustomBufferedReader)reader).forceClose();
                break;
            }
            if (parentReader == null) {
                throw new IllegalStateException("repeated visitation requested without NEEDS_MULTIPLE_PASSES");
            }
            parentReader.reset();
            reader = new CustomBufferedReader((Reader)parentReader);
        }
    }

    private static class CustomBufferedReader
    extends BufferedReader {
        private CustomBufferedReader(Reader in) {
            super(in);
        }

        public void forceClose() throws IOException {
            super.close();
        }

        @Override
        public void close() throws IOException {
        }
    }
}

