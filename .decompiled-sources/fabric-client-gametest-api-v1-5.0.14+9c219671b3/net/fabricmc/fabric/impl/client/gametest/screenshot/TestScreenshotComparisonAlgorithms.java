/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.gametest.screenshot;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.NativeImage;
import java.util.Objects;
import java.util.stream.IntStream;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotComparisonAlgorithm;
import net.fabricmc.fabric.impl.client.gametest.screenshot.NativeImageHooks;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.joml.Vector2i;
import org.jspecify.annotations.Nullable;

public class TestScreenshotComparisonAlgorithms {
    private static @Nullable Vector2i find(TestScreenshotComparisonAlgorithm.RawImage<?> haystack, TestScreenshotComparisonAlgorithm.RawImage<?> needle, PositionPredicate predicate) {
        if (needle.width() > haystack.width() || needle.height() > haystack.height()) {
            return null;
        }
        return IntStream.rangeClosed(0, haystack.height() - needle.height()).parallel().mapToObj(needleY -> {
            int maxNeedleX = haystack.width() - needle.width();
            for (int needleX = 0; needleX <= maxNeedleX; ++needleX) {
                if (!predicate.isAt(needleX, needleY)) continue;
                return new Vector2i(needleX, needleY);
            }
            return null;
        }).filter(Objects::nonNull).findAny().orElse(null);
    }

    @FunctionalInterface
    private static interface PositionPredicate {
        public boolean isAt(int var1, int var2);
    }

    public record RawImageImpl<DATA>(int width, int height, DATA data) implements TestScreenshotComparisonAlgorithm.RawImage<DATA>
    {
        public static TestScreenshotComparisonAlgorithm.RawImage<int[]> toColor(TestScreenshotComparisonAlgorithm.RawImage<byte[]> grayscaleImage) {
            byte[] grayscale = grayscaleImage.data();
            int[] color = new int[grayscale.length];
            for (int i = 0; i < grayscale.length; ++i) {
                int luminance = grayscale[i] & 0xFF;
                color[i] = luminance << 16 | luminance << 8 | luminance;
            }
            return new RawImageImpl<int[]>(grayscaleImage.width(), grayscaleImage.height(), color);
        }

        public static TestScreenshotComparisonAlgorithm.RawImage<byte[]> fromGrayscaleNativeImage(NativeImage image) {
            return new RawImageImpl<byte[]>(image.getWidth(), image.getHeight(), ((NativeImageHooks)((Object)image)).fabric_copyPixelsLuminance());
        }

        public static TestScreenshotComparisonAlgorithm.RawImage<int[]> fromColorNativeImage(NativeImage image) {
            return new RawImageImpl<int[]>(image.getWidth(), image.getHeight(), ((NativeImageHooks)((Object)image)).fabric_copyPixelsRgb());
        }
    }

    public static enum Exact implements TestScreenshotComparisonAlgorithm
    {
        INSTANCE;


        @Override
        public @Nullable Vector2i findColor(TestScreenshotComparisonAlgorithm.RawImage<int[]> haystack, TestScreenshotComparisonAlgorithm.RawImage<int[]> needle) {
            Preconditions.checkNotNull(haystack, "haystack");
            Preconditions.checkNotNull(needle, "needle");
            int[] haystackData = haystack.data();
            int[] needleData = needle.data();
            int haystackWidth = haystack.width();
            int needleWidth = needle.width();
            int needleHeight = needle.height();
            return TestScreenshotComparisonAlgorithms.find(haystack, needle, (needleX, needleY) -> {
                for (int y = 0; y < needleHeight; ++y) {
                    for (int x = 0; x < needleWidth; ++x) {
                        int haystackColor = haystackData[(needleY + y) * haystackWidth + needleX + x];
                        int needleColor = needleData[y * needleWidth + x];
                        if (haystackColor == needleColor) continue;
                        return false;
                    }
                }
                return true;
            });
        }

