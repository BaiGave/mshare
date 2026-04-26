/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.indigo.renderer.helper;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadView;
import net.fabricmc.fabric.impl.client.indigo.renderer.helper.GeometryHelper;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

public final class NormalHelper {
    private static final float PACK = 127.0f;
    private static final float UNPACK = 0.007874016f;

    private NormalHelper() {
    }

    public static int packNormal(float x, float y, float z, float w) {
        x = Mth.clamp(x, -1.0f, 1.0f);
        y = Mth.clamp(y, -1.0f, 1.0f);
        z = Mth.clamp(z, -1.0f, 1.0f);
        w = Mth.clamp(w, -1.0f, 1.0f);
        return (int)(x * 127.0f) & 0xFF | ((int)(y * 127.0f) & 0xFF) << 8 | ((int)(z * 127.0f) & 0xFF) << 16 | ((int)(w * 127.0f) & 0xFF) << 24;
    }

    public static int packNormal(Vector3f normal, float w) {
        return NormalHelper.packNormal(normal.x(), normal.y(), normal.z(), w);
    }

    public static int packNormal(float x, float y, float z) {
        x = Mth.clamp(x, -1.0f, 1.0f);
        y = Mth.clamp(y, -1.0f, 1.0f);
        z = Mth.clamp(z, -1.0f, 1.0f);
        return (int)(x * 127.0f) & 0xFF | ((int)(y * 127.0f) & 0xFF) << 8 | ((int)(z * 127.0f) & 0xFF) << 16;
    }

    public static int packNormal(Vector3f normal) {
        return NormalHelper.packNormal(normal.x(), normal.y(), normal.z());
    }

    public static float unpackNormalX(int packedNormal) {
        return (float)((byte)(packedNormal & 0xFF)) * 0.007874016f;
    }

    public static float unpackNormalY(int packedNormal) {
        return (float)((byte)(packedNormal >>> 8 & 0xFF)) * 0.007874016f;
    }

    public static float unpackNormalZ(int packedNormal) {
        return (float)((byte)(packedNormal >>> 16 & 0xFF)) * 0.007874016f;
    }

    public static float unpackNormalW(int packedNormal) {
        return (float)((byte)(packedNormal >>> 24 & 0xFF)) * 0.007874016f;
    }

    public static void unpackNormal(int packedNormal, Vector3f target) {
        target.set(NormalHelper.unpackNormalX(packedNormal), NormalHelper.unpackNormalY(packedNormal), NormalHelper.unpackNormalZ(packedNormal));
    }

    public static void computeFaceNormal(Vector3f saveTo, QuadView q) {
        float normZ;
        float dx0;
        float dx1;
        float normY;
        float dy1;
        float dz0;
        Direction nominalFace = q.nominalFace();
        if (nominalFace != null && GeometryHelper.isQuadParallelToFace(nominalFace, q)) {
            Vec3i vec = nominalFace.getUnitVec3i();
            saveTo.set(vec.getX(), vec.getY(), vec.getZ());
            return;
        }
        float x0 = q.x(0);
        float y0 = q.y(0);
        float z0 = q.z(0);
        float x1 = q.x(1);
        float y1 = q.y(1);
        float z1 = q.z(1);
        float x2 = q.x(2);
        float y2 = q.y(2);
        float z2 = q.z(2);
        float x3 = q.x(3);
        float y3 = q.y(3);
        float dy0 = y2 - y0;
        float z3 = q.z(3);
        float dz1 = z3 - z1;
        float normX = dy0 * dz1 - (dz0 = z2 - z0) * (dy1 = y3 - y1);
        float l = (float)Math.sqrt(normX * normX + (normY = dz0 * (dx1 = x3 - x1) - (dx0 = x2 - x0) * dz1) * normY + (normZ = dx0 * dy1 - dy0 * dx1) * normZ);
        if (l != 0.0f) {
            normX /= l;
            normY /= l;
            normZ /= l;
        }
        saveTo.set(normX, normY, normZ);
    }
}

