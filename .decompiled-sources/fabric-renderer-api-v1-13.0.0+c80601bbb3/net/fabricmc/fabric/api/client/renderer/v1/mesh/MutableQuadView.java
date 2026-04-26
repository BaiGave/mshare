/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.renderer.v1.mesh;

import com.mojang.blaze3d.platform.Transparency;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadAtlas;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.ShadeMode;
import net.fabricmc.fabric.api.client.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.client.renderer.v1.render.ExtraLightCoordsUtil;
import net.fabricmc.fabric.api.util.TriState;
import net.fabricmc.fabric.impl.client.renderer.QuadSpriteBaker;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public interface MutableQuadView
extends QuadView {
    public static final int BAKE_ROTATE_NONE = 0;
    public static final int BAKE_ROTATE_90 = 1;
    public static final int BAKE_ROTATE_180 = 2;
    public static final int BAKE_ROTATE_270 = 3;
    public static final int BAKE_LOCK_UV = 4;
    public static final int BAKE_FLIP_U = 8;
    public static final int BAKE_FLIP_V = 16;
    public static final int BAKE_NORMALIZED = 32;
    public static final float CULL_FACE_EPSILON = 1.0E-5f;

    public MutableQuadView pos(int var1, float var2, float var3, float var4);

    default public MutableQuadView pos(int vertexIndex, Vector3f pos) {
        return this.pos(vertexIndex, pos.x, pos.y, pos.z);
    }

    default public MutableQuadView pos(int vertexIndex, Vector3fc pos) {
        return this.pos(vertexIndex, pos.x(), pos.y(), pos.z());
    }

    default public MutableQuadView translate(float x, float y, float z) {
        this.pos(0, this.x(0) + x, this.y(0) + y, this.z(0) + z);
        this.pos(1, this.x(1) + x, this.y(1) + y, this.z(1) + z);
        this.pos(2, this.x(2) + x, this.y(2) + y, this.z(2) + z);
        this.pos(3, this.x(3) + x, this.y(3) + y, this.z(3) + z);
        return this;
    }

    public MutableQuadView color(int var1, int var2);

    default public MutableQuadView color(int c0, int c1, int c2, int c3) {
        this.color(0, c0);
        this.color(1, c1);
        this.color(2, c2);
        this.color(3, c3);
        return this;
    }

    default public MutableQuadView multiplyColor(int color) {
        this.color(0, ARGB.multiply(this.color(0), color));
        this.color(1, ARGB.multiply(this.color(1), color));
        this.color(2, ARGB.multiply(this.color(2), color));
        this.color(3, ARGB.multiply(this.color(3), color));
        return this;
    }

    public MutableQuadView uv(int var1, float var2, float var3);

    default public MutableQuadView uv(int vertexIndex, Vector2f uv) {
        return this.uv(vertexIndex, uv.x, uv.y);
    }

    default public MutableQuadView uv(int vertexIndex, Vector2fc uv) {
        return this.uv(vertexIndex, uv.x(), uv.y());
    }

    default public MutableQuadView uvUnitSquare() {
        this.uv(0, 0.0f, 0.0f);
        this.uv(1, 0.0f, 1.0f);
        this.uv(2, 1.0f, 1.0f);
        this.uv(3, 1.0f, 0.0f);
        return this;
    }

    default public MutableQuadView materialBake(Material.Baked material, int bakeFlags) {
        QuadSpriteBaker.bakeSprite(this, material.sprite(), bakeFlags);
        this.postMaterialBake(material);
        return this;
    }

    default public MutableQuadView postMaterialBake(Material.Baked material) {
        QuadAtlas atlas = QuadAtlas.ofLocation(material.sprite().atlasLocation());
        if (atlas == null) {
            atlas = QuadAtlas.BLOCK;
        }
        this.atlas(atlas);
        this.animated(material.sprite().contents().isAnimated());
        Transparency transparency = material.forceTranslucent() ? Transparency.TRANSLUCENT : ModelHelper.computeTransparency(material.sprite(), this);
        ChunkSectionLayer layer = ChunkSectionLayer.byTransparency(transparency);
        RenderType itemRenderType = material.sprite().atlasLocation().equals(TextureAtlas.LOCATION_BLOCKS) ? (transparency.hasTranslucent() ? Sheets.translucentBlockItemSheet() : Sheets.cutoutBlockItemSheet()) : (transparency.hasTranslucent() ? Sheets.translucentItemSheet() : Sheets.cutoutItemSheet());
        this.chunkLayer(layer);
        this.itemRenderType(itemRenderType);
        return this;
    }

    public MutableQuadView lightmap(int var1, int var2);

    default public MutableQuadView lightmap(int l0, int l1, int l2, int l3) {
        this.lightmap(0, l0);
        this.lightmap(1, l1);
        this.lightmap(2, l2);
        this.lightmap(3, l3);
        return this;
    }

    default public MutableQuadView minLightmap(int lightmap) {
        this.lightmap(0, ExtraLightCoordsUtil.smoothMax(this.lightmap(0), lightmap));
        this.lightmap(1, ExtraLightCoordsUtil.smoothMax(this.lightmap(1), lightmap));
        this.lightmap(2, ExtraLightCoordsUtil.smoothMax(this.lightmap(2), lightmap));
        this.lightmap(3, ExtraLightCoordsUtil.smoothMax(this.lightmap(3), lightmap));
        return this;
    }

    public MutableQuadView normal(int var1, float var2, float var3, float var4);

    default public MutableQuadView normal(int vertexIndex, Vector3f normal) {
        return this.normal(vertexIndex, normal.x, normal.y, normal.z);
    }

    default public MutableQuadView normal(int vertexIndex, Vector3fc normal) {
        return this.normal(vertexIndex, normal.x(), normal.y(), normal.z());
    }

    public MutableQuadView nominalFace(@Nullable Direction var1);

    public MutableQuadView cullFace(@Nullable Direction var1);

    public MutableQuadView atlas(QuadAtlas var1);

    public MutableQuadView chunkLayer(ChunkSectionLayer var1);

    public MutableQuadView itemRenderType(RenderType var1);

    public MutableQuadView emissive(boolean var1);

    public MutableQuadView diffuseShade(boolean var1);

    public MutableQuadView ambientOcclusion(TriState var1);

    public MutableQuadView foilType( @Nullable ItemStackRenderState.FoilType var1);

    public MutableQuadView shadeMode(ShadeMode var1);

    public MutableQuadView animated(boolean var1);

    public MutableQuadView tintIndex(int var1);

    public MutableQuadView tag(int var1);

    public MutableQuadView copyFrom(QuadView var1);

    public MutableQuadView fromBakedQuad(BakedQuad var1);

    public MutableQuadView clear();

    default public MutableQuadView square(Direction nominalFace, float left, float bottom, float right, float top, float depth) {
        if (Math.abs(depth) < 1.0E-5f) {
            this.cullFace(nominalFace);
            depth = 0.0f;
        } else {
            this.cullFace(null);
        }
        this.nominalFace(nominalFace);
        switch (nominalFace) {
            case UP: {
                depth = 1.0f - depth;
                top = 1.0f - top;
                bottom = 1.0f - bottom;
            }
            case DOWN: {
                this.pos(0, left, depth, top);
                this.pos(1, left, depth, bottom);
                this.pos(2, right, depth, bottom);
                this.pos(3, right, depth, top);
                break;
            }
            case EAST: {
                depth = 1.0f - depth;
                left = 1.0f - left;
                right = 1.0f - right;
            }
            case WEST: {
                this.pos(0, depth, top, left);
                this.pos(1, depth, bottom, left);
                this.pos(2, depth, bottom, right);
                this.pos(3, depth, top, right);
                break;
            }
            case SOUTH: {
                depth = 1.0f - depth;
                left = 1.0f - left;
                right = 1.0f - right;
            }
            case NORTH: {
                this.pos(0, 1.0f - left, top, depth);
                this.pos(1, 1.0f - left, bottom, depth);
                this.pos(2, 1.0f - right, bottom, depth);
                this.pos(3, 1.0f - right, top, depth);
            }
        }
        return this;
    }
}

