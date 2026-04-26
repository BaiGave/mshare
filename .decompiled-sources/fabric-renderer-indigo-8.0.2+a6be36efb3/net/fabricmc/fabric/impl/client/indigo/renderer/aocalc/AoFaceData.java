/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.indigo.renderer.aocalc;

class AoFaceData {
    float a0;
    float a1;
    float a2;
    float a3;
    int b0;
    int b1;
    int b2;
    int b3;
    int s0;
    int s1;
    int s2;
    int s3;

    AoFaceData() {
    }

    void l0(int l0) {
        this.b0 = l0 & 0xFFFF;
        this.s0 = l0 >>> 16 & 0xFFFF;
    }

    void l1(int l1) {
        this.b1 = l1 & 0xFFFF;
        this.s1 = l1 >>> 16 & 0xFFFF;
    }

    void l2(int l2) {
        this.b2 = l2 & 0xFFFF;
        this.s2 = l2 >>> 16 & 0xFFFF;
    }

    void l3(int l3) {
        this.b3 = l3 & 0xFFFF;
        this.s3 = l3 >>> 16 & 0xFFFF;
    }

    int weightedBlockLight(float[] w) {
        return (int)((float)this.b0 * w[0] + (float)this.b1 * w[1] + (float)this.b2 * w[2] + (float)this.b3 * w[3]) & 0xFF;
    }

    int weightedSkyLight(float[] w) {
        return (int)((float)this.s0 * w[0] + (float)this.s1 * w[1] + (float)this.s2 * w[2] + (float)this.s3 * w[3]) & 0xFF;
    }

    int weightedCombinedLight(float[] w) {
        return this.weightedSkyLight(w) << 16 | this.weightedBlockLight(w);
    }

    float weightedAo(float[] w) {
        return this.a0 * w[0] + this.a1 * w[1] + this.a2 * w[2] + this.a3 * w[3];
    }

    void toArrays(float[] aoOut, int[] lightOut, int[] vertexMap, int vertexOffset) {
        int i0 = vertexMap[vertexOffset];
        int i1 = vertexMap[(vertexOffset + 1) % 4];
        int i2 = vertexMap[(vertexOffset + 2) % 4];
        int i3 = vertexMap[(vertexOffset + 3) % 4];
        aoOut[i0] = this.a0;
        aoOut[i1] = this.a1;
        aoOut[i2] = this.a2;
        aoOut[i3] = this.a3;
        lightOut[i0] = this.s0 << 16 | this.b0;
        lightOut[i1] = this.s1 << 16 | this.b1;
        lightOut[i2] = this.s2 << 16 | this.b2;
        lightOut[i3] = this.s3 << 16 | this.b3;
    }

    static AoFaceData weightedMean(AoFaceData in0, float w0, AoFaceData in1, float w1, AoFaceData out) {
        out.a0 = in0.a0 * w0 + in1.a0 * w1;
        out.a1 = in0.a1 * w0 + in1.a1 * w1;
        out.a2 = in0.a2 * w0 + in1.a2 * w1;
        out.a3 = in0.a3 * w0 + in1.a3 * w1;
        out.b0 = (int)((float)in0.b0 * w0 + (float)in1.b0 * w1);
        out.b1 = (int)((float)in0.b1 * w0 + (float)in1.b1 * w1);
        out.b2 = (int)((float)in0.b2 * w0 + (float)in1.b2 * w1);
        out.b3 = (int)((float)in0.b3 * w0 + (float)in1.b3 * w1);
        out.s0 = (int)((float)in0.s0 * w0 + (float)in1.s0 * w1);
        out.s1 = (int)((float)in0.s1 * w0 + (float)in1.s1 * w1);
        out.s2 = (int)((float)in0.s2 * w0 + (float)in1.s2 * w1);
        out.s3 = (int)((float)in0.s3 * w0 + (float)in1.s3 * w1);
        return out;
    }
}

