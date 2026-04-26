/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.indigo.renderer.aocalc;

import net.fabricmc.fabric.impl.client.indigo.Indigo;
import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoConfig;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.QuadViewImpl;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3fc;

public class FlatLighter {
    private final BlockPos.MutableBlockPos scratchPos = new BlockPos.MutableBlockPos();
    private final BlockModelLighter.Cache lightCache;

    public FlatLighter(BlockModelLighter.Cache lightCache) {
        this.lightCache = lightCache;
    }

    public void applyDirectionalBrightness(CardinalLighting cardinalLighting, MutableQuadViewImpl quad, boolean vanillaShade) {
        block9: {
            block10: {
                float directionalBrightness;
                block12: {
                    block11: {
                        block8: {
                            if (quad.diffuseShade()) break block8;
                            float directionalBrightness2 = cardinalLighting.up();
                            if (directionalBrightness2 == 1.0f) break block9;
                            for (int i = 0; i < 4; ++i) {
                                quad.color(i, ARGB.scaleRGB(quad.color(i), directionalBrightness2));
                            }
                            break block9;
                        }
                        if ((Indigo.AMBIENT_OCCLUSION_MODE != AoConfig.HYBRID || vanillaShade) && Indigo.AMBIENT_OCCLUSION_MODE != AoConfig.ENHANCED) break block10;
                        if (!quad.hasAllVertexNormals()) break block11;
                        for (int i = 0; i < 4; ++i) {
                            float directionalBrightness3 = FlatLighter.normalShade(cardinalLighting, quad.normalX(i), quad.normalY(i), quad.normalZ(i));
                            quad.color(i, ARGB.scaleRGB(quad.color(i), directionalBrightness3));
                        }
                        break block9;
                    }
                    if ((quad.geometryFlags() & 2) != 0) {
                        directionalBrightness = cardinalLighting.byFace(quad.lightFace());
                    } else {
                        Vector3fc faceNormal = quad.faceNormal();
                        directionalBrightness = FlatLighter.normalShade(cardinalLighting, faceNormal.x(), faceNormal.y(), faceNormal.z());
                    }
                    if (!quad.hasVertexNormals()) break block12;
                    for (int i = 0; i < 4; ++i) {
                        float shade = quad.hasNormal(i) ? FlatLighter.normalShade(cardinalLighting, quad.normalX(i), quad.normalY(i), quad.normalZ(i)) : directionalBrightness;
                        quad.color(i, ARGB.scaleRGB(quad.color(i), shade));
                    }
                    break block9;
                }
                if (directionalBrightness == 1.0f) break block9;
                for (int i = 0; i < 4; ++i) {
                    quad.color(i, ARGB.scaleRGB(quad.color(i), directionalBrightness));
                }
                break block9;
            }
            float directionalBrightness = cardinalLighting.byFace(quad.lightFace());
            if (directionalBrightness != 1.0f) {
                for (int i = 0; i < 4; ++i) {
                    quad.color(i, ARGB.scaleRGB(quad.color(i), directionalBrightness));
                }
            }
        }
    }

    private static float normalShade(CardinalLighting cardinalLighting, float normalX, float normalY, float normalZ) {
        float sum = 0.0f;
        float div = 0.0f;
        if (normalX > 0.0f) {
            sum += normalX * cardinalLighting.byFace(Direction.EAST);
            div += normalX;
        } else if (normalX < 0.0f) {
            sum += -normalX * cardinalLighting.byFace(Direction.WEST);
            div -= normalX;
        }
        if (normalY > 0.0f) {
            sum += normalY * cardinalLighting.byFace(Direction.UP);
            div += normalY;
        } else if (normalY < 0.0f) {
            sum += -normalY * cardinalLighting.byFace(Direction.DOWN);
            div -= normalY;
        }
        if (normalZ > 0.0f) {
            sum += normalZ * cardinalLighting.byFace(Direction.SOUTH);
            div += normalZ;
        } else if (normalZ < 0.0f) {
            sum += -normalZ * cardinalLighting.byFace(Direction.NORTH);
            div -= normalZ;
        }
        return sum / div;
    }

    public int light(BlockAndTintGetter level, BlockState state, BlockPos pos, QuadViewImpl quad) {
        this.scratchPos.set(pos);
        if (quad.cullFace() != null) {
            this.scratchPos.move(quad.cullFace());
        } else {
            int flags = quad.geometryFlags();
            if ((flags & 4) != 0 || (flags & 2) != 0 && state.isCollisionShapeFullBlock(level, pos)) {
                this.scratchPos.move(quad.lightFace());
            }
        }
        return this.lightCache.getLightCoords(state, level, this.scratchPos);
    }
}

