/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.mappingio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.loader.impl.lib.mappingio.MappingVisitor;
import net.fabricmc.loader.impl.lib.mappingio.format.MappingFormat;
import net.fabricmc.loader.impl.lib.mappingio.format.enigma.EnigmaDirReader;
import net.fabricmc.loader.impl.lib.mappingio.format.enigma.EnigmaFileReader;
import net.fabricmc.loader.impl.lib.mappingio.format.intellij.MigrationMapFileReader;
import net.fabricmc.loader.impl.lib.mappingio.format.jobf.JobfFileReader;
import net.fabricmc.loader.impl.lib.mappingio.format.proguard.ProGuardFileReader;
import net.fabricmc.loader.impl.lib.mappingio.format.simple.RecafSimpleFileReader;
import net.fabricmc.loader.impl.lib.mappingio.format.srg.JamFileReader;
import net.fabricmc.loader.impl.lib.mappingio.format.srg.SrgFileReader;
import net.fabricmc.loader.impl.lib.mappingio.format.srg.TsrgFileReader;
import net.fabricmc.loader.impl.lib.mappingio.format.tiny.Tiny1FileReader;
import net.fabricmc.loader.impl.lib.mappingio.format.tiny.Tiny2FileReader;
import org.jetbrains.annotations.Nullable;

