/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.gametest.v1.screenshot;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotCommonOptions;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotComparisonAlgorithm;
import net.fabricmc.fabric.impl.client.gametest.screenshot.TestScreenshotComparisonOptionsImpl;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface TestScreenshotComparisonOptions
extends TestScreenshotCommonOptions<TestScreenshotComparisonOptions> {
    public static TestScreenshotComparisonOptions of(String templateImage) {
        Preconditions.checkNotNull(templateImage, "templateImage");
        return new TestScreenshotComparisonOptionsImpl(templateImage);
    }

    public static TestScreenshotComparisonOptions of(NativeImage templateImage) {
        Preconditions.checkNotNull(templateImage, "templateImage");
        return new TestScreenshotComparisonOptionsImpl(templateImage);
    }

    public TestScreenshotComparisonOptions save();

    public TestScreenshotComparisonOptions saveWithFileName(String var1);

    public TestScreenshotComparisonOptions withAlgorithm(TestScreenshotComparisonAlgorithm var1);

    public TestScreenshotComparisonOptions withGrayscale();

    public TestScreenshotComparisonOptions withRegion(int var1, int var2, int var3, int var4);
}

