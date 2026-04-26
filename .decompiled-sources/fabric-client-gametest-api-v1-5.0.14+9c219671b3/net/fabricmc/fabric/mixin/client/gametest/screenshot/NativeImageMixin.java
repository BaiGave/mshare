/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.gametest.screenshot;

import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.fabric.impl.client.gametest.screenshot.NativeImageHooks;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={NativeImage.class})
public abstract class NativeImageMixin
implements NativeImageHooks {
    @Shadow
    private long pixels;
    @Shadow
    @Final
    private NativeImage.Format format;

    @Shadow
    protected abstract void checkAllocated();

    @Shadow
    public abstract int getWidth();

    @Shadow
    public abstract int getHeight();

    @Shadow
    public abstract int[] getPixelsABGR();

    @Override
    public byte[] fabric_copyPixelsLuminance() {
        this.checkAllocated();
        byte[] result = new byte[this.getWidth() * this.getHeight()];
        switch (this.format) {
            case RGBA: {
                for (int i = 0; i < result.length; ++i) {
                    int red = MemoryUtil.memGetByte(this.pixels + (long)(i * 4)) & 0xFF;
                    int green = MemoryUtil.memGetByte(this.pixels + (long)(i * 4) + 1L) & 0xFF;
                    int blue = MemoryUtil.memGetByte(this.pixels + (long)(i * 4) + 2L) & 0xFF;
                    result[i] = NativeImageMixin.toGrayscale(red, green, blue);
                }
                break;
            }
            case RGB: {
                for (int i = 0; i < result.length; ++i) {
                    int red = MemoryUtil.memGetByte(this.pixels + (long)(i * 3)) & 0xFF;
                    int green = MemoryUtil.memGetByte(this.pixels + (long)(i * 3) + 1L) & 0xFF;
                    int blue = MemoryUtil.memGetByte(this.pixels + (long)(i * 3) + 2L) & 0xFF;
                    result[i] = NativeImageMixin.toGrayscale(red, green, blue);
                }
                break;
            }
            case LUMINANCE_ALPHA: {
                for (int i = 0; i < result.length; ++i) {
                    result[i] = MemoryUtil.memGetByte(this.pixels + (long)(i * 2));
                }
                break;
            }
            case LUMINANCE: {
                MemoryUtil.memByteBuffer(this.pixels, this.getWidth() * this.getHeight()).get(result);
            }
        }
        return result;
    }

    @Override
    public int[] fabric_copyPixelsRgb() {
        this.checkAllocated();
        return switch (this.format) {
            default -> throw new MatchException(null, null);
            case NativeImage.Format.RGBA -> {
                int[] result = this.getPixelsABGR();
                for (int i = 0; i < result.length; ++i) {
                    int color = result[i];
                    int blue = color >> 16 & 0xFF;
                    int green = color >> 8 & 0xFF;
                    int red = color & 0xFF;
                    result[i] = red << 16 | green << 8 | blue;
                }
                yield result;
            }
            case NativeImage.Format.RGB -> {
                int[] result = new int[this.getWidth() * this.getHeight()];
                for (int i = 0; i < result.length; ++i) {
                    int red = MemoryUtil.memGetByte(this.pixels + (long)(i * 3)) & 0xFF;
                    int green = MemoryUtil.memGetByte(this.pixels + (long)(i * 3) + 1L) & 0xFF;
                    int blue = MemoryUtil.memGetByte(this.pixels + (long)(i * 3) + 2L) & 0xFF;
                    result[i] = red << 16 | green << 8 | blue;
                }
                yield result;
            }
            case NativeImage.Format.LUMINANCE_ALPHA -> {
                int[] result = new int[this.getWidth() * this.getHeight()];
                for (int i = 0; i < result.length; ++i) {
                    int luminance = MemoryUtil.memGetByte(this.pixels + (long)(i * 2)) & 0xFF;
                    result[i] = luminance << 16 | luminance << 8 | luminance;
                }
                yield result;
            }
            case NativeImage.Format.LUMINANCE -> {
                int[] result = new int[this.getWidth() * this.getHeight()];
                for (int i = 0; i < result.length; ++i) {
                    int luminance = MemoryUtil.memGetByte(this.pixels + (long)i) & 0xFF;
                    result[i] = luminance << 16 | luminance << 8 | luminance;
                }
                yield result;
            }
        };
    }

    @Override
    public boolean fabric_isFullyOpaque() {
        if (!this.format.hasAlpha()) {
            return true;
        }
        int size = this.getWidth() * this.getHeight();
        int alphaOffset = this.format.alphaOffset() / 8;
        for (int i = 0; i < size; ++i) {
            int alpha = MemoryUtil.memGetByte(this.pixels + (long)(i * this.format.components()) + (long)alphaOffset) & 0xFF;
            if (alpha == 255) continue;
            return false;
        }
        return true;
    }

    @Unique
    private static byte toGrayscale(int red, int green, int blue) {
        return (byte)(red * 77 + green * 150 + blue * 29 >> 8);
    }
}