public final class MappingReader {
    @Nullable
    public static MappingFormat detectFormat(Path file) throws IOException {
        if (Files.isDirectory(file, new LinkOption[0])) {
            return MappingFormat.ENIGMA_DIR;
        }
        try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(file, new OpenOption[0]), StandardCharsets.UTF_8);){
            String fileName = file.getFileName().toString();
            int dotIdx = fileName.lastIndexOf(46);
            String fileExt = dotIdx >= 0 ? fileName.substring(dotIdx + 1) : null;
            MappingFormat mappingFormat = MappingReader.detectFormat(reader, fileExt);
            return mappingFormat;
        }
    }

    @Nullable
    public static MappingFormat detectFormat(Reader reader) throws IOException {
        return MappingReader.detectFormat(reader, null);
    }

    private static MappingFormat detectFormat(Reader reader, @Nullable String fileExt) throws IOException {
        int pos;
        int len;
        char[] buffer = new char[4096];
        BufferedReader br = reader instanceof BufferedReader ? (BufferedReader)reader : new BufferedReader(reader);
        br.mark(4096);
        for (pos = 0; pos < buffer.length && (len = br.read(buffer, pos, buffer.length - pos)) >= 0; pos += len) {
        }
        br.reset();
        if (pos < 3) {
            return null;
        }
        switch (String.valueOf(buffer, 0, 3)) {
            case "v1\t": {
                return MappingFormat.TINY_FILE;
            }
            case "tin": {
                return MappingFormat.TINY_2_FILE;
            }
            case "tsr": {
                return MappingFormat.TSRG_2_FILE;
            }
            case "CLA": {
                return MappingFormat.ENIGMA_FILE;
            }
            case "PK:": 
            case "CL:": 
            case "FD:": 
            case "MD:": {
                return MappingReader.detectSrgOrXsrg(br, fileExt);
            }
            case "CL ": 
            case "FD ": 
            case "MD ": 
            case "MP ": {
                return MappingFormat.JAM_FILE;
            }
        }
        String headerStr = String.valueOf(buffer, 0, pos);
        if (headerStr.contains("<migrationMap>")) {
            return MappingFormat.INTELLIJ_MIGRATION_MAP_FILE;
        }
        if ((headerStr.startsWith("p ") || headerStr.startsWith("c ") || headerStr.startsWith("f ") || headerStr.startsWith("m ")) && headerStr.contains(" = ")) {
            return MappingFormat.JOBF_FILE;
        }
        if (headerStr.contains(" -> ")) {
            return MappingFormat.PROGUARD_FILE;
        }
        if (headerStr.contains("\n\t")) {
            return MappingFormat.TSRG_FILE;
        }
        if (fileExt != null && fileExt.equals(MappingFormat.CSRG_FILE.fileExt)) {
            return MappingFormat.CSRG_FILE;
        }
        return null;
    }

    private static MappingFormat detectSrgOrXsrg(BufferedReader reader, @Nullable String fileExt) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.startsWith("FD:")) continue;
            String[] parts = line.split(" ");
            if (parts.length < 5 || MappingReader.isEmptyOrStartsWithHash(parts[3]) || MappingReader.isEmptyOrStartsWithHash(parts[4])) {
                return MappingFormat.SRG_FILE;
            }
            return MappingFormat.XSRG_FILE;
        }
        return MappingFormat.XSRG_FILE.fileExt.equals(fileExt) ? MappingFormat.XSRG_FILE : MappingFormat.SRG_FILE;
    }

    private static boolean isEmptyOrStartsWithHash(String string) {
        return string.isEmpty() || string.startsWith("#");
    }

    public static List<String> getNamespaces(Path file) throws IOException {
        return MappingReader.getNamespaces(file, null);
    }

    public static List<String> getNamespaces(Path file, MappingFormat format) throws IOException {
        if (format == null && (format = MappingReader.detectFormat(file)) == null) {
            throw new IOException("invalid/unsupported mapping format");
        }
        if (format.features().hasNamespaces()) {
            try (BufferedReader reader = Files.newBufferedReader(file);){
                List<String> list = MappingReader.getNamespaces(reader, format);
                return list;
            }
        }
        return Arrays.asList("source", "target");
    }

    public static List<String> getNamespaces(Reader reader) throws IOException {
        return MappingReader.getNamespaces(reader, null);
    }

    public static List<String> getNamespaces(Reader reader, MappingFormat format) throws IOException {
        if (format == null) {
            if (!reader.markSupported()) {
                reader = new BufferedReader(reader);
            }
            reader.mark(4096);
            format = MappingReader.detectFormat(reader);
            reader.reset();
            if (format == null) {
                throw new IOException("invalid/unsupported mapping format");
            }
        }
        if (format.features().hasNamespaces()) {
            MappingReader.checkReaderCompatible(format);
            switch (format) {
                case TINY_FILE: {
                    return Tiny1FileReader.getNamespaces(reader);
                }
                case TINY_2_FILE: {
                    return Tiny2FileReader.getNamespaces(reader);
                }
                case TSRG_2_FILE: {
                    return TsrgFileReader.getNamespaces(reader);
                }
            }
            throw new IllegalStateException();
        }
        return Arrays.asList("source", "target");
    }

    public static void read(Path path, MappingVisitor visitor) throws IOException {
        MappingReader.read(path, null, visitor);
    }

    public static void read(Path path, MappingFormat format, MappingVisitor visitor) throws IOException {
        if (format == null && (format = MappingReader.detectFormat(path)) == null) {
            throw new IOException("invalid/unsupported mapping format");
        }
        if (format.hasSingleFile()) {
            try (BufferedReader reader = Files.newBufferedReader(path);){
                MappingReader.read(reader, format, visitor);
            }
        } else {
            switch (format) {
                case ENIGMA_DIR: {
                    EnigmaDirReader.read(path, visitor);
                    break;
                }
                default: {
                    throw new IllegalStateException();
                }
            }
        }
    }

    public static void read(Reader reader, MappingVisitor visitor) throws IOException {
        MappingReader.read(reader, null, visitor);
    }

    public static void read(Reader reader, MappingFormat format, MappingVisitor visitor) throws IOException {
        if (format == null) {
            if (!reader.markSupported()) {
                reader = new BufferedReader(reader);
            }
            reader.mark(4096);
            format = MappingReader.detectFormat(reader);
            reader.reset();
            if (format == null) {
                throw new IOException("invalid/unsupported mapping format");
            }
        }
        MappingReader.checkReaderCompatible(format);
        switch (format) {
            case TINY_FILE: {
                Tiny1FileReader.read(reader, visitor);
                break;
            }
            case TINY_2_FILE: {
                Tiny2FileReader.read(reader, visitor);
                break;
            }
            case ENIGMA_FILE: {
                EnigmaFileReader.read(reader, visitor);
                break;
            }
            case SRG_FILE: 
            case XSRG_FILE: {
                SrgFileReader.read(reader, visitor);
                break;
            }
            case JAM_FILE: {
                JamFileReader.read(reader, visitor);
                break;
            }
            case TSRG_2_FILE: 
            case CSRG_FILE: 
            case TSRG_FILE: {
                TsrgFileReader.read(reader, visitor);
                break;
            }
            case PROGUARD_FILE: {
                ProGuardFileReader.read(reader, visitor);
                break;
            }
            case INTELLIJ_MIGRATION_MAP_FILE: {
                MigrationMapFileReader.read(reader, visitor);
                break;
            }
            case RECAF_SIMPLE_FILE: {
                RecafSimpleFileReader.read(reader, visitor);
                break;
            }
            case JOBF_FILE: {
                JobfFileReader.read(reader, visitor);
                break;
            }
            default: {
                throw new IllegalStateException();
            }
        }
    }

    private static void checkReaderCompatible(MappingFormat format) throws IOException {
        if (!format.hasSingleFile()) {
            throw new IOException("can't read mapping format " + format.name + " using a Reader, use the Path based API");
        }
    }
}

