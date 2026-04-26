/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.transformer;

import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.game.GameProvider;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.fabricmc.loader.impl.transformer.ClassStripper;
import net.fabricmc.loader.impl.transformer.EnvironmentStrippingData;
import net.fabricmc.loader.impl.transformer.PackageAccessFixer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

public final class FabricTransformer {
    public static byte[] transform(boolean isDevelopment, EnvType envType, String name, byte[] bytes) {
        ClassWriter classWriter;
        boolean applyClassTweaker;
        Set<GameProvider.BuiltinTransform> transforms = FabricLoaderImpl.INSTANCE.getGameProvider().getBuiltinTransforms(name);
        boolean transformAccess = transforms.contains((Object)GameProvider.BuiltinTransform.WIDEN_ALL_PACKAGE_ACCESS) && FabricLauncherBase.getLauncher().getMappingConfiguration().requiresPackageAccessHack();
        boolean environmentStrip = transforms.contains((Object)GameProvider.BuiltinTransform.STRIP_ENVIRONMENT);
        boolean bl = applyClassTweaker = transforms.contains((Object)GameProvider.BuiltinTransform.CLASS_TWEAKS) && FabricLoaderImpl.INSTANCE.getClassTweaker().getTargets().contains(name.replace('.', '/'));
        if (!(transformAccess || environmentStrip || applyClassTweaker)) {
            return bytes;
        }
        ClassReader classReader = new ClassReader(bytes);
        ClassVisitor visitor = classWriter = new ClassWriter(classReader, 0);
        int visitorCount = 0;
        if (applyClassTweaker) {
            visitor = FabricLoaderImpl.INSTANCE.getClassTweaker().createClassVisitor(589824, visitor, null);
            ++visitorCount;
        }
        if (transformAccess) {
            visitor = new PackageAccessFixer(589824, visitor);
            ++visitorCount;
        }
        if (environmentStrip) {
            EnvironmentStrippingData stripData = new EnvironmentStrippingData(589824, envType.toString());
            classReader.accept(stripData, 5);
            if (stripData.stripEntireClass()) {
                throw new RuntimeException("Cannot load class " + name + " in environment type " + (Object)((Object)envType));
            }
            if (!stripData.isEmpty()) {
                visitor = new ClassStripper(589824, visitor, stripData.getStripInterfaces(), stripData.getStripFields(), stripData.getStripMethods());
                ++visitorCount;
            }
        }
        if (visitorCount <= 0) {
            return bytes;
        }
        classReader.accept(visitor, 0);
        return classWriter.toByteArray();
    }
}

