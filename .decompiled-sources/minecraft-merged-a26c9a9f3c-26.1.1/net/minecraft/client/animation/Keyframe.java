/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.animation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.animation.AnimationChannel;
import org.joml.Vector3fc;

@Environment(value=EnvType.CLIENT)
public record Keyframe(float timestamp, Vector3fc preTarget, Vector3fc postTarget, AnimationChannel.Interpolation interpolation) {
    public Keyframe(float timestamp, Vector3fc postTarget, AnimationChannel.Interpolation interpolation) {
        this(timestamp, postTarget, postTarget, interpolation);
    }
}

