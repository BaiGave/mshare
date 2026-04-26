/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

@Environment(value=EnvType.CLIENT)
public class GoatRenderState
extends LivingEntityRenderState {
    public boolean hasLeftHorn = true;
    public boolean hasRightHorn = true;
    public float rammingXHeadRot;
}

