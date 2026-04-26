/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.indigo;

import java.util.List;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class IndigoMixinConfigPlugin
implements IMixinConfigPlugin {
    private static final String JSON_KEY_DISABLE_INDIGO = "fabric-renderer-api-v1:contains_renderer";
    private static boolean needsLoad = true;
    private static boolean indigoApplicable = true;

    private static void loadIfNeeded() {
        if (needsLoad) {
            for (ModContainer container : FabricLoader.getInstance().getAllMods()) {
                ModMetadata meta = container.getMetadata();
                if (!meta.containsCustomValue(JSON_KEY_DISABLE_INDIGO)) continue;
                indigoApplicable = false;
            }
            needsLoad = false;
        }
    }

    static boolean shouldApplyIndigo() {
        IndigoMixinConfigPlugin.loadIfNeeded();
        return indigoApplicable;
    }

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return IndigoMixinConfigPlugin.shouldApplyIndigo();
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}

