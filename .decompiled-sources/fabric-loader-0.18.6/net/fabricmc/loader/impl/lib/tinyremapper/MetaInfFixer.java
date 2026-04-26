/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.tinyremapper;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Iterator;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import net.fabricmc.loader.impl.lib.tinyremapper.OutputConsumerPath;
import net.fabricmc.loader.impl.lib.tinyremapper.TinyRemapper;

public class MetaInfFixer
implements OutputConsumerPath.ResourceRemapper {
    public static final MetaInfFixer INSTANCE = new MetaInfFixer();

    protected MetaInfFixer() {
    }

    @Override
    public boolean canTransform(TinyRemapper remapper, Path relativePath) {
        return relativePath.startsWith("META-INF") && (MetaInfFixer.shouldStripForFixMeta(relativePath) || relativePath.getFileName().toString().equals("MANIFEST.MF") || remapper != null && relativePath.getNameCount() == 3 && relativePath.getName(1).toString().equals("services"));
    }

    @Override
    public void transform(Path destinationDirectory, Path relativePath, InputStream input, TinyRemapper remapper) throws IOException {
        String fileName = relativePath.getFileName().toString();
        if (relativePath.getNameCount() == 2 && fileName.equals("MANIFEST.MF")) {
            Manifest manifest = new Manifest(input);
            MetaInfFixer.fixManifest(manifest, remapper);
            Path outputFile = destinationDirectory.resolve(relativePath.toString());
            Path outputDir = outputFile.getParent();
            if (outputDir != null) {
                Files.createDirectories(outputDir, new FileAttribute[0]);
            }
            try (BufferedOutputStream os = new BufferedOutputStream(Files.newOutputStream(outputFile, new OpenOption[0]));){
                manifest.write(os);
            }
        }
        if (remapper != null && relativePath.getNameCount() == 3 && relativePath.getName(1).toString().equals("services")) {
            Path outputDir = destinationDirectory.resolve(relativePath.toString()).getParent();
            Files.createDirectories(outputDir, new FileAttribute[0]);
            Path outputFile = outputDir.resolve(MetaInfFixer.mapFullyQualifiedClassName(fileName, remapper));
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                 BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);){
                MetaInfFixer.fixServiceDecl(reader, writer, remapper);
            }
        }
    }

    private static boolean shouldStripForFixMeta(Path file) {
        if (file.getNameCount() != 2) {
            return false;
        }
        assert (file.getName(0).toString().equals("META-INF"));
        String fileName = file.getFileName().toString();
        return fileName.endsWith(".SF") || fileName.endsWith(".DSA") || fileName.endsWith(".RSA") || fileName.endsWith(".EC") || fileName.startsWith("SIG-");
    }

    private static String mapFullyQualifiedClassName(String name, TinyRemapper tr) {
        assert (name.indexOf(47) < 0);
        return tr.defaultState.remapper.map(name.replace('.', '/')).replace('/', '.');
    }

    private static void fixManifest(Manifest manifest, TinyRemapper remapper) {
        Attributes mainAttrs = manifest.getMainAttributes();
        if (remapper != null) {
            String val = mainAttrs.getValue(Attributes.Name.MAIN_CLASS);
            if (val != null) {
                mainAttrs.put(Attributes.Name.MAIN_CLASS, MetaInfFixer.mapFullyQualifiedClassName(val, remapper));
            }
            if ((val = mainAttrs.getValue("Launcher-Agent-Class")) != null) {
                mainAttrs.putValue("Launcher-Agent-Class", MetaInfFixer.mapFullyQualifiedClassName(val, remapper));
            }
        }
        mainAttrs.remove(Attributes.Name.SIGNATURE_VERSION);
        Iterator<Attributes> it = manifest.getEntries().values().iterator();
        while (it.hasNext()) {
            Attributes attrs = it.next();
            Iterator<Object> it2 = attrs.keySet().iterator();
            while (it2.hasNext()) {
                Attributes.Name attrName = (Attributes.Name)it2.next();
                String name = attrName.toString();
                if (!name.endsWith("-Digest") && !name.contains("-Digest-") && !name.equals("Magic")) continue;
                it2.remove();
            }
            if (!attrs.isEmpty()) continue;
            it.remove();
        }
    }

    private static void fixServiceDecl(BufferedReader reader, BufferedWriter writer, TinyRemapper remapper) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            char c;
            int start;
            int end = line.indexOf(35);
            if (end < 0) {
                end = line.length();
            }
            for (start = 0; start < end && ((c = line.charAt(start)) == ' ' || c == '\t'); ++start) {
            }
            while (end > start && ((c = line.charAt(end - 1)) == ' ' || c == '\t')) {
                --end;
            }
            if (start == end) {
                writer.write(line);
            } else {
                writer.write(line, 0, start);
                writer.write(MetaInfFixer.mapFullyQualifiedClassName(line.substring(start, end), remapper));
                writer.write(line, end, line.length() - end);
            }
            writer.newLine();
        }
    }
}

