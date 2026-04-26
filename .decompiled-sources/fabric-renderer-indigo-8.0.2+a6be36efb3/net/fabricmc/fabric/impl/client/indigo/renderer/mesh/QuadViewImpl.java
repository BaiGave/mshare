/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.indigo.renderer.mesh;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadAtlas;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.ShadeMode;
import net.fabricmc.fabric.api.util.TriState;
import net.fabricmc.fabric.impl.client.indigo.renderer.helper.GeometryHelper;
import net.fabricmc.fabric.impl.client.indigo.renderer.helper.NormalHelper;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.EncodingFormat;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

public class QuadViewImpl
implements QuadView {
    protected @Nullable Direction nominalFace;
    protected boolean isGeometryInvalid = true;
    protected final Vector3f faceNormal = new Vector3f();
    protected int[] data;
    protected int baseIndex = 0;
    private final Vector4f posVec = new Vector4f();
    private final Vector3f normalVec = new Vector3f();
    private final Vector3f normalVec1 = new Vector3f();

    public final void load() {
        this.isGeometryInvalid = false;
        this.nominalFace = this.lightFace();
        NormalHelper.unpackNormal(this.packedFaceNormal(), this.faceNormal);
    }

    protected final void computeGeometry() {
        if (this.isGeometryInvalid) {
            this.isGeometryInvalid = false;
            NormalHelper.computeFaceNormal(this.faceNormal, this);
            this.data[this.baseIndex + 1] = NormalHelper.packNormal(this.faceNormal);
            this.data[this.baseIndex + 0] = EncodingFormat.lightFace(this.data[this.baseIndex + 0], GeometryHelper.lightFace(this));
            this.data[this.baseIndex + 0] = EncodingFormat.geometryFlags(this.data[this.baseIndex + 0], GeometryHelper.computeShapeFlags(this));
        }
    }

    public final int geometryFlags() {
        this.computeGeometry();
        return EncodingFormat.geometryFlags(this.data[this.baseIndex + 0]);
    }

    @Override
    public final float x(int vertexIndex) {
        return Float.intBitsToFloat(this.data[this.baseIndex + vertexIndex * 8 + EncodingFormat.VERTEX_X]);
    }

    @Override
    public final float y(int vertexIndex) {
        return Float.intBitsToFloat(this.data[this.baseIndex + vertexIndex * 8 + EncodingFormat.VERTEX_Y]);
    }

    @Override
    public final float z(int vertexIndex) {
        return Float.intBitsToFloat(this.data[this.baseIndex + vertexIndex * 8 + EncodingFormat.VERTEX_Z]);
    }

    @Override
    public final float posByIndex(int vertexIndex, int coordinateIndex) {
        return Float.intBitsToFloat(this.data[this.baseIndex + vertexIndex * 8 + EncodingFormat.VERTEX_X + coordinateIndex]);
    }

    @Override
    public final Vector3f copyPos(int vertexIndex, @Nullable Vector3f target) {
        if (target == null) {
            target = new Vector3f();
        }
        int index = this.baseIndex + vertexIndex * 8 + EncodingFormat.VERTEX_X;
        target.set(Float.intBitsToFloat(this.data[index]), Float.intBitsToFloat(this.data[index + 1]), Float.intBitsToFloat(this.data[index + 2]));
        return target;
    }

    @Override
    public final int color(int vertexIndex) {
        return this.data[this.baseIndex + vertexIndex * 8 + EncodingFormat.VERTEX_COLOR];
    }

    @Override
    public final float u(int vertexIndex) {
        return Float.intBitsToFloat(this.data[this.baseIndex + vertexIndex * 8 + EncodingFormat.VERTEX_U]);
    }

    @Override
    public final float v(int vertexIndex) {
        return Float.intBitsToFloat(this.data[this.baseIndex + vertexIndex * 8 + EncodingFormat.VERTEX_V]);
    }

    @Override
    public final Vector2f copyUv(int vertexIndex, @Nullable Vector2f target) {
        if (target == null) {
            target = new Vector2f();
        }
        int index = this.baseIndex + vertexIndex * 8 + EncodingFormat.VERTEX_U;
        target.set(Float.intBitsToFloat(this.data[index]), Float.intBitsToFloat(this.data[index + 1]));
        return target;
    }

    @Override
    public final int lightmap(int vertexIndex) {
        return this.data[this.baseIndex + vertexIndex * 8 + EncodingFormat.VERTEX_LIGHTMAP];
    }

    public final int normalFlags() {
        return EncodingFormat.normalFlags(this.data[this.baseIndex + 0]);
    }

    @Override
    public final boolean hasNormal(int vertexIndex) {
        return (this.normalFlags() & 1 << vertexIndex) != 0;
    }

    public final boolean hasVertexNormals() {
        return this.normalFlags() != 0;
    }

    public final boolean hasAllVertexNormals() {
        return (this.normalFlags() & 0xF) == 15;
    }

    protected final int normalIndex(int vertexIndex) {
        return this.baseIndex + vertexIndex * 8 + EncodingFormat.VERTEX_NORMAL;
    }

    @Override
    public final float normalX(int vertexIndex) {
        return this.hasNormal(vertexIndex) ? NormalHelper.unpackNormalX(this.data[this.normalIndex(vertexIndex)]) : Float.NaN;
    }

    @Override
    public final float normalY(int vertexIndex) {
        return this.hasNormal(vertexIndex) ? NormalHelper.unpackNormalY(this.data[this.normalIndex(vertexIndex)]) : Float.NaN;
    }

    @Override
    public final float normalZ(int vertexIndex) {
        return this.hasNormal(vertexIndex) ? NormalHelper.unpackNormalZ(this.data[this.normalIndex(vertexIndex)]) : Float.NaN;
    }

    @Override
    public final @Nullable Vector3f copyNormal(int vertexIndex, @Nullable Vector3f target) {
        if (this.hasNormal(vertexIndex)) {
            if (target == null) {
                target = new Vector3f();
            }
            int normal = this.data[this.normalIndex(vertexIndex)];
            NormalHelper.unpackNormal(normal, target);
            return target;
        }
        return null;
    }

    public final int packedFaceNormal() {
        this.computeGeometry();
        return this.data[this.baseIndex + 1];
    }

    @Override
    public final Vector3fc faceNormal() {
        this.computeGeometry();
        return this.faceNormal;
    }

    @Override
    public final Direction lightFace() {
        this.computeGeometry();
        return EncodingFormat.lightFace(this.data[this.baseIndex + 0]);
    }

    @Override
    public final @Nullable Direction nominalFace() {
        return this.nominalFace;
    }

    @Override
    public final @Nullable Direction cullFace() {
        return EncodingFormat.cullFace(this.data[this.baseIndex + 0]);
    }

    @Override
    public QuadAtlas atlas() {
        return EncodingFormat.quadAtlas(this.data[this.baseIndex + 0]);
    }

    @Override
    public ChunkSectionLayer chunkLayer() {
        return EncodingFormat.chunkLayer(this.data[this.baseIndex + 0]);
    }

    @Override
    public RenderType itemRenderType() {
        return EncodingFormat.itemRenderType(this.data[this.baseIndex + 0]);
    }

    @Override
    public boolean emissive() {
        return EncodingFormat.emissive(this.data[this.baseIndex + 0]);
    }

    @Override
    public boolean diffuseShade() {
        return EncodingFormat.diffuseShade(this.data[this.baseIndex + 0]);
    }

    @Override
    public TriState ambientOcclusion() {
        return EncodingFormat.ambientOcclusion(this.data[this.baseIndex + 0]);
    }

    @Override
    public  @Nullable ItemStackRenderState.FoilType foilType() {
        return EncodingFormat.foilType(this.data[this.baseIndex + 0]);
    }

    @Override
    public ShadeMode shadeMode() {
        return EncodingFormat.shadeMode(this.data[this.baseIndex + 0]);
    }

    @Override
    public boolean animated() {
        return EncodingFormat.animated(this.data[this.baseIndex + 0]);
    }

    @Override
    public final int tintIndex() {
        return this.data[this.baseIndex + 2];
    }

    @Override
    public final int tag() {
        return this.data[this.baseIndex + 3];
    }

    @Override
    public final void buffer(int overlayCoords, VertexConsumer vertexConsumer) {
        if (!this.hasVertexNormals()) {
            Vector3fc faceNormal = this.faceNormal();
            for (int i = 0; i < 4; ++i) {
                vertexConsumer.addVertex(this.x(i), this.y(i), this.z(i), this.color(i), this.u(i), this.v(i), overlayCoords, this.lightmap(i), faceNormal.x(), faceNormal.y(), faceNormal.z());
            }
        } else if (this.hasAllVertexNormals()) {
            Vector3f normalVec = this.normalVec;
            for (int i = 0; i < 4; ++i) {
                this.copyNormal(i, normalVec);
                vertexConsumer.addVertex(this.x(i), this.y(i), this.z(i), this.color(i), this.u(i), this.v(i), overlayCoords, this.lightmap(i), normalVec.x(), normalVec.y(), normalVec.z());
            }
        } else {
            Vector3f normalVec = this.normalVec;
            Vector3fc faceNormal = this.faceNormal();
            for (int i = 0; i < 4; ++i) {
                if (this.hasNormal(i)) {
                    this.copyNormal(i, normalVec);
                } else {
                    normalVec.set(faceNormal);
                }
                vertexConsumer.addVertex(this.x(i), this.y(i), this.z(i), this.color(i), this.u(i), this.v(i), overlayCoords, this.lightmap(i), normalVec.x(), normalVec.y(), normalVec.z());
            }
        }
    }

    @Override
    public final void buffer(int overlayCoords, PoseStack.Pose pose, VertexConsumer vertexConsumer) {
        Vector4f posVec = this.posVec;
        Vector3f normalVec = this.normalVec;
        Matrix4f posMatrix = pose.pose();
        if (!this.hasVertexNormals()) {
            pose.transformNormal(this.faceNormal(), normalVec);
            for (int i = 0; i < 4; ++i) {
                posVec.set(this.x(i), this.y(i), this.z(i), 1.0f);
                posVec.mul(posMatrix);
                vertexConsumer.addVertex(posVec.x(), posVec.y(), posVec.z(), this.color(i), this.u(i), this.v(i), overlayCoords, this.lightmap(i), normalVec.x(), normalVec.y(), normalVec.z());
            }
        } else if (this.hasAllVertexNormals()) {
            for (int i = 0; i < 4; ++i) {
                posVec.set(this.x(i), this.y(i), this.z(i), 1.0f);
                posVec.mul(posMatrix);
                this.copyNormal(i, normalVec);
                pose.transformNormal(normalVec, normalVec);
                vertexConsumer.addVertex(posVec.x(), posVec.y(), posVec.z(), this.color(i), this.u(i), this.v(i), overlayCoords, this.lightmap(i), normalVec.x(), normalVec.y(), normalVec.z());
            }
        } else {
            Vector3f transformedFaceNormal = pose.transformNormal(this.faceNormal(), this.normalVec1);
            for (int i = 0; i < 4; ++i) {
                posVec.set(this.x(i), this.y(i), this.z(i), 1.0f);
                posVec.mul(posMatrix);
                if (this.hasNormal(i)) {
                    this.copyNormal(i, normalVec);
                    pose.transformNormal(normalVec, normalVec);
                } else {
                    normalVec.set(transformedFaceNormal);
                }
                vertexConsumer.addVertex(posVec.x(), posVec.y(), posVec.z(), this.color(i), this.u(i), this.v(i), overlayCoords, this.lightmap(i), normalVec.x(), normalVec.y(), normalVec.z());
            }
        }
    }
}

