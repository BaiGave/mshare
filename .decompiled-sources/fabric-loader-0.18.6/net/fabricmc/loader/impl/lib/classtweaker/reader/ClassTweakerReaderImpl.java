/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.classtweaker.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import net.fabricmc.loader.impl.lib.classtweaker.api.ClassTweakerReader;
import net.fabricmc.loader.impl.lib.classtweaker.api.visitor.AccessWidenerVisitor;
import net.fabricmc.loader.impl.lib.classtweaker.api.visitor.ClassTweakerVisitor;
import net.fabricmc.loader.impl.lib.classtweaker.reader.ClassTweakerFormatException;

public final class ClassTweakerReaderImpl
implements ClassTweakerReader {
    public static final Charset ENCODING = StandardCharsets.UTF_8;
    private static final Pattern V1_DELIMITER = Pattern.compile("\\s+");
    private static final Pattern V2_DELIMITER = Pattern.compile("[ \\t]+");
    private static final Pattern ENUM_PARAMS_STR_PATTERN = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
    private final ClassTweakerVisitor visitor;
    private int lineNumber;

    public ClassTweakerReaderImpl(ClassTweakerVisitor visitor) {
        this.visitor = visitor;
    }

    @Override
    public void read(byte[] content, String currentNamespace) {
        String strContent = new String(content, ENCODING);
        try {
            this.read(new BufferedReader(new StringReader(strContent)), currentNamespace);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void read(BufferedReader reader, String currentNamespace) throws IOException {
        String line;
        Pattern delimiter;
        HeaderImpl header = ClassTweakerReaderImpl.readHeader(reader);
        this.lineNumber = 1;
        this.visitor.visitLineNumber(1);
        int version = header.version;
        if (currentNamespace != null && !header.namespace.equals(currentNamespace)) {
            throw this.error("Namespace (%s) does not match current runtime namespace (%s)", header.namespace, currentNamespace);
        }
        this.visitor.visitHeader(header.namespace);
        Pattern pattern = delimiter = version < 2 ? V1_DELIMITER : V2_DELIMITER;
        block10: while ((line = reader.readLine()) != null) {
            ++this.lineNumber;
            this.visitor.visitLineNumber(this.lineNumber);
            if ((line = this.handleComment(version, line)).isEmpty()) continue;
            if (Character.isWhitespace(line.codePointAt(0))) {
                throw this.error("Leading whitespace is not allowed", new Object[0]);
            }
            List<String> tokens = Arrays.asList(delimiter.split(line));
            String firstToken = tokens.get(0);
            if (version >= 3 && ("inject-interface".equals(firstToken) || "transitive-inject-interface".equals(firstToken))) {
                if (tokens.size() != 3) {
                    throw this.error("Expected (inject-interface <className> <interfaceName>) got (%s)", line);
                }
                this.visitor.visitInjectedInterface(tokens.get(1), tokens.get(2), tokens.get(0).startsWith("transitive-"));
                continue;
            }
            boolean transitive = false;
            if (version >= 2 && firstToken.startsWith("transitive-")) {
                firstToken = firstToken.substring("transitive-".length());
                transitive = true;
            }
            AccessWidenerVisitor.AccessType access = this.readAccessType(firstToken);
            if (tokens.size() < 2) {
                throw this.error("Expected <class|field|method> following " + tokens.get(0), new Object[0]);
            }
            switch (tokens.get(1)) {
                case "class": {
                    this.handleClass(line, tokens, transitive, access);
                    continue block10;
                }
                case "field": {
                    this.handleField(line, tokens, transitive, access);
                    continue block10;
                }
                case "method": {
                    this.handleMethod(line, tokens, transitive, access);
                    continue block10;
                }
            }
            throw this.error("Unsupported type: '" + tokens.get(1) + "'", new Object[0]);
        }
    }

    public static HeaderImpl readHeader(BufferedReader reader) throws IOException {
        int version;
        String headerLine = reader.readLine();
        String[] header = headerLine.split("\\s+");
        if (header.length != 3 || !header[0].equals("accessWidener") && !header[0].equals("classTweaker")) {
            throw new ClassTweakerFormatException(1, "Invalid access widener file header. Expected: 'classTweaker <version> <namespace>'");
        }
        boolean accessWidener = header[0].equals("accessWidener");
        if (accessWidener) {
            switch (header[1]) {
                case "v1": {
                    version = 1;
                    break;
                }
                case "v2": {
                    version = 2;
                    break;
                }
                default: {
                    throw new ClassTweakerFormatException(1, "Unsupported access widener format: " + header[1]);
                }
            }
        } else {
            switch (header[1]) {
                case "v1": {
                    version = 3;
                    break;
                }
                default: {
                    throw new ClassTweakerFormatException(1, "Unsupported class tweaker format: " + header[1]);
                }
            }
        }
        return new HeaderImpl(version, header[2]);
    }

    private void handleClass(String line, List<String> tokens, boolean transitive, AccessWidenerVisitor.AccessType access) {
        if (tokens.size() != 3) {
            throw this.error("Expected (<access> class <className>) got (%s)", line);
        }
        String name = tokens.get(2);
        this.validateClassName(name);
        try {
            this.visitor.visitAccessWidener(name).visitClass(access, transitive);
        }
        catch (Exception e) {
            throw this.error(e.toString(), new Object[0]);
        }
    }

    private void handleField(String line, List<String> tokens, boolean transitive, AccessWidenerVisitor.AccessType access) {
        if (tokens.size() != 5) {
            throw this.error("Expected (<access> field <className> <fieldName> <fieldDesc>) got (%s)", line);
        }
        String owner = tokens.get(2);
        String fieldName = tokens.get(3);
        String descriptor = tokens.get(4);
        this.validateClassName(owner);
        try {
            this.visitor.visitAccessWidener(owner).visitField(fieldName, descriptor, access, transitive);
        }
        catch (Exception e) {
            throw this.error(e.toString(), new Object[0]);
        }
    }

    private void handleMethod(String line, List<String> tokens, boolean transitive, AccessWidenerVisitor.AccessType access) {
        if (tokens.size() != 5) {
            throw this.error("Expected (<access> method <className> <methodName> <methodDesc>) got (%s)", line);
        }
        String owner = tokens.get(2);
        String methodName = tokens.get(3);
        String descriptor = tokens.get(4);
        this.validateClassName(owner);
        try {
            this.visitor.visitAccessWidener(owner).visitMethod(methodName, descriptor, access, transitive);
        }
        catch (Exception e) {
            throw this.error(e.toString(), new Object[0]);
        }
    }

    private String handleComment(int version, String line) {
        int commentPos = line.indexOf(35);
        if (commentPos >= 0) {
            line = line.substring(0, commentPos);
            if (version <= 1) {
                line = line.trim();
            }
        }
        return line;
    }

    private AccessWidenerVisitor.AccessType readAccessType(String access) {
        switch (access.toLowerCase(Locale.ROOT)) {
            case "accessible": {
                return AccessWidenerVisitor.AccessType.ACCESSIBLE;
            }
            case "extendable": {
                return AccessWidenerVisitor.AccessType.EXTENDABLE;
            }
            case "mutable": {
                return AccessWidenerVisitor.AccessType.MUTABLE;
            }
        }
        throw this.error("Unknown access type: " + access, new Object[0]);
    }

    private ClassTweakerFormatException error(String format, Object ... args) {
        String message = String.format(Locale.ROOT, format, args);
        return new ClassTweakerFormatException(this.lineNumber, message);
    }

    private void validateClassName(String className) {
        if (className.contains(".")) {
            throw this.error("Class-names must be specified as a/b/C, not a.b.C, but found: %s", className);
        }
    }

    static class HeaderImpl {
        private final int version;
        private final String namespace;

        HeaderImpl(int version, String namespace) {
            this.version = version;
            this.namespace = namespace;
        }
    }
}

