/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.launch.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import net.fabricmc.loader.impl.launch.knot.KnotServer;
import net.fabricmc.loader.impl.util.LoaderUtil;
import net.fabricmc.loader.impl.util.SystemProperties;

public class FabricServerLauncher {
    private static final ClassLoader parentLoader = FabricServerLauncher.class.getClassLoader();
    private static String mainClass = KnotServer.class.getName();

    public static void main(String[] args) {
        boolean dev;
        URL propUrl = parentLoader.getResource("fabric-server-launch.properties");
        if (propUrl != null) {
            Properties properties = new Properties();
            try (InputStreamReader reader = new InputStreamReader(propUrl.openStream(), StandardCharsets.UTF_8);){
                properties.load(reader);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            if (properties.containsKey("launch.mainClass")) {
                mainClass = properties.getProperty("launch.mainClass");
            }
        }
        if (!(dev = SystemProperties.isSet("fabric.development"))) {
            try {
                FabricServerLauncher.setup(args);
            }
            catch (Exception e) {
                throw new RuntimeException("Failed to setup Fabric server environment!", e);
            }
        }
        try {
            Class<?> c = Class.forName(mainClass);
            MethodHandles.lookup().findStatic(c, "main", MethodType.methodType(Void.TYPE, String[].class)).invokeExact(args);
        }
        catch (Throwable e) {
            throw new RuntimeException("An exception occurred when launching the server!", e);
        }
    }

    private static void setup(String ... runArguments) throws IOException {
        Path serverJar;
        String path = System.getProperty("fabric.gameJarPath");
        if (path == null) {
            path = FabricServerLauncher.getServerJarPath();
            System.setProperty("fabric.gameJarPath", path);
        }
        if (!Files.exists(serverJar = LoaderUtil.normalizePath(Paths.get(path, new String[0])), new LinkOption[0])) {
            System.err.println("The Minecraft server .JAR is missing (" + serverJar + ")!");
            System.err.println();
            System.err.println("Fabric's server-side launcher expects the server .JAR to be provided.");
            System.err.println("You can edit its location in fabric-server-launcher.properties.");
            System.err.println();
            System.err.println("Without the official Minecraft server .JAR, Fabric Loader cannot launch.");
            throw new RuntimeException("Missing game jar at " + serverJar);
        }
    }

    private static String getServerJarPath() throws IOException {
        Path propertiesFile = Paths.get("fabric-server-launcher.properties", new String[0]);
        Properties properties = new Properties();
        if (Files.exists(propertiesFile, new LinkOption[0])) {
            try (BufferedReader reader = Files.newBufferedReader(propertiesFile);){
                properties.load(reader);
            }
        }
        if (!properties.containsKey("serverJar")) {
            properties.put("serverJar", "server.jar");
            try (BufferedWriter writer = Files.newBufferedWriter(propertiesFile, new OpenOption[0]);){
                properties.store(writer, null);
            }
        }
        return (String)properties.get("serverJar");
    }
}

