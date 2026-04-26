/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.model.loading.v1;

import com.google.common.collect.HashMultimap;
import com.mojang.logging.LogUtils;
import java.util.function.BiFunction;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedExtraModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadAtlas;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadView;
import net.fabricmc.fabric.api.client.renderer.v1.model.MeshQuadCollection;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;

public final class SimpleUnbakedExtraModel<T>
implements UnbakedExtraModel<T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Identifier model;
    private final BiFunction<ResolvedModel, ModelBaker, T> bake;

    public SimpleUnbakedExtraModel(Identifier model, BiFunction<ResolvedModel, ModelBaker, T> bake) {
        this.model = model;
        this.bake = bake;
    }

    public static SimpleUnbakedExtraModel<BlockStateModel> blockStateModel(Identifier model) {
        return SimpleUnbakedExtraModel.blockStateModel(model, BlockModelRotation.IDENTITY);
    }

    public static SimpleUnbakedExtraModel<BlockStateModel> blockStateModel(Identifier model, ModelState state) {
        return new SimpleUnbakedExtraModel<BlockStateModel>(model, (baked, baker) -> new SingleVariant(SimpleUnbakedExtraModel.bakeResolved(baker, baked, state)));
    }

    private static BlockStateModelPart bakeResolved(ModelBaker modelBakery, ResolvedModel model, ModelState state) {
        TextureSlots textureSlots = model.getTopTextureSlots();
        boolean hasAmbientOcclusion = model.getTopAmbientOcclusion();
        Material.Baked particleMaterial = model.resolveParticleMaterial(textureSlots, modelBakery);
        QuadCollection geometry = model.bakeTopGeometry(textureSlots, modelBakery, state);
        HashMultimap<Identifier, Identifier> forbiddenSprites = null;
        if (geometry instanceof MeshQuadCollection) {
            MeshQuadCollection meshQuadCollection = (MeshQuadCollection)geometry;
            MutableObject<Object> forbiddenSpritesRef = new MutableObject<Object>(forbiddenSprites);
            meshQuadCollection.getMesh().forEach(quad -> {
                if (quad.atlas() != QuadAtlas.BLOCK) {
                    HashMultimap<Identifier, Identifier> forbiddenSprites1 = (HashMultimap<Identifier, Identifier>)forbiddenSpritesRef.get();
                    if (forbiddenSprites1 == null) {
                        forbiddenSprites1 = HashMultimap.create();
                        forbiddenSpritesRef.setValue(forbiddenSprites1);
                    }
                    TextureAtlasSprite sprite = modelBakery.materials().spriteFinder(quad.atlas()).find((QuadView)quad);
                    forbiddenSprites1.put(sprite.atlasLocation(), sprite.contents().name());
                }
            });
            forbiddenSprites = (HashMultimap<Identifier, Identifier>)forbiddenSpritesRef.get();
        }
        for (BakedQuad bakedQuad : geometry.getAll()) {
            TextureAtlasSprite sprite = bakedQuad.materialInfo().sprite();
            if (sprite.atlasLocation().equals(TextureAtlas.LOCATION_BLOCKS)) continue;
            if (forbiddenSprites == null) {
                forbiddenSprites = HashMultimap.create();
            }
            forbiddenSprites.put(sprite.atlasLocation(), sprite.contents().name());
        }
        if (forbiddenSprites != null) {
            LOGGER.warn("Rejecting block model {}, since it contains sprites from outside of supported atlas: {}", (Object)model.debugName(), (Object)forbiddenSprites);
            return modelBakery.missingBlockModelPart();
        }
        return new SimpleModelWrapper(geometry, hasAmbientOcclusion, particleMaterial);
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
        resolver.markDependency(this.model);
    }

    @Override
    public T bake(ModelBaker baker) {
        return this.bake.apply(baker.getModel(this.model), baker);
    }
}

