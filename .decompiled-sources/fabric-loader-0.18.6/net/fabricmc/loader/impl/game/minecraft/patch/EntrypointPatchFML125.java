/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.game.minecraft.patch;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.function.Function;
import net.fabricmc.loader.impl.game.minecraft.patch.ModClassLoader_125_FML;
import net.fabricmc.loader.impl.game.patch.GamePatch;
import net.fabricmc.loader.impl.launch.FabricLauncher;
import net.fabricmc.loader.impl.launch.knot.Knot;
import net.fabricmc.loader.impl.util.LoaderUtil;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;

public class EntrypointPatchFML125
extends GamePatch {
    private static final String FROM = ModClassLoader_125_FML.class.getName();
    private static final String TO = "cpw.mods.fml.common.ModClassLoader";
    private static final String FROM_INTERNAL = FROM.replace('.', '/');
    private static final String TO_INTERNAL = "cpw/mods/fml/common/ModClassLoader";

    @Override
    public void process(FabricLauncher launcher, Function<String, ClassNode> classSource, Consumer<ClassNode> classEmitter) {
        if (classSource.apply(TO) != null && classSource.apply("cpw.mods.fml.relauncher.FMLRelauncher") == null) {
            ClassNode patchedClassLoader;
            block11: {
                if (!(launcher instanceof Knot)) {
                    throw new RuntimeException("1.2.5 FML patch only supported on Knot!");
                }
                Log.debug(LogCategory.GAME_PATCH, "Detected 1.2.5 FML - Knotifying ModClassLoader...");
                patchedClassLoader = new ClassNode();
                try (InputStream stream = launcher.getResourceAsStream(LoaderUtil.getClassFileName(FROM));){
                    if (stream != null) {
                        ClassReader patchedClassLoaderReader = new ClassReader(stream);
                        patchedClassLoaderReader.accept(patchedClassLoader, 0);
                        break block11;
                    }
                    throw new IOException("Could not find class " + FROM + " in the launcher classpath while transforming ModClassLoader");
                }
                catch (IOException e) {
                    throw new RuntimeException("An error occurred while reading class " + FROM + " while transforming ModClassLoader", e);
                }
            }
            ClassNode remappedClassLoader = new ClassNode();
            patchedClassLoader.accept(new ClassRemapper(remappedClassLoader, new Remapper(){

                @Override
                public String map(String internalName) {
                    return FROM_INTERNAL.equals(internalName) ? EntrypointPatchFML125.TO_INTERNAL : internalName;
                }
            }));
            classEmitter.accept(remappedClassLoader);
        }
    }
}

