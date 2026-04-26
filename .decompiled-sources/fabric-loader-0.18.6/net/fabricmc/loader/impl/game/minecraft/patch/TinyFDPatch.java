/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.game.minecraft.patch;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.game.patch.GamePatch;
import net.fabricmc.loader.impl.launch.FabricLauncher;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public final class TinyFDPatch
extends GamePatch {
    private static final String MORE_OPTIONS_DIALOG_CLASS_NAME = "net.minecraft.class_5292";
    private static final String TINYFD_METHOD_NAME = "tinyfd_openFileDialog";
    private static final String DIALOG_TITLE = "Select settings file (.json)";

    @Override
    public void process(FabricLauncher launcher, Function<String, ClassNode> classSource, Consumer<ClassNode> classEmitter) {
        ClassNode classNode;
        if (launcher.getEnvironmentType() != EnvType.CLIENT) {
            return;
        }
        String className = MORE_OPTIONS_DIALOG_CLASS_NAME;
        if (!launcher.getMappingConfiguration().getRuntimeNamespace().equals("intermediary") && FabricLoader.getInstance().getMappingResolver().getNamespaces().contains("intermediary")) {
            className = FabricLoader.getInstance().getMappingResolver().mapClassName("intermediary", MORE_OPTIONS_DIALOG_CLASS_NAME);
        }
        if ((classNode = classSource.apply(className)) == null) {
            return;
        }
        this.patchMoreOptionsDialog(classNode);
        classEmitter.accept(classNode);
    }

    private void patchMoreOptionsDialog(ClassNode classNode) {
        for (MethodNode method : classNode.methods) {
            ListIterator<AbstractInsnNode> iterator = this.findTargetMethodNode(method);
            if (iterator == null) continue;
            while (iterator.hasPrevious()) {
                AbstractInsnNode insnNode = iterator.previous();
                if (insnNode.getOpcode() != 185 && insnNode.getOpcode() != 182) continue;
                InsnList insnList = new InsnList();
                insnList.add(new InsnNode(87));
                insnList.add(new LdcInsnNode(DIALOG_TITLE));
                method.instructions.insert(insnNode, insnList);
                return;
            }
            throw new IllegalStateException("Failed to patch MoreOptionsDialog");
        }
    }

    private ListIterator<AbstractInsnNode> findTargetMethodNode(MethodNode methodNode) {
        if ((methodNode.access & 0x1000) == 0) {
            return null;
        }
        Iterator iterator = methodNode.instructions.iterator();
        while (iterator.hasNext()) {
            AbstractInsnNode instruction = (AbstractInsnNode)iterator.next();
            if (instruction.getOpcode() != 184 || !(instruction instanceof MethodInsnNode)) continue;
            MethodInsnNode methodInsnNode = (MethodInsnNode)instruction;
            if (!methodInsnNode.name.equals(TINYFD_METHOD_NAME)) continue;
            return iterator;
        }
        return null;
    }
}

