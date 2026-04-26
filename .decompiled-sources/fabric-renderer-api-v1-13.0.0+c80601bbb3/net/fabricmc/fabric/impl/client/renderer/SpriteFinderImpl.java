/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.renderer;

import java.util.Map;
import java.util.Objects;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadView;
import net.fabricmc.fabric.api.client.renderer.v1.sprite.SpriteFinder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpriteFinderImpl
implements SpriteFinder {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpriteFinderImpl.class);
    private final Node root = new Node(this, 0.5f, 0.5f, 0.25f);
    private final TextureAtlasSprite missingSprite;
    private int badSpriteCount = 0;

    public SpriteFinderImpl(Map<Identifier, TextureAtlasSprite> sprites, TextureAtlasSprite missingSprite) {
        this.missingSprite = missingSprite;
        sprites.values().forEach(this.root::add);
    }

    @Override
    public TextureAtlasSprite find(QuadView quad) {
        float u = 0.0f;
        float v = 0.0f;
        for (int i = 0; i < 4; ++i) {
            u += quad.u(i);
            v += quad.v(i);
        }
        return this.find(u * 0.25f, v * 0.25f);
    }

    @Override
    public TextureAtlasSprite find(float u, float v) {
        return this.root.find(u, v);
    }

    private class Node {
        final float midU;
        final float midV;
        final float cellRadius;
        @Nullable Object lowLow;
        @Nullable Object lowHigh;
        @Nullable Object highLow;
        @Nullable Object highHigh;
        static final float EPS = 1.0E-5f;
        final /* synthetic */ SpriteFinderImpl this$0;

        Node(SpriteFinderImpl spriteFinderImpl, float midU, float midV, float radius) {
            SpriteFinderImpl spriteFinderImpl2 = spriteFinderImpl;
            Objects.requireNonNull(spriteFinderImpl2);
            this.this$0 = spriteFinderImpl2;
            this.lowLow = null;
            this.lowHigh = null;
            this.highLow = null;
            this.highHigh = null;
            this.midU = midU;
            this.midV = midV;
            this.cellRadius = radius;
        }

        void add(TextureAtlasSprite sprite) {
            boolean highV;
            if (sprite.getU0() < -1.0E-5f || sprite.getU1() > 1.00001f || sprite.getV0() < -1.0E-5f || sprite.getV1() > 1.00001f) {
                if (this.this$0.badSpriteCount++ < 5) {
                    String errorMessage = "SpriteFinderImpl: Skipping sprite {} with broken bounds [{}, {}]x[{}, {}]. Sprite bounds should be between 0 and 1.";
                    LOGGER.error(errorMessage, sprite.contents().name(), Float.valueOf(sprite.getU0()), Float.valueOf(sprite.getU1()), Float.valueOf(sprite.getV0()), Float.valueOf(sprite.getV1()));
                }
                return;
            }
            boolean lowU = sprite.getU0() < this.midU - 1.0E-5f;
            boolean highU = sprite.getU1() > this.midU + 1.0E-5f;
            boolean lowV = sprite.getV0() < this.midV - 1.0E-5f;
            boolean bl = highV = sprite.getV1() > this.midV + 1.0E-5f;
            if (lowU && lowV) {
                this.lowLow = this.addInner(sprite, this.lowLow, -1, -1);
            }
            if (lowU && highV) {
                this.lowHigh = this.addInner(sprite, this.lowHigh, -1, 1);
            }
            if (highU && lowV) {
                this.highLow = this.addInner(sprite, this.highLow, 1, -1);
            }
            if (highU && highV) {
                this.highHigh = this.addInner(sprite, this.highHigh, 1, 1);
            }
        }

        private Object addInner(TextureAtlasSprite sprite, @Nullable Object quadrant, int uStep, int vStep) {
            if (quadrant == null) {
                return sprite;
            }
            if (quadrant instanceof Node) {
                Node node = (Node)quadrant;
                node.add(sprite);
                return quadrant;
            }
            Node n = new Node(this.this$0, this.midU + this.cellRadius * (float)uStep, this.midV + this.cellRadius * (float)vStep, this.cellRadius * 0.5f);
            if (quadrant instanceof TextureAtlasSprite) {
                TextureAtlasSprite prevSprite = (TextureAtlasSprite)quadrant;
                n.add(prevSprite);
            }
            n.add(sprite);
            return n;
        }

        private TextureAtlasSprite find(float u, float v) {
            if (u < this.midU) {
                return v < this.midV ? this.findInner(this.lowLow, u, v) : this.findInner(this.lowHigh, u, v);
            }
            return v < this.midV ? this.findInner(this.highLow, u, v) : this.findInner(this.highHigh, u, v);
        }

        private TextureAtlasSprite findInner(@Nullable Object quadrant, float u, float v) {
            if (quadrant instanceof Node) {
                Node node = (Node)quadrant;
                return node.find(u, v);
            }
            if (quadrant instanceof TextureAtlasSprite) {
                TextureAtlasSprite sprite = (TextureAtlasSprite)quadrant;
                return sprite;
            }
            return this.this$0.missingSprite;
        }
    }
}

