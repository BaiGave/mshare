/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.transfer.v1.fluid;

public final class FluidConstants {
    public static final long BUCKET = 81000L;
    public static final long BOTTLE = 27000L;
    public static final long BOWL = 27000L;
    public static final long BLOCK = 81000L;
    public static final long INGOT = 9000L;
    public static final long NUGGET = 1000L;
    public static final long DROPLET = 1L;
    public static final int WATER_TEMPERATURE = 300;
    public static final int LAVA_TEMPERATURE = 1300;
    public static final int WATER_VISCOSITY = 1000;
    public static final int LAVA_VISCOSITY = 6000;
    public static final int LAVA_VISCOSITY_NETHER = 2000;
    public static final int VISCOSITY_RATIO = 200;

    public static long fromBucketFraction(long numerator, long denominator) {
        long total = numerator * 81000L;
        if (total % denominator != 0L) {
            throw new IllegalArgumentException("Not a valid number of droplets!");
        }
        return total / denominator;
    }

    private FluidConstants() {
    }
}

