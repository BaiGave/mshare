/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.launch.knot;

import org.spongepowered.asm.service.IMixinServiceBootstrap;

public class MixinServiceKnotBootstrap
implements IMixinServiceBootstrap {
    @Override
    public String getName() {
        return "Knot";
    }

    @Override
    public String getServiceClassName() {
        return "net.fabricmc.loader.impl.launch.knot.MixinServiceKnot";
    }

    @Override
    public void bootstrap() {
    }
}

