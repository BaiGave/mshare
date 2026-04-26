/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.gui;

import java.awt.GraphicsEnvironment;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.game.GameProvider;
import net.fabricmc.loader.impl.gui.FabricMainWindow;
import net.fabricmc.loader.impl.gui.FabricStatusTree;
import net.fabricmc.loader.impl.util.LoaderUtil;
import net.fabricmc.loader.impl.util.Localization;
import net.fabricmc.loader.impl.util.UrlUtil;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;

public final class FabricGuiEntry {
    public static void open(FabricStatusTree tree) throws Exception {
        GameProvider provider = FabricLoaderImpl.INSTANCE.tryGetGameProvider();
        if (provider == null && LoaderUtil.hasAwtSupport() || provider != null && provider.hasAwtSupport()) {
            FabricMainWindow.open(tree, true);
        } else {
            FabricGuiEntry.openForked(tree);
        }
    }

    private static void openForked(FabricStatusTree tree) throws IOException, InterruptedException {
        Path javaBinDir = LoaderUtil.normalizePath(Paths.get(System.getProperty("java.home"), "bin"));
        String[] executables = new String[]{"javaw.exe", "java.exe", "java"};
        Path javaPath = null;
        for (String executable : executables) {
            Path path = javaBinDir.resolve(executable);
            if (!Files.isRegularFile(path, new LinkOption[0])) continue;
            javaPath = path;
            break;
        }
        if (javaPath == null) {
            throw new RuntimeException("can't find java executable in " + javaBinDir);
        }
        Process process = new ProcessBuilder(javaPath.toString(), "-Xmx100M", "-cp", UrlUtil.LOADER_CODE_SOURCE.toString(), FabricGuiEntry.class.getName()).redirectOutput(ProcessBuilder.Redirect.INHERIT).redirectError(ProcessBuilder.Redirect.INHERIT).start();
        Thread shutdownHook = new Thread(process::destroy);
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        try (DataOutputStream os = new DataOutputStream(process.getOutputStream());){
            tree.writeTo(os);
        }
        int rVal = process.waitFor();
        Runtime.getRuntime().removeShutdownHook(shutdownHook);
        if (rVal != 0) {
            throw new IOException("subprocess exited with code " + rVal);
        }
    }

    public static void main(String[] args) throws Exception {
        FabricStatusTree tree = new FabricStatusTree(new DataInputStream(System.in));
        FabricMainWindow.open(tree, true);
        System.exit(0);
    }

    public static void displayCriticalError(Throwable exception, boolean exitAfter) {
        Log.error(LogCategory.GENERAL, "A critical error occurred", exception);
        FabricGuiEntry.displayError(Localization.format("gui.error.header", new Object[0]), exception, exitAfter);
    }

    public static void displayError(String mainText, Throwable exception, boolean exitAfter) {
        FabricGuiEntry.displayError(mainText, exception, tree -> {
            StringWriter error = new StringWriter();
            error.append(mainText);
            if (exception != null) {
                error.append(System.lineSeparator());
                exception.printStackTrace(new PrintWriter(error));
            }
            tree.addButton(Localization.format("gui.button.copyError", new Object[0]), FabricStatusTree.FabricBasicButtonType.CLICK_MANY).withClipboard(error.toString());
        }, exitAfter);
    }

    public static void displayError(String mainText, Throwable exception, Consumer<FabricStatusTree> treeCustomiser, boolean exitAfter) {
        GameProvider provider = FabricLoaderImpl.INSTANCE.tryGetGameProvider();
        if (!GraphicsEnvironment.isHeadless() && (provider == null || provider.canOpenErrorGui())) {
            String title = "Fabric Loader 0.18.6";
            FabricStatusTree tree = new FabricStatusTree(title, mainText);
            FabricStatusTree.FabricStatusTab crashTab = tree.addTab(Localization.format("gui.tab.crash", new Object[0]));
            if (exception != null) {
                crashTab.node.addCleanedException(exception);
            } else {
                crashTab.node.addMessage(Localization.format("gui.error.missingException", new Object[0]), FabricStatusTree.FabricTreeWarningLevel.NONE);
            }
            tree.addButton(Localization.format("gui.button.exit", new Object[0]), FabricStatusTree.FabricBasicButtonType.CLICK_ONCE).makeClose();
            treeCustomiser.accept(tree);
            try {
                FabricGuiEntry.open(tree);
            }
            catch (Exception e) {
                if (exitAfter) {
                    Log.warn(LogCategory.GENERAL, "Failed to open the error gui!", e);
                }
                throw new RuntimeException("Failed to open the error gui!", e);
            }
        }
        if (exitAfter) {
            System.exit(1);
        }
    }
}

