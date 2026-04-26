/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.indigo.renderer.aocalc;

import com.mojang.blaze3d.vertex.QuadInstance;
import net.fabricmc.fabric.impl.client.indigo.Indigo;
import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoConfig;
import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoFace;
import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoFaceData;
import net.fabricmc.fabric.impl.client.indigo.renderer.helper.GeometryHelper;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.QuadViewImpl;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AoCalculator {
    private static final Logger LOGGER = LoggerFactory.getLogger(AoCalculator.class);
    private final BlockModelLighter.Cache lightCache;
    private final BlockPos.MutableBlockPos lightPos = new BlockPos.MutableBlockPos();
    private final BlockPos.MutableBlockPos searchPos = new BlockPos.MutableBlockPos();
    private BlockAndTintGetter level;
    private BlockState state;
    private BlockPos pos;
    private final AoFaceData[] faceData = new AoFaceData[24];
    private int completionFlags = 0;
    private final float[] w = new float[4];
    public final float[] ao = new float[4];
    public final int[] light = new int[4];
    private final BlockModelLighter vanillaCalc = new BlockModelLighter();
    private final QuadInstance vanillaQuadInstance = new QuadInstance();
    private final Vector3f vanillaPos0 = new Vector3f();
    private final Vector3f vanillaPos1 = new Vector3f();
    private final Vector3f vanillaPos2 = new Vector3f();
    private final Vector3f vanillaPos3 = new Vector3f();
    private final BakedQuad.MaterialInfo vanillaMaterialInfo = new BakedQuad.MaterialInfo(null, ChunkSectionLayer.SOLID, Sheets.cutoutBlockItemSheet(), -1, true, 0);
    private final AoFaceData tmpFace = new AoFaceData();
    private final Vector3f vertexNormal = new Vector3f();

    public AoCalculator(BlockModelLighter.Cache lightCache) {
        this.lightCache = lightCache;
        for (int i = 0; i < 24; ++i) {
            this.faceData[i] = new AoFaceData();
        }
    }

    public void prepare(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        this.level = level;
        this.state = state;
        this.pos = pos;
        this.completionFlags = 0;
    }

    public void clear() {
        this.level = null;
    }

    public void compute(QuadViewImpl quad, boolean vanillaShade) {
        AoConfig config = Indigo.AMBIENT_OCCLUSION_MODE;
        switch (config) {
            case VANILLA: {
                this.calcVanilla(quad);
                break;
            }
            case EMULATE: {
                this.calcFastVanilla(quad);
                break;
            }
            case HYBRID: {
                if (vanillaShade) {
                    this.calcFastVanilla(quad);
                    break;
                }
                this.calcEnhanced(quad);
                break;
            }
            case ENHANCED: {
                this.calcEnhanced(quad);
            }
        }
        if (Indigo.DEBUG_COMPARE_LIGHTING && vanillaShade && (config == AoConfig.EMULATE || config == AoConfig.HYBRID)) {
            float[] vanillaAo = new float[4];
            int[] vanillaLight = new int[4];
            this.calcVanilla(quad, vanillaAo, vanillaLight);
            for (int i = 0; i < 4; ++i) {
                if (this.light[i] == vanillaLight[i] && Mth.equal(this.ao[i], vanillaAo[i])) continue;
                LOGGER.info(String.format("Mismatch for %s @ %s", this.state.toString(), this.pos.toString()));
                LOGGER.info(String.format("Flags = %d, LightFace = %s", quad.geometryFlags(), quad.lightFace().toString()));
                LOGGER.info(String.format("    Old Brightness: %.2f, %.2f, %.2f, %.2f", Float.valueOf(vanillaAo[0]), Float.valueOf(vanillaAo[1]), Float.valueOf(vanillaAo[2]), Float.valueOf(vanillaAo[3])));
                LOGGER.info(String.format("    New Brightness: %.2f, %.2f, %.2f, %.2f", Float.valueOf(this.ao[0]), Float.valueOf(this.ao[1]), Float.valueOf(this.ao[2]), Float.valueOf(this.ao[3])));
                LOGGER.info(String.format("    Old Light: %s, %s, %s, %s", Integer.toHexString(vanillaLight[0]), Integer.toHexString(vanillaLight[1]), Integer.toHexString(vanillaLight[2]), Integer.toHexString(vanillaLight[3])));
                LOGGER.info(String.format("    New Light: %s, %s, %s, %s", Integer.toHexString(this.light[0]), Integer.toHexString(this.light[1]), Integer.toHexString(this.light[2]), Integer.toHexString(this.light[3])));
                break;
            }
        }
    }

    private void calcVanilla(QuadViewImpl quad) {
        this.calcVanilla(quad, this.ao, this.light);
    }

    private void calcVanilla(QuadViewImpl quad, float[] aoDest, int[] lightDest) {
        BakedQuad bakedQuad = new BakedQuad(quad.copyPos(0, this.vanillaPos0), quad.copyPos(1, this.vanillaPos1), quad.copyPos(2, this.vanillaPos2), quad.copyPos(3, this.vanillaPos3), 0L, 0L, 0L, 0L, quad.lightFace(), this.vanillaMaterialInfo);
        this.vanillaCalc.prepareQuadAmbientOcclusion(this.level, this.state, this.pos, bakedQuad, this.vanillaQuadInstance);
        for (int i = 0; i < 4; ++i) {
            aoDest[i] = ARGB.redFloat(this.vanillaQuadInstance.getColor(i));
            lightDest[i] = this.vanillaQuadInstance.getLightCoords(i);
        }
    }

    private void calcFastVanilla(QuadViewImpl quad) {
        boolean isOnLightFace;
        int flags = quad.geometryFlags();
        boolean bl = isOnLightFace = (flags & 4) != 0;
        if (!isOnLightFace && (flags & 2) != 0 && this.state.isCollisionShapeFullBlock(this.level, this.pos)) {
            isOnLightFace = true;
        }
        if ((flags & 1) == 0) {
            this.vanillaPartialFace(quad, quad.lightFace(), isOnLightFace, quad.diffuseShade());
        } else {
            this.vanillaFullFace(quad, quad.lightFace(), isOnLightFace, quad.diffuseShade());
        }
    }

    private void calcEnhanced(QuadViewImpl quad) {
        switch (quad.geometryFlags()) {
            case 7: {
                this.vanillaFullFace(quad, quad.lightFace(), true, quad.diffuseShade());
                break;
            }
            case 6: {
                this.vanillaPartialFace(quad, quad.lightFace(), true, quad.diffuseShade());
                break;
            }
            case 3: {
                this.blendedFullFace(quad, quad.lightFace(), quad.diffuseShade());
                break;
            }
            case 2: {
                this.blendedPartialFace(quad, quad.lightFace(), quad.diffuseShade());
                break;
            }
            default: {
                this.irregularFace(quad, quad.diffuseShade());
            }
        }
    }

    private void fullFace(QuadViewImpl quad, Direction lightFace, AoFaceData faceData) {
        faceData.toArrays(this.ao, this.light, AoFace.get((Direction)lightFace).vertexMap, GeometryHelper.firstCubicVertex(quad));
    }

    private void partialFace(QuadViewImpl quad, Direction lightFace, AoFaceData faceData) {
        AoFace aoFace = AoFace.get(lightFace);
        float[] w = this.w;
        for (int i = 0; i < 4; ++i) {
            aoFace.computeCornerWeights(quad, i, w);
            this.light[i] = faceData.weightedCombinedLight(w);
            this.ao[i] = faceData.weightedAo(w);
        }
    }

    private void vanillaFullFace(QuadViewImpl quad, Direction lightFace, boolean isOnLightFace, boolean shade) {
        this.fullFace(quad, lightFace, this.computeFace(lightFace, isOnLightFace, shade));
    }

    private void vanillaPartialFace(QuadViewImpl quad, Direction lightFace, boolean isOnLightFace, boolean shade) {
        this.partialFace(quad, lightFace, this.computeFace(lightFace, isOnLightFace, shade));
    }

    private AoFaceData blendedInsetFace(QuadViewImpl quad, int vertexIndex, Direction lightFace, boolean shade) {
        float w1 = AoFace.get(lightFace).computeDepth(quad, vertexIndex);
        float w0 = 1.0f - w1;
        return AoFaceData.weightedMean(this.computeFace(lightFace, true, shade), w0, this.computeFace(lightFace, false, shade), w1, this.tmpFace);
    }

    private AoFaceData gatherInsetFace(QuadViewImpl quad, int vertexIndex, Direction lightFace, boolean shade) {
        float w1 = AoFace.get(lightFace).computeDepth(quad, vertexIndex);
        if (Mth.equal(w1, 0.0f)) {
            return this.computeFace(lightFace, true, shade);
        }
        if (Mth.equal(w1, 1.0f)) {
            return this.computeFace(lightFace, false, shade);
        }
        float w0 = 1.0f - w1;
        return AoFaceData.weightedMean(this.computeFace(lightFace, true, shade), w0, this.computeFace(lightFace, false, shade), w1, this.tmpFace);
    }

    private void blendedFullFace(QuadViewImpl quad, Direction lightFace, boolean shade) {
        this.fullFace(quad, lightFace, this.blendedInsetFace(quad, 0, lightFace, shade));
    }

    private void blendedPartialFace(QuadViewImpl quad, Direction lightFace, boolean shade) {
        this.partialFace(quad, lightFace, this.blendedInsetFace(quad, 0, lightFace, shade));
    }

    private void irregularFace(QuadViewImpl quad, boolean shade) {
        Vector3fc faceNorm = quad.faceNormal();
        float[] w = this.w;
        float[] aoResult = this.ao;
        int[] lightResult = this.light;
        for (int i = 0; i < 4; ++i) {
            float z;
            float y;
            Vector3fc normal = quad.hasNormal(i) ? quad.copyNormal(i, this.vertexNormal) : faceNorm;
            float ao = 0.0f;
            float sky = 0.0f;
            float block = 0.0f;
            float maxAo = 0.0f;
            int maxSky = 0;
            int maxBlock = 0;
            float x = normal.x();
            if (!Mth.equal(0.0f, x)) {
                Direction face = x > 0.0f ? Direction.EAST : Direction.WEST;
                AoFaceData fd = this.gatherInsetFace(quad, i, face, shade);
                AoFace.get(face).computeCornerWeights(quad, i, w);
                float n = x * x;
                float a = fd.weightedAo(w);
                int s = fd.weightedSkyLight(w);
                int b = fd.weightedBlockLight(w);
                ao += n * a;
                sky += n * (float)s;
                block += n * (float)b;
                maxAo = a;
                maxSky = s;
                maxBlock = b;
            }
            if (!Mth.equal(0.0f, y = normal.y())) {
                Direction face = y > 0.0f ? Direction.UP : Direction.DOWN;
                AoFaceData fd = this.gatherInsetFace(quad, i, face, shade);
                AoFace.get(face).computeCornerWeights(quad, i, w);
                float n = y * y;
                float a = fd.weightedAo(w);
                int s = fd.weightedSkyLight(w);
                int b = fd.weightedBlockLight(w);
                ao += n * a;
                sky += n * (float)s;
                block += n * (float)b;
                maxAo = Math.max(maxAo, a);
                maxSky = Math.max(maxSky, s);
                maxBlock = Math.max(maxBlock, b);
            }
            if (!Mth.equal(0.0f, z = normal.z())) {
                Direction face = z > 0.0f ? Direction.SOUTH : Direction.NORTH;
                AoFaceData fd = this.gatherInsetFace(quad, i, face, shade);
                AoFace.get(face).computeCornerWeights(quad, i, w);
                float n = z * z;
                float a = fd.weightedAo(w);
                int s = fd.weightedSkyLight(w);
                int b = fd.weightedBlockLight(w);
                ao += n * a;
                sky += n * (float)s;
                block += n * (float)b;
                maxAo = Math.max(maxAo, a);
                maxSky = Math.max(maxSky, s);
                maxBlock = Math.max(maxBlock, b);
            }
            aoResult[i] = (ao + maxAo) * 0.5f;
            lightResult[i] = ((int)((sky + (float)maxSky) * 0.5f) & 0xFF) << 16 | (int)((block + (float)maxBlock) * 0.5f) & 0xFF;
        }
    }

    private AoFaceData computeFace(Direction lightFace, boolean isOnBlockFace, boolean shade) {
        int faceDataIndex = shade ? (isOnBlockFace ? lightFace.get3DDataValue() : lightFace.get3DDataValue() + 6) : (isOnBlockFace ? lightFace.get3DDataValue() + 12 : lightFace.get3DDataValue() + 18);
        int mask = 1 << faceDataIndex;
        AoFaceData result = this.faceData[faceDataIndex];
        if ((this.completionFlags & mask) == 0) {
            this.completionFlags |= mask;
            this.computeFace(result, lightFace, isOnBlockFace, shade);
        }
        return result;
    }

    private void computeFace(AoFaceData result, Direction lightFace, boolean isOnBlockFace, boolean shade) {
        boolean isClearCenter;
        int lightCenter;
        boolean cIsClear3;
        int cLight3;
        float cAo3;
        boolean cIsClear2;
        int cLight2;
        float cAo2;
        boolean cIsClear1;
        int cLight1;
        float cAo1;
        boolean cIsClear0;
        int cLight0;
        float cAo0;
        boolean isClear3;
        BlockAndTintGetter level = this.level;
        BlockPos pos = this.pos;
        BlockState blockState = this.state;
        BlockPos.MutableBlockPos lightPos = this.lightPos;
        BlockPos.MutableBlockPos searchPos = this.searchPos;
        if (isOnBlockFace) {
            lightPos.setWithOffset((Vec3i)pos, lightFace);
        } else {
            lightPos.set(pos);
        }
        AoFace aoFace = AoFace.get(lightFace);
        searchPos.setWithOffset((Vec3i)lightPos, aoFace.neighbors[0]);
        BlockState searchState = level.getBlockState(searchPos);
        int light0 = this.lightCache.getLightCoords(searchState, level, searchPos);
        float ao0 = this.lightCache.getShadeBrightness(searchState, level, searchPos);
        if (!Indigo.FIX_SMOOTH_LIGHTING_OFFSET) {
            searchPos.move(lightFace);
            searchState = level.getBlockState(searchPos);
        }
        boolean isClear0 = !searchState.isViewBlocking(level, searchPos) || searchState.getLightDampening() == 0;
        searchPos.setWithOffset((Vec3i)lightPos, aoFace.neighbors[1]);
        searchState = level.getBlockState(searchPos);
        int light1 = this.lightCache.getLightCoords(searchState, level, searchPos);
        float ao1 = this.lightCache.getShadeBrightness(searchState, level, searchPos);
        if (!Indigo.FIX_SMOOTH_LIGHTING_OFFSET) {
            searchPos.move(lightFace);
            searchState = level.getBlockState(searchPos);
        }
        boolean isClear1 = !searchState.isViewBlocking(level, searchPos) || searchState.getLightDampening() == 0;
        searchPos.setWithOffset((Vec3i)lightPos, aoFace.neighbors[2]);
        searchState = level.getBlockState(searchPos);
        int light2 = this.lightCache.getLightCoords(searchState, level, searchPos);
        float ao2 = this.lightCache.getShadeBrightness(searchState, level, searchPos);
        if (!Indigo.FIX_SMOOTH_LIGHTING_OFFSET) {
            searchPos.move(lightFace);
            searchState = level.getBlockState(searchPos);
        }
        boolean isClear2 = !searchState.isViewBlocking(level, searchPos) || searchState.getLightDampening() == 0;
        searchPos.setWithOffset((Vec3i)lightPos, aoFace.neighbors[3]);
        searchState = level.getBlockState(searchPos);
        int light3 = this.lightCache.getLightCoords(searchState, level, searchPos);
        float ao3 = this.lightCache.getShadeBrightness(searchState, level, searchPos);
        if (!Indigo.FIX_SMOOTH_LIGHTING_OFFSET) {
            searchPos.move(lightFace);
            searchState = level.getBlockState(searchPos);
        }
        boolean bl = isClear3 = !searchState.isViewBlocking(level, searchPos) || searchState.getLightDampening() == 0;
        if (!isClear2 && !isClear0) {
            cAo0 = ao0;
            cLight0 = light0;
            cIsClear0 = false;
        } else {
            searchPos.setWithOffset((Vec3i)lightPos, aoFace.neighbors[0]).move(aoFace.neighbors[2]);
            searchState = level.getBlockState(searchPos);
            cAo0 = this.lightCache.getShadeBrightness(searchState, level, searchPos);
            cLight0 = this.lightCache.getLightCoords(searchState, level, searchPos);
            boolean bl2 = cIsClear0 = !searchState.isViewBlocking(level, searchPos) || searchState.getLightDampening() == 0;
        }
        if (!isClear3 && !isClear0) {
            cAo1 = ao0;
            cLight1 = light0;
            cIsClear1 = false;
        } else {
            searchPos.setWithOffset((Vec3i)lightPos, aoFace.neighbors[0]).move(aoFace.neighbors[3]);
            searchState = level.getBlockState(searchPos);
            cAo1 = this.lightCache.getShadeBrightness(searchState, level, searchPos);
            cLight1 = this.lightCache.getLightCoords(searchState, level, searchPos);
            boolean bl3 = cIsClear1 = !searchState.isViewBlocking(level, searchPos) || searchState.getLightDampening() == 0;
        }
        if (!isClear2 && !isClear1) {
            cAo2 = ao1;
            cLight2 = light1;
            cIsClear2 = false;
        } else {
            searchPos.setWithOffset((Vec3i)lightPos, aoFace.neighbors[1]).move(aoFace.neighbors[2]);
            searchState = level.getBlockState(searchPos);
            cAo2 = this.lightCache.getShadeBrightness(searchState, level, searchPos);
            cLight2 = this.lightCache.getLightCoords(searchState, level, searchPos);
            boolean bl4 = cIsClear2 = !searchState.isViewBlocking(level, searchPos) || searchState.getLightDampening() == 0;
        }
        if (!isClear3 && !isClear1) {
            cAo3 = ao1;
            cLight3 = light1;
            cIsClear3 = false;
        } else {
            searchPos.setWithOffset((Vec3i)lightPos, aoFace.neighbors[1]).move(aoFace.neighbors[3]);
            searchState = level.getBlockState(searchPos);
            cAo3 = this.lightCache.getShadeBrightness(searchState, level, searchPos);
            cLight3 = this.lightCache.getLightCoords(searchState, level, searchPos);
            cIsClear3 = !searchState.isViewBlocking(level, searchPos) || searchState.getLightDampening() == 0;
        }
        searchPos.setWithOffset((Vec3i)pos, lightFace);
        searchState = level.getBlockState(searchPos);
        if (isOnBlockFace && !searchState.isSolidRender()) {
            lightCenter = this.lightCache.getLightCoords(searchState, level, searchPos);
            isClearCenter = !searchState.isViewBlocking(level, searchPos) || searchState.getLightDampening() == 0;
        } else {
            lightCenter = this.lightCache.getLightCoords(blockState, level, pos);
            isClearCenter = !blockState.isViewBlocking(level, pos) || blockState.getLightDampening() == 0;
        }
        float aoCenter = this.lightCache.getShadeBrightness(level.getBlockState(lightPos), level, lightPos);
        float shadeBrightness = shade ? level.cardinalLighting().byFace(lightFace) : level.cardinalLighting().up();
        result.a0 = (ao3 + ao0 + cAo1 + aoCenter) * 0.25f * shadeBrightness;
        result.a1 = (ao2 + ao0 + cAo0 + aoCenter) * 0.25f * shadeBrightness;
        result.a2 = (ao2 + ao1 + cAo2 + aoCenter) * 0.25f * shadeBrightness;
        result.a3 = (ao3 + ao1 + cAo3 + aoCenter) * 0.25f * shadeBrightness;
        result.l0(AoCalculator.meanLight(light3, light0, cLight1, lightCenter, isClear3, isClear0, cIsClear1, isClearCenter));
        result.l1(AoCalculator.meanLight(light2, light0, cLight0, lightCenter, isClear2, isClear0, cIsClear0, isClearCenter));
        result.l2(AoCalculator.meanLight(light2, light1, cLight2, lightCenter, isClear2, isClear1, cIsClear2, isClearCenter));
        result.l3(AoCalculator.meanLight(light3, light1, cLight3, lightCenter, isClear3, isClear1, cIsClear3, isClearCenter));
    }

    private static int meanLight(int lightA, int lightB, int lightC, int lightD, boolean isClearA, boolean isClearB, boolean isClearC, boolean isClearD) {
        if (Indigo.FIX_MEAN_LIGHT_CALCULATION) {
            int lightABlock = lightA & 0xFFFF;
            int lightASky = lightA >>> 16 & 0xFFFF;
            int lightBBlock = lightB & 0xFFFF;
            int lightBSky = lightB >>> 16 & 0xFFFF;
            int lightCBlock = lightC & 0xFFFF;
            int lightCSky = lightC >>> 16 & 0xFFFF;
            int lightDBlock = lightD & 0xFFFF;
            int lightDSky = lightD >>> 16 & 0xFFFF;
            int minBlock = 65536;
            int minSky = 65536;
            if (isClearA) {
                minBlock = lightABlock;
                minSky = lightASky;
            }
            if (isClearB) {
                minBlock = Math.min(minBlock, lightBBlock);
                minSky = Math.min(minSky, lightBSky);
            }
            if (isClearC) {
                minBlock = Math.min(minBlock, lightCBlock);
                minSky = Math.min(minSky, lightCSky);
            }
            if (isClearD) {
                minBlock = Math.min(minBlock, lightDBlock);
                minSky = Math.min(minSky, lightDSky);
            }
            lightA = Math.max(lightASky, minSky &= 0xFFFF) << 16 | Math.max(lightABlock, minBlock &= 0xFFFF);
            lightB = Math.max(lightBSky, minSky) << 16 | Math.max(lightBBlock, minBlock);
            lightC = Math.max(lightCSky, minSky) << 16 | Math.max(lightCBlock, minBlock);
            lightD = Math.max(lightDSky, minSky) << 16 | Math.max(lightDBlock, minBlock);
            return AoCalculator.meanInnerLight(lightA, lightB, lightC, lightD);
        }
        return AoCalculator.vanillaMeanLight(lightA, lightB, lightC, lightD);
    }

    private static int vanillaMeanLight(int a, int b, int c, int d) {
        if (a == 0) {
            a = d;
        }
        if (b == 0) {
            b = d;
        }
        if (c == 0) {
            c = d;
        }
        return a + b + c + d >> 2 & 0xFF00FF;
    }

    private static int meanInnerLight(int a, int b, int c, int d) {
        return a + b + c + d >> 2 & 0xFF00FF;
    }
}

