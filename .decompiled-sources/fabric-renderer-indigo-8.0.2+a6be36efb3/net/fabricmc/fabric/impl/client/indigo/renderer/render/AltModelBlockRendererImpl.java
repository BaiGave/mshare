/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.indigo.renderer.render;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadTransform;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.ShadeMode;
import net.fabricmc.fabric.api.client.renderer.v1.render.AltModelBlockRenderer;
import net.fabricmc.fabric.api.client.renderer.v1.render.ExtraLightCoordsUtil;
import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoCalculator;
import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.FlatLighter;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl;
import net.fabricmc.fabric.mixin.client.indigo.renderer.BlockModelLighterAccessor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

public class AltModelBlockRendererImpl
implements AltModelBlockRenderer,
QuadTransform {
    private final boolean ambientOcclusion;
    private final boolean cull;
    private final BlockColors blockColors;
    private final Predicate<@Nullable Direction> cullTest = this::shouldCullFace;
    private final AoCalculator aoCalc;
    private final FlatLighter flatLighter;
    private final RandomSource random = RandomSource.createThreadLocalInstance(0L);
    private final BlockPos.MutableBlockPos scratchPos = new BlockPos.MutableBlockPos();
    private int cacheValid;
    private int shouldCullFaceCache;
    private int tintCacheIndex = -1;
    private int tintCacheValue;
    private boolean tintSourcesInitialized;
    private final List<@Nullable BlockTintSource> tintSources = new ObjectArrayList<BlockTintSource>();
    private final IntList computedTintValues = new IntArrayList();
    private final Vector3f offset = new Vector3f();
    private BlockAndTintGetter level;
    private BlockPos pos;
    private BlockState blockState;
    private boolean defaultAo;

    public AltModelBlockRendererImpl(boolean ambientOcclusion, boolean cull, BlockColors blockColors) {
        this.ambientOcclusion = ambientOcclusion;
        this.cull = cull;
        this.blockColors = blockColors;
        BlockModelLighter.Cache lightCache = BlockModelLighterAccessor.fabric_getCACHE().get();
        this.aoCalc = new AoCalculator(lightCache);
        this.flatLighter = new FlatLighter(lightCache);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void tesselateBlock(QuadEmitter output, float x, float y, float z, BlockAndTintGetter level, BlockPos pos, BlockState blockState, BlockStateModel model, long seed) {
        Vec3 offset = blockState.getOffset(pos);
        this.offset.set((double)x + offset.x, (double)y + offset.y, (double)z + offset.z);
        this.level = level;
        this.pos = pos;
        this.blockState = blockState;
        this.defaultAo = this.ambientOcclusion && blockState.getLightEmission() == 0;
        this.cacheValid = 0;
        this.shouldCullFaceCache = 0;
        this.aoCalc.prepare(level, blockState, pos);
        this.random.setSeed(seed);
        output.clear();
        output.pushTransform(this);
        try {
            model.emitQuads(output, level, pos, blockState, this.random, this.cull ? this.cullTest : direction -> false);
        }
        finally {
            output.popTransform();
            this.level = null;
            this.aoCalc.clear();
            this.resetTintCache();
        }
    }

    @Override
    public boolean transform(MutableQuadView quad) {
        if (this.cull && this.shouldCullFace(quad.cullFace())) {
            return false;
        }
        this.shadeQuad((MutableQuadViewImpl)quad, this.ambientOcclusion && quad.ambientOcclusion().orElse(this.defaultAo), quad.emissive(), quad.shadeMode() == ShadeMode.VANILLA);
        this.tintQuad(quad);
        quad.translate(this.offset.x, this.offset.y, this.offset.z);
        return true;
    }

    private boolean shouldCullFace(@Nullable Direction direction) {
        if (direction == null) {
            return false;
        }
        int cacheMask = 1 << direction.ordinal();
        if ((this.cacheValid & cacheMask) == 0) {
            this.cacheValid |= cacheMask;
            BlockState neighborState = this.level.getBlockState(this.scratchPos.setWithOffset((Vec3i)this.pos, direction));
            if (!Block.shouldRenderFace(this.blockState, neighborState, direction)) {
                this.shouldCullFaceCache |= cacheMask;
                return true;
            }
            return false;
        }
        return (this.shouldCullFaceCache & cacheMask) != 0;
    }

    private void shadeQuad(MutableQuadViewImpl quad, boolean ao, boolean emissive, boolean vanillaShade) {
        if (ao) {
            this.aoCalc.compute(quad, vanillaShade);
            if (emissive) {
                for (int i = 0; i < 4; ++i) {
                    quad.color(i, ARGB.scaleRGB(quad.color(i), this.aoCalc.ao[i]));
                    quad.lightmap(i, 0xF000F0);
                }
            } else {
                for (int i = 0; i < 4; ++i) {
                    quad.color(i, ARGB.scaleRGB(quad.color(i), this.aoCalc.ao[i]));
                    quad.lightmap(i, ExtraLightCoordsUtil.smoothMax(quad.lightmap(i), this.aoCalc.light[i]));
                }
            }
        } else {
            if (emissive) {
                quad.lightmap(0xF000F0, 0xF000F0, 0xF000F0, 0xF000F0);
            } else {
                quad.minLightmap(this.flatLighter.light(this.level, this.blockState, this.pos, quad));
            }
            this.flatLighter.applyDirectionalBrightness(this.level.cardinalLighting(), quad, vanillaShade);
        }
    }

    private void tintQuad(MutableQuadView quad) {
        int tintIndex = quad.tintIndex();
        if (tintIndex != -1) {
            quad.multiplyColor(this.getTintColor(this.level, this.blockState, this.pos, tintIndex));
        }
    }

    private void configureTintCache(BlockState blockState) {
        List<BlockTintSource> tintSources = this.blockColors.getTintSources(blockState);
        int tintSourceCount = tintSources.size();
        if (tintSourceCount > 0) {
            this.tintSources.addAll(tintSources);
            for (int i = 0; i < tintSourceCount; ++i) {
                this.computedTintValues.add(-1);
            }
        }
    }

    private int computeTintColor(BlockAndTintGetter level, BlockState state, BlockPos pos, int tintIndex) {
        if (!this.tintSourcesInitialized) {
            this.configureTintCache(state);
            this.tintSourcesInitialized = true;
        }
        if (tintIndex >= this.tintSources.size()) {
            return -1;
        }
        BlockTintSource tintSource = this.tintSources.set(tintIndex, null);
        if (tintSource != null) {
            int computedTintValue = tintSource.colorInWorld(state, level, pos);
            this.computedTintValues.set(tintIndex, computedTintValue);
            return computedTintValue;
        }
        return this.computedTintValues.getInt(tintIndex);
    }

    private int getTintColor(BlockAndTintGetter level, BlockState state, BlockPos pos, int tintIndex) {
        if (this.tintCacheIndex == tintIndex) {
            return this.tintCacheValue;
        }
        int tintColor = this.computeTintColor(level, state, pos, tintIndex);
        this.tintCacheIndex = tintIndex;
        this.tintCacheValue = tintColor;
        return tintColor;
    }

    private void resetTintCache() {
        this.tintCacheIndex = -1;
        if (this.tintSourcesInitialized) {
            this.tintSources.clear();
            this.computedTintValues.clear();
            this.tintSourcesInitialized = false;
        }
    }
}

