/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.jtracy;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

class Loader {
    private final String name;

    Loader() {
        String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        String osArch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);
        String prefix = "";
        String name = "jtracy-jni";
        String suffix = "";
        switch (osArch) {
            case "amd64": 
            case "x86_64": 
            case "x86-64": {
                if (osName.contains("win")) {
                    suffix = "-windows.dll";
                    break;
                }
                if (osName.contains("mac") || osName.contains("darwin")) {
                    prefix = "lib";
                    suffix = "-macos.dylib";
                    break;
                }
                if (osName.contains("linux") || osName.contains("unix")) {
                    prefix = "lib";
                    suffix = "-linux.so";
                    break;
                }
                throw new UnsatisfiedLinkError("Unsupported OS name: " + osName + " / " + osArch);
            }
            case "aarch64": {
                if (osName.contains("mac") || osName.contains("darwin")) {
                    prefix = "lib";
                    suffix = "-macos-arm64.dylib";
                    break;
                }
                throw new UnsatisfiedLinkError("Unsupported OS name: " + osName + " / " + osArch);
            }
            default: {
                throw new UnsatisfiedLinkError("Unsupported OS arch: " + osName + " / " + osArch);
            }
        }
        this.name = prefix + "jtracy-jni" + suffix;
    }

    private Path createUnpackRoot() {
        Path path = Path.of(System.getProperty("java.io.tmpdir"), new String[0]).resolve("jtracy-" + String.valueOf(UUID.randomUUID()));
        try {
            Files.createDirectory(path, new FileAttribute[0]);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        return path;
    }

    public void load() {
        Path root = this.createUnpackRoot();
        try {
            Path path = this.unpackLibrary(root);
            System.load(path.toAbsolutePath().toString());
        }
        catch (Throwable throwable) {
            try {
                Files.walkFileTree(root, Set.of(), 1, (FileVisitor<? super Path>)new SimpleFileVisitor<Path>(this){

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
            catch (IOException iOException) {
                // empty catch block
            }
            try {
                Files.deleteIfExists(root);
            }
            catch (IOException iOException) {
                // empty catch block
            }
            throw throwable;
        }
        try {
            Files.walkFileTree(root, Set.of(), 1, (FileVisitor<? super Path>)new /* invalid duplicate definition of identical inner class */);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        try {
            Files.deleteIfExists(root);
        }
        catch (IOException iOException) {}
    }

    private Path unpackLibrary(Path root) {
        Path path;
        block9: {
            InputStream input = Loader.class.getClassLoader().getResourceAsStream(this.name);
            try {
                if (input == null) {
                    throw new UnsatisfiedLinkError("Could not find jtracy natives at " + this.name);
                }
                Path path2 = Files.createTempFile(root, this.name, null, new FileAttribute[0]);
                Files.copy(input, path2, StandardCopyOption.REPLACE_EXISTING);
                path = path2;
                if (input == null) break block9;
            }
            catch (Throwable throwable) {
                try {
                    if (input != null) {
                        try {
                            input.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (IOException e) {
                    throw new LinkageError("Can't unpack jtracy natives found at " + this.name + " to " + String.valueOf(root), e);
                }
            }
            input.close();
        }
        return path;
    }
}

