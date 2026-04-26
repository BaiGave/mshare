/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.renderer.block.model;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadAtlas;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadView;
import net.fabricmc.fabric.api.client.renderer.v1.model.MeshQuadCollection;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={SimpleModelWrapper.class})
abstract class SimpleModelWrapperMixin
implements BlockStateModelPart {
    @Shadow
    @Final
    private QuadCollection quads;
    @Shadow
    @Final
    private boolean useAmbientOcclusion;

    SimpleModelWrapperMixin() {
    }

    @Inject(method={"bake"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/resources/model/geometry/QuadCollection;getAll()Ljava/util/List;")})
    private static void analyzeMesh(ModelBaker modelBakery, Identifier location, ModelState state, CallbackInfoReturnable<BlockStateModelPart> cir, @Local(name={"geometry"}) QuadCollection geometry, @Local(name={"forbiddenSprites"}) LocalRef<Multimap<Identifier, Identifier>> forbiddenSpritesRef) {
        if (geometry instanceof MeshQuadCollection) {
            MeshQuadCollection meshQuadCollection = (MeshQuadCollection)geometry;
            meshQuadCollection.getMesh().forEach(quad -> {
                if (quad.atlas() != QuadAtlas.BLOCK) {
                    HashMultimap<Identifier, Identifier> forbiddenSprites = (HashMultimap<Identifier, Identifier>)forbiddenSpritesRef.get();
                    if (forbiddenSprites == null) {
                        forbiddenSprites = HashMultimap.create();
                        forbiddenSpritesRef.set(forbiddenSprites);
                    }
                    TextureAtlasSprite sprite = modelBakery.materials().spriteFinder(quad.atlas()).find((QuadView)quad);
                    forbiddenSprites.put(sprite.atlasLocation(), sprite.contents().name());
                }
            });
        }
    }

    @Override
    public void emitQuads(QuadEmitter emitter, Predicate<@Nullable Direction> cullTest) {
        QuadCollection quadCollection = this.quads;
        if (quadCollection instanceof MeshQuadCollection) {
            MeshQuadCollection meshQuadCollection = (MeshQuadCollection)quadCollection;
            if (this.useAmbientOcclusion) {
                meshQuadCollection.getMesh().outputTo(emitter);
            } else {
                emitter.pushTransform(quad -> {
                    if (quad.ambientOcclusion() == TriState.DEFAULT) {
                        quad.ambientOcclusion(TriState.FALSE);
                    }
                    return true;
                });
                meshQuadCollection.getMesh().outputTo(emitter);
                emitter.popTransform();
            }
        } else {
            BlockStateModelPart.super.emitQuads(emitter, cullTest);
        }
    }
}

