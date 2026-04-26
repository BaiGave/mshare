/*
 * Decompiled with CFR 0.152.
 */
package org.spongepowered.asm.mixin;

import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.injection.selectors.ISelectorContext;

public final class FabricUtil {
    public static final String KEY_MOD_ID = "fabric-modId";
    public static final String KEY_COMPATIBILITY = "fabric-compat";
    public static final int COMPATIBILITY_0_9_2 = 9002;
    public static final int COMPATIBILITY_0_10_0 = 10000;
    public static final int COMPATIBILITY_0_14_0 = 14000;
    public static final int COMPATIBILITY_0_16_5 = 16005;
    public static final int COMPATIBILITY_0_17_0 = 17000;
    public static final int COMPATIBILITY_LATEST = 17000;

    public static String getModId(IMixinConfig config) {
        return FabricUtil.getModId(config, "(unknown)");
    }

    public static String getModId(IMixinConfig config, String defaultValue) {
        return FabricUtil.getDecoration(config, KEY_MOD_ID, defaultValue);
    }

    public static String getModId(ISelectorContext context) {
        return FabricUtil.getDecoration(FabricUtil.getConfig(context), KEY_MOD_ID, "(unknown)");
    }

    public static int getCompatibility(ISelectorContext context) {
        return FabricUtil.getDecoration(FabricUtil.getConfig(context), KEY_COMPATIBILITY, 17000);
    }

    private static IMixinConfig getConfig(ISelectorContext context) {
        return context.getMixin().getMixin().getConfig();
    }

    private static <T> T getDecoration(IMixinConfig config, String key, T defaultValue) {
        if (config.hasDecoration(key)) {
            return (T)config.getDecoration(key);
        }
        return defaultValue;
    }

    private FabricUtil() {
    }
}

