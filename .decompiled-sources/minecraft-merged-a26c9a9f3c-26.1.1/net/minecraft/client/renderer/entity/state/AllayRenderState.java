/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;

@Environment(value=EnvType.CLIENT)
public class AllayRenderState
extends ArmedEntityRenderState {
    public boolean isDancing;
    public boolean isSpinning;
    public float spinningProgress;
    public float holdingAnimationProgress;
}

