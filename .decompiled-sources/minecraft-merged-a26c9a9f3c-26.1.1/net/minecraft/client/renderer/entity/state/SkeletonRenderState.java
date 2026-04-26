/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

@Environment(value=EnvType.CLIENT)
public class SkeletonRenderState
extends HumanoidRenderState {
    public boolean isAggressive;
    public boolean isShaking;
    public boolean isHoldingBow;
}