        @Override
        public @Nullable Vector2i findGrayscale(TestScreenshotComparisonAlgorithm.RawImage<byte[]> haystack, TestScreenshotComparisonAlgorithm.RawImage<byte[]> needle) {
            Preconditions.checkNotNull(haystack, "haystack");
            Preconditions.checkNotNull(needle, "needle");
            byte[] haystackData = haystack.data();
            byte[] needleData = needle.data();
            int haystackWidth = haystack.width();
            int needleWidth = needle.width();
            int needleHeight = needle.height();
            return TestScreenshotComparisonAlgorithms.find(haystack, needle, (needleX, needleY) -> {
                for (int y = 0; y < needleHeight; ++y) {
                    for (int x = 0; x < needleWidth; ++x) {
                        byte haystackLuminance = haystackData[(needleY + y) * haystackWidth + needleX + x];
                        byte needleLuminance = needleData[y * needleWidth + x];
                        if (haystackLuminance == needleLuminance) continue;
                        return false;
                    }
                }
                return true;
            });
        }
    }

    public record MeanSquaredDifference(float maxMeanSquaredDifference) implements TestScreenshotComparisonAlgorithm
    {
        public static final MeanSquaredDifference DEFAULT = new MeanSquaredDifference(0.005f);

        @Override
        public @Nullable Vector2i findColor(TestScreenshotComparisonAlgorithm.RawImage<int[]> haystack, TestScreenshotComparisonAlgorithm.RawImage<int[]> needle) {
            Preconditions.checkNotNull(haystack, "haystack");
            Preconditions.checkNotNull(needle, "needle");
            int[] haystackData = haystack.data();
            int[] needleData = needle.data();
            int haystackWidth = haystack.width();
            int needleWidth = needle.width();
            int needleHeight = needle.height();
            long threshold = (long)((double)this.maxMeanSquaredDifference * (double)needleWidth * (double)needleHeight * 3.0 * 255.0 * 255.0);
            return TestScreenshotComparisonAlgorithms.find(haystack, needle, (needleX, needleY) -> {
                long sumSquaredDifference = 0L;
                for (int y = 0; y < needleHeight; ++y) {
                    for (int x = 0; x < needleWidth; ++x) {
                        int haystackColor = haystackData[(needleY + y) * haystackWidth + needleX + x];
                        int haystackRed = ARGB.red(haystackColor);
                        int haystackGreen = ARGB.green(haystackColor);
                        int haystackBlue = ARGB.blue(haystackColor);
                        int needleColor = needleData[y * needleWidth + x];
                        int needleRed = ARGB.red(needleColor);
                        int needleGreen = ARGB.green(needleColor);
                        int needleBlue = ARGB.blue(needleColor);
                        if ((sumSquaredDifference += (long)(Mth.square(haystackRed - needleRed) + Mth.square(haystackGreen - needleGreen) + Mth.square(haystackBlue - needleBlue))) < threshold) continue;
                        return false;
                    }
                }
                return true;
            });
        }

        @Override
        public @Nullable Vector2i findGrayscale(TestScreenshotComparisonAlgorithm.RawImage<byte[]> haystack, TestScreenshotComparisonAlgorithm.RawImage<byte[]> needle) {
            Preconditions.checkNotNull(haystack, "haystack");
            Preconditions.checkNotNull(needle, "needle");
            byte[] haystackData = haystack.data();
            byte[] needleData = needle.data();
            int haystackWidth = haystack.width();
            int needleWidth = needle.width();
            int needleHeight = needle.height();
            long threshold = (long)((double)this.maxMeanSquaredDifference * (double)needleWidth * (double)needleHeight * 255.0 * 255.0);
            return TestScreenshotComparisonAlgorithms.find(haystack, needle, (needleX, needleY) -> {
                long sumSquaredDifference = 0L;
                for (int y = 0; y < needleHeight; ++y) {
                    for (int x = 0; x < needleWidth; ++x) {
                        int haystackLuminance = haystackData[(needleY + y) * haystackWidth + needleX + x] & 0xFF;
                        int needleLuminance = needleData[y * needleWidth + x] & 0xFF;
                        if ((sumSquaredDifference += (long)Mth.square(haystackLuminance - needleLuminance)) < threshold) continue;
                        return false;
                    }
                }
                return true;
            });
        }
    }
}

