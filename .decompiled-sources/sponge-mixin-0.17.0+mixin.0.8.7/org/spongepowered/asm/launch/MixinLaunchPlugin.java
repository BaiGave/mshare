/*
 * Decompiled with CFR 0.152.
 */
package org.spongepowered.asm.launch;

import cpw.mods.modlauncher.api.NamedPath;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import org.spongepowered.asm.launch.MixinLaunchPluginLegacy;

public class MixinLaunchPlugin
extends MixinLaunchPluginLegacy {
    public void initializeLaunch(ILaunchPluginService.ITransformerLoader transformerLoader, NamedPath[] specialPaths) {
        this.initializeLaunch(transformerLoader);
    }
}

