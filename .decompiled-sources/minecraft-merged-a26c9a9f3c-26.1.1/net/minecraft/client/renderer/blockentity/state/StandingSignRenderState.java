/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.blockentity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.world.level.block.PlainSignBlock;

@Environment(value=EnvType.CLIENT)
public class StandingSignRenderState
extends SignRenderState {
    public PlainSignBlock.Attachment attachmentType = PlainSignBlock.Attachment.GROUND;
}

