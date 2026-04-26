/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.renderer.v1.mesh;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadAtlas;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.ShadeMode;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public interface QuadView {
    public float x(int var1);

    public float y(int var1);

    public float z(int var1);

    public float posByIndex(int var1, int var2);

    public Vector3f copyPos(int var1, @Nullable Vector3f var2);

    public int color(int var1);

    public float u(int var1);

    public float v(int var1);

    public Vector2f copyUv(int var1, @Nullable Vector2f var2);

    public int lightmap(int var1);

    public boolean hasNormal(int var1);

    public float normalX(int var1);

    public float normalY(int var1);

    public float normalZ(int var1);

    public @Nullable Vector3f copyNormal(int var1, @Nullable Vector3f var2);

    public Vector3fc faceNormal();

    public Direction lightFace();

    public @Nullable Direction nominalFace();

    public @Nullable Direction cullFace();

    public QuadAtlas atlas();

    public ChunkSectionLayer chunkLayer();

    public RenderType itemRenderType();

    public boolean emissive();

    public boolean diffuseShade();

    public TriState ambientOcclusion();

    public  @Nullable ItemStackRenderState.FoilType foilType();

    public ShadeMode shadeMode();

    public boolean animated();

    public int tintIndex();

    public int tag();

    default public BakedQuad toBakedQuad(TextureAtlasSprite sprite) {
        Vector3f position0 = this.copyPos(0, null);
        Vector3f position1 = this.copyPos(1, null);
        Vector3f position2 = this.copyPos(2, null);
        Vector3f position3 = this.copyPos(3, null);
        long packedUV0 = UVPair.pack(this.u(0), this.v(0));
        long packedUV1 = UVPair.pack(this.u(1), this.v(1));
        long packedUV2 = UVPair.pack(this.u(2), this.v(2));
        long packedUV3 = UVPair.pack(this.u(3), this.v(3));
        int lightEmission = 15;
        if (!this.emissive()) {
            for (int i = 0; i < 4; ++i) {
                int lightmap = this.lightmap(i);
                if (lightmap == 0) {
                    lightEmission = 0;
                    break;
                }
                int blockLight = LightCoordsUtil.block(lightmap);
                int skyLight = LightCoordsUtil.sky(lightmap);
                lightEmission = Math.min(lightEmission, Math.min(blockLight, skyLight));
            }
        }
        BakedQuad.MaterialInfo materialInfo = new BakedQuad.MaterialInfo(sprite, this.chunkLayer(), this.itemRenderType(), this.tintIndex(), this.diffuseShade(), lightEmission);
        return new BakedQuad(position0, position1, position2, position3, packedUV0, packedUV1, packedUV2, packedUV3, this.lightFace(), materialInfo);
    }

    public void buffer(int var1, VertexConsumer var2);

    public void buffer(int var1, PoseStack.Pose var2, VertexConsumer var3);
}

