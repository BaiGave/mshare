/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.gametest.v1.screenshot;

import com.google.common.base.Preconditions;
import net.fabricmc.fabric.impl.client.gametest.screenshot.TestScreenshotComparisonAlgorithms;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector2i;
import org.jspecify.annotations.Nullable;

public interface TestScreenshotComparisonAlgorithm {
    public static TestScreenshotComparisonAlgorithm defaultAlgorithm() {
        return TestScreenshotComparisonAlgorithms.MeanSquaredDifference.DEFAULT;
    }

    public static TestScreenshotComparisonAlgorithm meanSquaredDifference(float maxMeanSquaredDifference) {
        Preconditions.checkArgument(maxMeanSquaredDifference >= 0.0f && maxMeanSquaredDifference <= 1.0f, "maxMeanSquaredError must be between 0 and 1");
        return new TestScreenshotComparisonAlgorithms.MeanSquaredDifference(maxMeanSquaredDifference);
    }

    public static TestScreenshotComparisonAlgorithm exact() {
        return TestScreenshotComparisonAlgorithms.Exact.INSTANCE;
    }

    public @Nullable Vector2i findColor(RawImage<int[]> var1, RawImage<int[]> var2);

    default public @Nullable Vector2i findGrayscale(RawImage<byte[]> haystack, RawImage<byte[]> needle) {
        RawImage<int[]> colorHaystack = TestScreenshotComparisonAlgorithms.RawImageImpl.toColor(haystack);
        RawImage<int[]> colorNeedle = TestScreenshotComparisonAlgorithms.RawImageImpl.toColor(needle);
        return this.findColor(colorHaystack, colorNeedle);
    }

    @ApiStatus.NonExtendable
    public static interface RawImage<DATA> {
        public int width();

        public int height();

        public DATA data();
    }
}

