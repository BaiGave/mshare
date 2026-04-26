/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.indigo.renderer.aocalc;

import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoVertexClampFunction;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.QuadViewImpl;
import net.minecraft.core.Direction;

enum AoFace {
    DOWN(new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH}, new int[]{0, 1, 2, 3}){

        @Override
        void computeCornerWeights(QuadViewImpl q, int vertexIndex, float[] out) {
            float u = AoVertexClampFunction.CLAMP_FUNC.clamp(q.x(vertexIndex));
            float v = AoVertexClampFunction.CLAMP_FUNC.clamp(q.z(vertexIndex));
            out[0] = (1.0f - u) * v;
            out[1] = (1.0f - u) * (1.0f - v);
            out[2] = u * (1.0f - v);
            out[3] = u * v;
        }

        @Override
        float computeDepth(QuadViewImpl q, int vertexIndex) {
            return AoVertexClampFunction.CLAMP_FUNC.clamp(q.y(vertexIndex));
        }
    }
    ,
    UP(new Direction[]{Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH}, new int[]{2, 3, 0, 1}){

        @Override
        void computeCornerWeights(QuadViewImpl q, int vertexIndex, float[] out) {
            float u = AoVertexClampFunction.CLAMP_FUNC.clamp(q.x(vertexIndex));
            float v = AoVertexClampFunction.CLAMP_FUNC.clamp(q.z(vertexIndex));
            out[0] = u * v;
            out[1] = u * (1.0f - v);
            out[2] = (1.0f - u) * (1.0f - v);
            out[3] = (1.0f - u) * v;
        }

        @Override
        float computeDepth(QuadViewImpl q, int vertexIndex) {
            return 1.0f - AoVertexClampFunction.CLAMP_FUNC.clamp(q.y(vertexIndex));
        }
    }
    ,
    NORTH(new Direction[]{Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST}, new int[]{3, 0, 1, 2}){

        @Override
        void computeCornerWeights(QuadViewImpl q, int vertexIndex, float[] out) {
            float u = AoVertexClampFunction.CLAMP_FUNC.clamp(q.y(vertexIndex));
            float v = AoVertexClampFunction.CLAMP_FUNC.clamp(q.x(vertexIndex));
            out[0] = u * (1.0f - v);
            out[1] = u * v;
            out[2] = (1.0f - u) * v;
            out[3] = (1.0f - u) * (1.0f - v);
        }

        @Override
        float computeDepth(QuadViewImpl q, int vertexIndex) {
            return AoVertexClampFunction.CLAMP_FUNC.clamp(q.z(vertexIndex));
        }
    }
    ,
    SOUTH(new Direction[]{Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP}, new int[]{0, 1, 2, 3}){

        @Override
        void computeCornerWeights(QuadViewImpl q, int vertexIndex, float[] out) {
            float u = AoVertexClampFunction.CLAMP_FUNC.clamp(q.y(vertexIndex));
            float v = AoVertexClampFunction.CLAMP_FUNC.clamp(q.x(vertexIndex));
            out[0] = u * (1.0f - v);
            out[1] = (1.0f - u) * (1.0f - v);
            out[2] = (1.0f - u) * v;
            out[3] = u * v;
        }

        @Override
        float computeDepth(QuadViewImpl q, int vertexIndex) {
            return 1.0f - AoVertexClampFunction.CLAMP_FUNC.clamp(q.z(vertexIndex));
        }
    }
    ,
    WEST(new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH}, new int[]{3, 0, 1, 2}){

        @Override
        void computeCornerWeights(QuadViewImpl q, int vertexIndex, float[] out) {
            float u = AoVertexClampFunction.CLAMP_FUNC.clamp(q.y(vertexIndex));
            float v = AoVertexClampFunction.CLAMP_FUNC.clamp(q.z(vertexIndex));
            out[0] = u * v;
            out[1] = u * (1.0f - v);
            out[2] = (1.0f - u) * (1.0f - v);
            out[3] = (1.0f - u) * v;
        }

        @Override
        float computeDepth(QuadViewImpl q, int vertexIndex) {
            return AoVertexClampFunction.CLAMP_FUNC.clamp(q.x(vertexIndex));
        }
    }
    ,
    EAST(new Direction[]{Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH}, new int[]{1, 2, 3, 0}){

        @Override
        void computeCornerWeights(QuadViewImpl q, int vertexIndex, float[] out) {
            float u = AoVertexClampFunction.CLAMP_FUNC.clamp(q.y(vertexIndex));
            float v = AoVertexClampFunction.CLAMP_FUNC.clamp(q.z(vertexIndex));
            out[0] = (1.0f - u) * v;
            out[1] = (1.0f - u) * (1.0f - v);
            out[2] = u * (1.0f - v);
            out[3] = u * v;
        }

        @Override
        float computeDepth(QuadViewImpl q, int vertexIndex) {
            return 1.0f - AoVertexClampFunction.CLAMP_FUNC.clamp(q.x(vertexIndex));
        }
    };

    private static final AoFace[] VALUES;
    final Direction[] neighbors;
    final int[] vertexMap;

    private AoFace(Direction[] neighbors, int[] vertexMap) {
        this.neighbors = neighbors;
        this.vertexMap = vertexMap;
    }

    abstract void computeCornerWeights(QuadViewImpl var1, int var2, float[] var3);

    abstract float computeDepth(QuadViewImpl var1, int var2);

    static AoFace get(Direction direction) {
        return VALUES[direction.get3DDataValue()];
    }

    static {
        VALUES = AoFace.values();
    }
}

