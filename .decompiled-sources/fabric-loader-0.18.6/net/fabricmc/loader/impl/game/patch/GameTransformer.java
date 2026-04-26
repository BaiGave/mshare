/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.game.patch;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.fabricmc.loader.impl.game.patch.GamePatch;
import net.fabricmc.loader.impl.launch.FabricLauncher;
import net.fabricmc.loader.impl.util.ExceptionUtil;
import net.fabricmc.loader.impl.util.SimpleClassPath;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public class GameTransformer {
    private final List<GamePatch> patches;
    private Map<String, byte[]> patchedClasses;
    private boolean entrypointsLocated = false;

    public GameTransformer(GamePatch ... patches) {
        this.patches = Arrays.asList(patches);
    }

    private void addPatchedClass(ClassNode node) {
        String key = node.name.replace('/', '.');
        if (this.patchedClasses.containsKey(key)) {
            throw new RuntimeException("Duplicate addPatchedClasses call: " + key);
        }
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        this.patchedClasses.put(key, writer.toByteArray());
    }

    public void locateEntrypoints(FabricLauncher launcher, List<Path> gameJars) {
        if (this.entrypointsLocated) {
            return;
        }
        this.patchedClasses = new HashMap<String, byte[]>();
        try (SimpleClassPath cp = new SimpleClassPath(gameJars);){
            HashMap patchedClassNodes = new HashMap();
            Function<String, ClassNode> classSource = name -> {
                if (patchedClassNodes.containsKey(name)) {
                    return (ClassNode)patchedClassNodes.get(name);
                }
                return this.readClassNode(cp, (String)name);
            };
            for (GamePatch patch : this.patches) {
                patch.process(launcher, classSource, classNode -> patchedClassNodes.put(classNode.name, classNode));
            }
            for (ClassNode patchedClassNode : patchedClassNodes.values()) {
                this.addPatchedClass(patchedClassNode);
            }
        }
        catch (IOException e) {
            throw ExceptionUtil.wrap(e);
        }
        Log.debug(LogCategory.GAME_PATCH, "Patched %d class%s", this.patchedClasses.size(), this.patchedClasses.size() != 1 ? "s" : "");
        this.entrypointsLocated = true;
    }

    /*
     * Exception decompiling
     */
    private ClassNode readClassNode(SimpleClassPath classpath, String name) {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Started 2 blocks at once
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.getStartingBlocks(Op04StructuredStatement.java:412)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:487)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    public byte[] transform(String className) {
        return this.patchedClasses.get(className);
    }

    private static ClassNode readClass(ClassReader reader) {
        if (reader == null) {
            return null;
        }
        ClassNode node = new ClassNode();
        reader.accept(node, 0);
        return node;
    }
}

