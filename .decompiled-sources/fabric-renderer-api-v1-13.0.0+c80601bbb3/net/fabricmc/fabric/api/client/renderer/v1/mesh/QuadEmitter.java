/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.renderer.v1.mesh;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadAtlas;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadTransform;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.ShadeMode;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public interface QuadEmitter
extends MutableQuadView {
    @Override
    public QuadEmitter pos(int var1, float var2, float var3, float var4);

    @Override
    default public QuadEmitter pos(int vertexIndex, Vector3f pos) {
        MutableQuadView.super.pos(vertexIndex, pos);
        return this;
    }

    @Override
    default public QuadEmitter pos(int vertexIndex, Vector3fc pos) {
        MutableQuadView.super.pos(vertexIndex, pos);
        return this;
    }

    @Override
    default public QuadEmitter translate(float x, float y, float z) {
        MutableQuadView.super.translate(x, y, z);
        return this;
    }

    @Override
    public QuadEmitter color(int var1, int var2);

    @Override
    default public QuadEmitter color(int c0, int c1, int c2, int c3) {
        MutableQuadView.super.color(c0, c1, c2, c3);
        return this;
    }

    @Override
    default public QuadEmitter multiplyColor(int color) {
        MutableQuadView.super.multiplyColor(color);
        return this;
    }

    @Override
    public QuadEmitter uv(int var1, float var2, float var3);

    @Override
    default public QuadEmitter uv(int vertexIndex, Vector2f uv) {
        MutableQuadView.super.uv(vertexIndex, uv);
        return this;
    }

    @Override
    default public QuadEmitter uv(int vertexIndex, Vector2fc uv) {
        MutableQuadView.super.uv(vertexIndex, uv);
        return this;
    }

    @Override
    default public MutableQuadView uvUnitSquare() {
        MutableQuadView.super.uvUnitSquare();
        return this;
    }

    @Override
    default public QuadEmitter materialBake(Material.Baked material, int bakeFlags) {
        MutableQuadView.super.materialBake(material, bakeFlags);
        return this;
    }

    @Override
    default public QuadEmitter postMaterialBake(Material.Baked material) {
        MutableQuadView.super.postMaterialBake(material);
        return this;
    }

    @Override
    public QuadEmitter lightmap(int var1, int var2);

    @Override
    default public QuadEmitter lightmap(int l0, int l1, int l2, int l3) {
        MutableQuadView.super.lightmap(l0, l1, l2, l3);
        return this;
    }

    @Override
    default public QuadEmitter minLightmap(int lightmap) {
        MutableQuadView.super.minLightmap(lightmap);
        return this;
    }

    @Override
    public QuadEmitter normal(int var1, float var2, float var3, float var4);

    @Override
    default public QuadEmitter normal(int vertexIndex, Vector3f normal) {
        MutableQuadView.super.normal(vertexIndex, normal);
        return this;
    }

    @Override
    default public QuadEmitter normal(int vertexIndex, Vector3fc normal) {
        MutableQuadView.super.normal(vertexIndex, normal);
        return this;
    }

    @Override
    public QuadEmitter nominalFace(@Nullable Direction var1);

    @Override
    public QuadEmitter cullFace(@Nullable Direction var1);

    @Override
    public QuadEmitter atlas(QuadAtlas var1);

    @Override
    public QuadEmitter chunkLayer(ChunkSectionLayer var1);

    @Override
    public QuadEmitter itemRenderType(RenderType var1);

    @Override
    public QuadEmitter emissive(boolean var1);

    @Override
    public QuadEmitter diffuseShade(boolean var1);

    @Override
    public QuadEmitter ambientOcclusion(TriState var1);

    @Override
    public QuadEmitter foilType( @Nullable ItemStackRenderState.FoilType var1);

    @Override
    public QuadEmitter shadeMode(ShadeMode var1);

    @Override
    public QuadEmitter animated(boolean var1);

    @Override
    public QuadEmitter tintIndex(int var1);

    @Override
    public QuadEmitter tag(int var1);

    @Override
    public QuadEmitter copyFrom(QuadView var1);

    @Override
    public QuadEmitter fromBakedQuad(BakedQuad var1);

    @Override
    public QuadEmitter clear();

    @Override
    default public QuadEmitter square(Direction nominalFace, float left, float bottom, float right, float top, float depth) {
        MutableQuadView.super.square(nominalFace, left, bottom, right, top, depth);
        return this;
    }

    public void pushTransform(QuadTransform var1);

    public void popTransform();

    public QuadEmitter emit();
}

