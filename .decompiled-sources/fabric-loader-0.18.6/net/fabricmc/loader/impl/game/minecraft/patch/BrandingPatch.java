/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.game.minecraft.patch;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;
import net.fabricmc.loader.impl.game.minecraft.Hooks;
import net.fabricmc.loader.impl.game.patch.GamePatch;
import net.fabricmc.loader.impl.launch.FabricLauncher;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public final class BrandingPatch
extends GamePatch {
    @Override
    public void process(FabricLauncher launcher, Function<String, ClassNode> classSource, Consumer<ClassNode> classEmitter) {
        for (String brandClassName : new String[]{"net.minecraft.client.ClientBrandRetriever", "net.minecraft.server.MinecraftServer"}) {
            ClassNode brandClass = classSource.apply(brandClassName);
            if (brandClass == null || !this.applyBrandingPatch(brandClass)) continue;
            classEmitter.accept(brandClass);
        }
    }

    private boolean applyBrandingPatch(ClassNode classNode) {
        boolean applied = false;
        for (MethodNode node : classNode.methods) {
            if (!node.name.equals("getClientModName") && (!node.name.equals("getServerModName") || !node.desc.endsWith(")Ljava/lang/String;"))) continue;
            Log.debug(LogCategory.GAME_PATCH, "Applying brand name hook to %s::%s", classNode.name, node.name);
            Iterator it = node.instructions.iterator();
            while (it.hasNext()) {
                if (((AbstractInsnNode)it.next()).getOpcode() != 176) continue;
                it.previous();
                it.add(new MethodInsnNode(184, Hooks.INTERNAL_NAME, "insertBranding", "(Ljava/lang/String;)Ljava/lang/String;", false));
                it.next();
            }
            applied = true;
        }
        return applied;
    }
}

