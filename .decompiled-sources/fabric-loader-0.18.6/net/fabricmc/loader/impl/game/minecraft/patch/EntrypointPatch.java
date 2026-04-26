/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.game.minecraft.patch;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;
import net.fabricmc.loader.impl.game.minecraft.Hooks;
import net.fabricmc.loader.impl.game.minecraft.MinecraftGameProvider;
import net.fabricmc.loader.impl.game.patch.GamePatch;
import net.fabricmc.loader.impl.launch.FabricLauncher;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.fabricmc.loader.impl.util.version.VersionPredicateParser;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class EntrypointPatch
extends GamePatch {
    private static final VersionPredicate VERSION_1_19_4 = EntrypointPatch.createVersionPredicate(">=1.19.4-");
    private static final VersionPredicate VERSION_25w14craftmine = EntrypointPatch.createVersionPredicate("1.21.6-alpha.25.14.craftmine");
    private final MinecraftGameProvider gameProvider;

    public EntrypointPatch(MinecraftGameProvider gameProvider) {
        this.gameProvider = gameProvider;
    }

    private void finishEntrypoint(EnvType type, ListIterator<AbstractInsnNode> it) {
        String methodName = String.format("start%s", type == EnvType.CLIENT ? "Client" : "Server");
        it.add(new MethodInsnNode(184, Hooks.INTERNAL_NAME, methodName, "(Ljava/io/File;Ljava/lang/Object;)V", false));
    }

    @Override
    public void process(FabricLauncher launcher, Function<String, ClassNode> classSource, Consumer<ClassNode> classEmitter) {
        ClassNode gameClass;
        List<FieldNode> newGameFields;
        EnvType type = launcher.getEnvironmentType();
        String entrypoint = launcher.getEntrypoint();
        Version gameVersion = this.getGameVersion();
        if (!entrypoint.startsWith("net.minecraft.") && !entrypoint.startsWith("com.mojang.")) {
            return;
        }
        String gameEntrypoint = null;
        boolean serverHasFile = true;
        boolean isApplet = entrypoint.contains("Applet");
        ClassNode mainClass = classSource.apply(entrypoint);
        if (mainClass == null) {
            throw new RuntimeException("Could not load main class " + entrypoint + "!");
        }
        boolean is20w22aServerOrHigher = false;
        if (type == EnvType.CLIENT && (newGameFields = this.findFields(mainClass, f -> !this.isStatic(f.access) && f.desc.startsWith("L") && !f.desc.startsWith("Ljava/"))).size() == 1) {
            gameEntrypoint = Type.getType(newGameFields.get((int)0).desc).getClassName();
        }
        if (gameEntrypoint == null) {
            MethodInsnNode newGameInsn;
            MethodNode mainMethod = this.findMethod(mainClass, method -> method.name.equals("main") && method.desc.equals("([Ljava/lang/String;)V") && this.isPublicStatic(method.access));
            if (mainMethod == null) {
                throw new RuntimeException("Could not find main method in " + entrypoint + "!");
            }
            if (type == EnvType.CLIENT && mainMethod.instructions.size() < 10) {
                MethodInsnNode invocation = null;
                for (AbstractInsnNode insn2 : mainMethod.instructions) {
                    if (invocation == null && insn2.getType() == 5) {
                        MethodInsnNode methodInsn = (MethodInsnNode)insn2;
                        if (methodInsn.owner.equals(mainClass.name)) {
                            invocation = methodInsn;
                            continue;
                        }
                    }
                    if (insn2.getOpcode() <= 25 || insn2.getOpcode() == 177) continue;
                    invocation = null;
                    break;
                }
                if (invocation != null) {
                    MethodInsnNode reqMethod = invocation;
                    mainMethod = this.findMethod(mainClass, m -> m.name.equals(reqMethod.name) && m.desc.equals(reqMethod.desc));
                }
            } else if (type == EnvType.SERVER && (newGameInsn = (MethodInsnNode)this.findInsn(mainMethod, insn -> insn.getOpcode() == 183 && ((MethodInsnNode)insn).name.equals("<init>") && ((MethodInsnNode)insn).owner.equals(mainClass.name), false)) != null) {
                gameEntrypoint = newGameInsn.owner.replace('/', '.');
                serverHasFile = newGameInsn.desc.startsWith("(Ljava/io/File;");
            }
            if (gameEntrypoint == null) {
                newGameInsn = (MethodInsnNode)this.findInsn(mainMethod, type == EnvType.CLIENT ? insn -> (insn.getOpcode() == 183 || insn.getOpcode() == 182) && !((MethodInsnNode)insn).owner.startsWith("java/") : insn -> insn.getOpcode() == 183 && ((MethodInsnNode)insn).name.equals("<init>") && this.hasSuperClass(((MethodInsnNode)insn).owner, mainClass.name, classSource), true);
                if (newGameInsn == null && type == EnvType.SERVER) {
                    newGameInsn = (MethodInsnNode)this.findInsn(mainMethod, insn -> insn instanceof MethodInsnNode && insn.getOpcode() == 183 && this.hasStrInMethod(((MethodInsnNode)insn).owner, "<clinit>", "()V", "^[a-fA-F0-9]{40}$", classSource), false);
                }
                if (type == EnvType.SERVER && this.hasStrInMethod(mainClass.name, mainMethod.name, mainMethod.desc, "Safe mode active, only vanilla datapack will be loaded", classSource)) {
                    is20w22aServerOrHigher = true;
                    gameEntrypoint = mainClass.name;
                }
                if (newGameInsn != null) {
                    gameEntrypoint = newGameInsn.owner.replace('/', '.');
                    serverHasFile = newGameInsn.desc.startsWith("(Ljava/io/File;");
                }
            }
        }
        if (gameEntrypoint == null) {
            throw new RuntimeException("Could not find game constructor in " + entrypoint + "!");
        }
        Log.debug(LogCategory.GAME_PATCH, "Found game constructor: %s -> %s", entrypoint, gameEntrypoint);
        if (gameEntrypoint.equals(entrypoint) || is20w22aServerOrHigher) {
            gameClass = mainClass;
        } else {
            gameClass = classSource.apply(gameEntrypoint);
            if (gameClass == null) {
                throw new RuntimeException("Could not load game class " + gameEntrypoint + "!");
            }
        }
        MethodNode gameMethod = null;
        MethodNode gameConstructor = null;
        AbstractInsnNode lwjglLogNode = null;
        MethodInsnNode currentThreadNode = null;
        int gameMethodQuality = 0;
        if (!is20w22aServerOrHigher) {
            for (MethodNode gmCandidate : gameClass.methods) {
                String s;
                Object cst;
                MethodInsnNode methodInsn;
                if (gmCandidate.name.equals("<init>")) {
                    gameConstructor = gmCandidate;
                    if (gameMethodQuality < 1) {
                        gameMethod = gmCandidate;
                        gameMethodQuality = 1;
                    }
                }
                if (type == EnvType.CLIENT && !isApplet && gmCandidate.name.equals("run")) {
                    MethodInsnNode potentialInitInsn = null;
                    boolean hasFailedToStartLog = false;
                    block2: for (AbstractInsnNode insn3 : gmCandidate.instructions) {
                        if (insn3 instanceof MethodInsnNode && potentialInitInsn == null) {
                            methodInsn = (MethodInsnNode)insn3;
                            if (methodInsn.getOpcode() != 182 || !methodInsn.owner.equals(gameClass.name)) break;
                            potentialInitInsn = methodInsn;
                        }
                        if (insn3 instanceof LdcInsnNode && !hasFailedToStartLog) {
                            if (potentialInitInsn == null) break;
                            cst = ((LdcInsnNode)insn3).cst;
                            if (cst instanceof String && (s = (String)cst).equals("Failed to start RubyDung")) {
                                hasFailedToStartLog = true;
                            }
                            if (!hasFailedToStartLog) break;
                        }
                        if (potentialInitInsn == null || !hasFailedToStartLog) continue;
                        cst = gameClass.methods.iterator();
                        while (cst.hasNext()) {
                            MethodNode gm = cst.next();
                            if (!gm.name.equals(potentialInitInsn.name) || !gm.desc.equals(potentialInitInsn.desc)) continue;
                            gameMethod = gm;
                            gameMethodQuality = 2;
                            continue block2;
                        }
                    }
                }
                if (type != EnvType.CLIENT || isApplet || gameMethodQuality >= 2) continue;
                int qual = 2;
                boolean hasLwjglLog = false;
                for (AbstractInsnNode insn3 : gmCandidate.instructions) {
                    if (insn3.getOpcode() == 184 && insn3 instanceof MethodInsnNode) {
                        methodInsn = (MethodInsnNode)insn3;
                        if (!"currentThread".equals(methodInsn.name) || !"java/lang/Thread".equals(methodInsn.owner) || !"()Ljava/lang/Thread;".equals(methodInsn.desc)) continue;
                        currentThreadNode = methodInsn;
                        continue;
                    }
                    if (!(insn3 instanceof LdcInsnNode) || !((cst = ((LdcInsnNode)insn3).cst) instanceof String) || !(s = (String)cst).startsWith("LWJGL Version: ") && !s.startsWith("Backend library: ")) continue;
                    hasLwjglLog = true;
                    if (!"LWJGL Version: ".equals(s) && !"LWJGL Version: {}".equals(s) && !"Backend library: {}".equals(s)) break;
                    qual = 3;
                    lwjglLogNode = insn3;
                    break;
                }
                if (!hasLwjglLog) continue;
                gameMethod = gmCandidate;
                gameMethodQuality = qual;
            }
        } else {
            gameMethod = this.findMethod(mainClass, method -> method.name.equals("main") && method.desc.equals("([Ljava/lang/String;)V") && this.isPublicStatic(method.access));
        }
        if (gameMethod == null) {
            throw new RuntimeException("Could not find game constructor method in " + gameClass.name + "!");
        }
        boolean patched = false;
        Log.debug(LogCategory.GAME_PATCH, "Patching game constructor %s%s", gameMethod.name, gameMethod.desc);
        if (type == EnvType.SERVER) {
            Iterator it = gameMethod.instructions.iterator();
            if (!is20w22aServerOrHigher) {
                this.moveBefore((ListIterator<AbstractInsnNode>)it, 177);
                if (serverHasFile) {
                    it.add(new VarInsnNode(25, 1));
                } else {
                    it.add(new InsnNode(1));
                }
                it.add(new VarInsnNode(25, 0));
                this.finishEntrypoint(type, (ListIterator<AbstractInsnNode>)it);
                patched = true;
            } else {
                Log.debug(LogCategory.GAME_PATCH, "20w22a+ detected, patching main method...");
                LdcInsnNode serverPropertiesLdc = (LdcInsnNode)this.findInsn(gameMethod, insn -> insn instanceof LdcInsnNode && ((LdcInsnNode)insn).cst.equals("server.properties"), false);
                this.moveBefore((ListIterator<AbstractInsnNode>)it, serverPropertiesLdc);
                MethodNode serverStartMethod = this.findMethod(mainClass, method -> {
                    if ((method.access & 0x1000) == 0 || method.name.equals("main") && method.desc.equals("([Ljava/lang/String;)V") || VERSION_25w14craftmine.test(gameVersion) && method.parameters.size() < 10) {
                        return false;
                    }
                    Type methodReturnType = Type.getReturnType(method.desc);
                    return methodReturnType.getSort() != 1 && methodReturnType.getSort() != 0 && methodReturnType.getSort() == 10;
                });
                if (serverStartMethod == null) {
                    Log.debug(LogCategory.GAME_PATCH, "Detected 20w22a");
                } else {
                    Log.debug(LogCategory.GAME_PATCH, "Detected version above 20w22a");
                    AbstractInsnNode previous = serverPropertiesLdc.getPrevious();
                    while (true) {
                        if (previous == null) {
                            throw new RuntimeException("Failed to find static method before loading server properties");
                        }
                        if (previous.getOpcode() == 184) break;
                        previous = previous.getPrevious();
                    }
                    boolean foundNode = false;
                    while (it.hasPrevious()) {
                        if (it.previous() != previous) continue;
                        if (!it.hasPrevious()) break;
                        foundNode = true;
                        it.previous();
                        break;
                    }
                    if (!foundNode) {
                        throw new RuntimeException("Failed to find static method before loading server properties");
                    }
                }
                it.add(new InsnNode(1));
                it.add(new InsnNode(1));
                this.finishEntrypoint(type, (ListIterator<AbstractInsnNode>)it);
                if (serverStartMethod == null) {
                    Log.debug(LogCategory.GAME_PATCH, "Server game instance has not be implemented yet for 20w22a");
                } else {
                    Iterator serverStartIt = serverStartMethod.instructions.iterator();
                    MethodInsnNode dedicatedServerConstructor = (MethodInsnNode)this.findInsn(serverStartMethod, insn -> {
                        if (insn instanceof MethodInsnNode && ((MethodInsnNode)insn).name.equals("<init>")) {
                            Type constructorType = Type.getMethodType(((MethodInsnNode)insn).desc);
                            if (constructorType.getArgumentTypes().length <= 0) {
                                return false;
                            }
                            return constructorType.getArgumentTypes()[0].getDescriptor().equals("Ljava/lang/Thread;");
                        }
                        return false;
                    }, false);
                    if (dedicatedServerConstructor == null) {
                        throw new RuntimeException("Could not find dedicated server constructor");
                    }
                    this.moveAfter((ListIterator<AbstractInsnNode>)serverStartIt, dedicatedServerConstructor);
                    serverStartIt.add(new InsnNode(89));
                    serverStartIt.add(new MethodInsnNode(184, Hooks.INTERNAL_NAME, "setGameInstance", "(Ljava/lang/Object;)V", false));
                }
                patched = true;
            }
        } else if (type == EnvType.CLIENT && isApplet) {
            FieldNode runDirectory = this.findField(gameClass, f -> this.isStatic(f.access) && f.desc.equals("Ljava/io/File;"));
            if (runDirectory == null) {
                Log.warn(LogCategory.GAME_PATCH, "Could not find applet run directory! (If you're running pre-late-indev versions, this is fine.)");
                Iterator it = gameMethod.instructions.iterator();
                if (gameConstructor == gameMethod) {
                    this.moveBefore((ListIterator<AbstractInsnNode>)it, 177);
                }
                it.add(new InsnNode(1));
                it.add(new MethodInsnNode(184, "net/fabricmc/loader/impl/game/minecraft/applet/AppletMain", "hookGameDir", "(Ljava/io/File;)Ljava/io/File;", false));
                it.add(new VarInsnNode(25, 0));
                this.finishEntrypoint(type, (ListIterator<AbstractInsnNode>)it);
            } else {
                Iterator it = gameConstructor.instructions.iterator();
                this.moveAfter((ListIterator<AbstractInsnNode>)it, 183);
                it.add(new FieldInsnNode(178, gameClass.name, runDirectory.name, runDirectory.desc));
                it.add(new MethodInsnNode(184, "net/fabricmc/loader/impl/game/minecraft/applet/AppletMain", "hookGameDir", "(Ljava/io/File;)Ljava/io/File;", false));
                it.add(new FieldInsnNode(179, gameClass.name, runDirectory.name, runDirectory.desc));
                it = gameMethod.instructions.iterator();
                if (gameConstructor == gameMethod) {
                    this.moveBefore((ListIterator<AbstractInsnNode>)it, 177);
                }
                it.add(new FieldInsnNode(178, gameClass.name, runDirectory.name, runDirectory.desc));
                it.add(new VarInsnNode(25, 0));
                this.finishEntrypoint(type, (ListIterator<AbstractInsnNode>)it);
            }
            patched = true;
        } else {
            if (gameConstructor == null) {
                throw new RuntimeException("Non-applet client-side, but could not find constructor?");
            }
            Iterator consIt = gameConstructor.instructions.iterator();
            while (consIt.hasNext()) {
                AbstractInsnNode insn4 = (AbstractInsnNode)consIt.next();
                if (insn4.getOpcode() != 181 || !((FieldInsnNode)insn4).desc.equals("Ljava/io/File;")) continue;
                Log.debug(LogCategory.GAME_PATCH, "Run directory field is thought to be %s/%s", ((FieldInsnNode)insn4).owner, ((FieldInsnNode)insn4).name);
                Iterator it = gameMethod == gameConstructor ? consIt : gameMethod.instructions.iterator();
                if (currentThreadNode != null && VERSION_1_19_4.test(gameVersion)) {
                    this.moveBefore((ListIterator<AbstractInsnNode>)it, currentThreadNode);
                } else if (lwjglLogNode != null) {
                    this.moveBefore((ListIterator<AbstractInsnNode>)it, lwjglLogNode);
                    for (int i = 0; i < 4; ++i) {
                        this.moveBeforeType((ListIterator<AbstractInsnNode>)it, 5);
                    }
                }
                it.add(new VarInsnNode(25, 0));
                it.add(new FieldInsnNode(180, ((FieldInsnNode)insn4).owner, ((FieldInsnNode)insn4).name, ((FieldInsnNode)insn4).desc));
                it.add(new VarInsnNode(25, 0));
                this.finishEntrypoint(type, (ListIterator<AbstractInsnNode>)it);
                patched = true;
                break;
            }
            if (!patched && gameMethod != gameConstructor) {
                Iterator it = gameMethod.instructions.iterator();
                it.add(new InsnNode(1));
                it.add(new VarInsnNode(25, 0));
                this.finishEntrypoint(type, (ListIterator<AbstractInsnNode>)it);
                patched = true;
            }
        }
        if (!patched) {
            throw new RuntimeException("Game constructor patch not applied!");
        }
        if (gameClass != mainClass) {
            classEmitter.accept(gameClass);
        } else {
            classEmitter.accept(mainClass);
        }
        if (isApplet) {
            Hooks.appletMainClass = entrypoint;
        }
    }

    private boolean hasSuperClass(String cls, String superCls, Function<String, ClassNode> classSource) {
        if (cls.contains("$") || !cls.startsWith("net/minecraft") && cls.contains("/")) {
            return false;
        }
        ClassNode classNode = classSource.apply(cls);
        return classNode != null && classNode.superName.equals(superCls);
    }

    private boolean hasStrInMethod(String cls, String methodName, String methodDesc, String str, Function<String, ClassNode> classSource) {
        if (cls.contains("$") || !cls.startsWith("net/minecraft") && cls.contains("/")) {
            return false;
        }
        ClassNode node = classSource.apply(cls);
        if (node == null) {
            return false;
        }
        for (MethodNode method : node.methods) {
            if (!method.name.equals(methodName) || !method.desc.equals(methodDesc)) continue;
            for (AbstractInsnNode insn : method.instructions) {
                Object cst;
                if (!(insn instanceof LdcInsnNode) || !((cst = ((LdcInsnNode)insn).cst) instanceof String) || !cst.equals(str)) continue;
                return true;
            }
        }
        return false;
    }

    private Version getGameVersion() {
        try {
            return Version.parse(this.gameProvider.getNormalizedGameVersion());
        }
        catch (VersionParsingException e) {
            throw new RuntimeException(e);
        }
    }

    private static VersionPredicate createVersionPredicate(String predicate) {
        try {
            return VersionPredicateParser.parse(predicate);
        }
        catch (VersionParsingException e) {
            throw new RuntimeException(e);
        }
    }
}

