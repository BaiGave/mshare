/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.math;

import org.joml.Quaternionf;
import org.joml.Vector3f;

@FunctionalInterface
public interface Axis {
    public static final Axis XN = angle -> new Quaternionf().rotationX(-angle);
    public static final Axis XP = angle -> new Quaternionf().rotationX(angle);
    public static final Axis YN = angle -> new Quaternionf().rotationY(-angle);
    public static final Axis YP = angle -> new Quaternionf().rotationY(angle);
    public static final Axis ZN = angle -> new Quaternionf().rotationZ(-angle);
    public static final Axis ZP = angle -> new Quaternionf().rotationZ(angle);

    public static Axis of(Vector3f vector) {
        return angle -> new Quaternionf().rotationAxis(angle, vector);
    }

    public Quaternionf rotation(float var1);

    default public Quaternionf rotationDegrees(float angle) {
        return this.rotation(angle * ((float)Math.PI / 180));
    }
}

