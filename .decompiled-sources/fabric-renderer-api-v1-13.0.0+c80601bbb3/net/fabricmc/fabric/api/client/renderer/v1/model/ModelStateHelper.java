/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.renderer.v1.model;

import com.mojang.math.MatrixUtil;
import com.mojang.math.Transformation;
import java.util.EnumMap;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadTransform;
import net.fabricmc.fabric.api.client.renderer.v1.sprite.SpriteFinder;
import net.fabricmc.fabric.api.client.renderer.v1.sprite.SpriteFinderGetter;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockMath;
import net.minecraft.core.Direction;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

public final class ModelStateHelper {
    private static final Direction[] DIRECTIONS = Direction.values();

    private ModelStateHelper() {
    }

    public static ModelState of(final Transformation transformation, boolean uvLock) {
        Matrix4fc matrix = transformation.getMatrix();
        if (MatrixUtil.isIdentity(matrix)) {
            return BlockModelRotation.IDENTITY;
        }
        if (!uvLock) {
            return new ModelState(){

                @Override
                public Transformation transformation() {
                    return transformation;
                }
            };
        }
        final EnumMap<Direction, Matrix4fc> faceTransformations = new EnumMap<Direction, Matrix4fc>(Direction.class);
        final EnumMap<Direction, Matrix4f> inverseFaceTransformations = new EnumMap<Direction, Matrix4f>(Direction.class);
        for (Direction face : DIRECTIONS) {
            Matrix4fc faceTransformation = BlockMath.getFaceTransformation(transformation, face).getMatrix();
            faceTransformations.put(face, faceTransformation);
            inverseFaceTransformations.put(face, faceTransformation.invert(new Matrix4f()));
        }
        return new ModelState(){

            @Override
            public Transformation transformation() {
                return transformation;
            }

            @Override
            public Matrix4fc faceTransformation(Direction face) {
                return (Matrix4fc)faceTransformations.get(face);
            }

            @Override
            public Matrix4fc inverseFaceTransformation(Direction face) {
                return (Matrix4fc)inverseFaceTransformations.get(face);
            }
        };
    }

    public static ModelState multiply(ModelState left, ModelState right) {
        if (MatrixUtil.isIdentity(left.transformation().getMatrix())) {
            return right;
        }
        if (MatrixUtil.isIdentity(right.transformation().getMatrix())) {
            return left;
        }
        final Transformation transformation = left.transformation().compose(right.transformation());
        boolean leftHasFaceTransformations = false;
        boolean rightHasFaceTransformations = false;
        for (Direction face : DIRECTIONS) {
            if (!leftHasFaceTransformations && !MatrixUtil.isIdentity(left.faceTransformation(face))) {
                leftHasFaceTransformations = true;
            }
            if (rightHasFaceTransformations || MatrixUtil.isIdentity(right.faceTransformation(face))) continue;
            rightHasFaceTransformations = true;
        }
        if (leftHasFaceTransformations & rightHasFaceTransformations) {
            final EnumMap<Direction, Matrix4f> faceTransformations = new EnumMap<Direction, Matrix4f>(Direction.class);
            final EnumMap<Direction, Matrix4f> inverseFaceTransformations = new EnumMap<Direction, Matrix4f>(Direction.class);
            for (Direction face : DIRECTIONS) {
                faceTransformations.put(face, left.faceTransformation(face).mul(right.faceTransformation(face), new Matrix4f()));
                inverseFaceTransformations.put(face, right.inverseFaceTransformation(face).mul(left.inverseFaceTransformation(face), new Matrix4f()));
            }
            return new ModelState(){

                @Override
                public Transformation transformation() {
                    return transformation;
                }

                @Override
                public Matrix4fc faceTransformation(Direction face) {
                    return (Matrix4fc)faceTransformations.get(face);
                }

                @Override
                public Matrix4fc inverseFaceTransformation(Direction face) {
                    return (Matrix4fc)inverseFaceTransformations.get(face);
                }
            };
        }
        final ModelState faceTransformDelegate = leftHasFaceTransformations ? left : right;
        return new ModelState(){

            @Override
            public Transformation transformation() {
                return transformation;
            }

            @Override
            public Matrix4fc faceTransformation(Direction face) {
                return faceTransformDelegate.faceTransformation(face);
            }

            @Override
            public Matrix4fc inverseFaceTransformation(Direction face) {
                return faceTransformDelegate.inverseFaceTransformation(face);
            }
        };
    }

    public static QuadTransform asQuadTransform(ModelState state, SpriteFinderGetter spriteFinderGetter) {
        Matrix4fc matrix = state.transformation().getMatrix();
        if (MatrixUtil.isIdentity(matrix)) {
            return q -> true;
        }
        Matrix3f normalMatrix = matrix.normal(new Matrix3f());
        Vector4f vec4 = new Vector4f();
        Vector3f vec3 = new Vector3f();
        return quad -> {
            Direction lightFace = quad.lightFace();
            Matrix4fc reverseMatrix = state.inverseFaceTransformation(lightFace);
            if (!MatrixUtil.isIdentity(reverseMatrix)) {
                SpriteFinder spriteFinder = spriteFinderGetter.spriteFinder(quad.atlas());
                TextureAtlasSprite sprite = spriteFinder.find(quad);
                for (int vertexIndex = 0; vertexIndex < 4; ++vertexIndex) {
                    float frameU = ModelStateHelper.getFrameFromU(sprite, quad.u(vertexIndex));
                    float frameV = ModelStateHelper.getFrameFromV(sprite, quad.v(vertexIndex));
                    vec3.set(frameU - 0.5f, frameV - 0.5f, 0.0f);
                    reverseMatrix.transformPosition(vec3);
                    frameU = vec3.x + 0.5f;
                    frameV = vec3.y + 0.5f;
                    quad.uv(vertexIndex, sprite.getU(frameU), sprite.getV(frameV));
                }
            }
            for (int vertexIndex = 0; vertexIndex < 4; ++vertexIndex) {
                vec4.set(quad.x(vertexIndex) - 0.5f, quad.y(vertexIndex) - 0.5f, quad.z(vertexIndex) - 0.5f, 1.0f);
                vec4.mul(matrix);
                quad.pos(vertexIndex, vec4.x + 0.5f, vec4.y + 0.5f, vec4.z + 0.5f);
                if (!quad.hasNormal(vertexIndex)) continue;
                quad.copyNormal(vertexIndex, vec3);
                vec3.mul(normalMatrix);
                vec3.normalize();
                quad.normal(vertexIndex, vec3);
            }
            Direction cullFace = quad.cullFace();
            if (cullFace != null) {
                quad.cullFace(Direction.rotate(matrix, cullFace));
            }
            return true;
        };
    }

    private static float getFrameFromU(TextureAtlasSprite sprite, float u) {
        float f = sprite.getU1() - sprite.getU0();
        return (u - sprite.getU0()) / f;
    }

    private static float getFrameFromV(TextureAtlasSprite sprite, float v) {
        float f = sprite.getV1() - sprite.getV0();
        return (v - sprite.getV0()) / f;
    }
}

