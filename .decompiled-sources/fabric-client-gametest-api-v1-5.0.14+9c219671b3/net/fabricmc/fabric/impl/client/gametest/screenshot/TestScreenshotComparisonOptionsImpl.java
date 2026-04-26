/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.gametest.screenshot;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Either;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Optional;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotComparisonAlgorithm;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotComparisonOptions;
import net.fabricmc.fabric.impl.client.gametest.FabricClientGameTestRunner;
import net.fabricmc.fabric.impl.client.gametest.screenshot.NativeImageHooks;
import net.fabricmc.fabric.impl.client.gametest.screenshot.TestScreenshotCommonOptionsImpl;
import net.fabricmc.fabric.impl.client.gametest.screenshot.TestScreenshotComparisonAlgorithms;
import net.minecraft.client.renderer.Rect2i;
import org.jspecify.annotations.Nullable;

public final class TestScreenshotComparisonOptionsImpl
extends TestScreenshotCommonOptionsImpl<TestScreenshotComparisonOptions>
implements TestScreenshotComparisonOptions {
    private final Either<String, NativeImage> templateImage;
    public @Nullable String savedFileName;
    public TestScreenshotComparisonAlgorithm algorithm = TestScreenshotComparisonAlgorithm.defaultAlgorithm();
    public boolean grayscale = false;
    public @Nullable Rect2i region;

    public TestScreenshotComparisonOptionsImpl(String templateImage) {
        this.templateImage = Either.left(templateImage);
    }

    public TestScreenshotComparisonOptionsImpl(NativeImage templateImage) {
        this.templateImage = Either.right(templateImage);
    }

    @Override
    public TestScreenshotComparisonOptions save() {
        return this.saveWithFileName(this.getTemplateImagePathOrThrow());
    }

    @Override
    public TestScreenshotComparisonOptions saveWithFileName(String fileName) {
        Preconditions.checkNotNull(fileName, "fileName");
        this.savedFileName = fileName;
        return this;
    }

    @Override
    public TestScreenshotComparisonOptions withAlgorithm(TestScreenshotComparisonAlgorithm algorithm) {
        Preconditions.checkNotNull(algorithm, "algorithm");
        this.algorithm = algorithm;
        return this;
    }

    @Override
    public TestScreenshotComparisonOptions withGrayscale() {
        this.grayscale = true;
        return this;
    }

    @Override
    public TestScreenshotComparisonOptions withRegion(int x, int y, int width, int height) {
        Preconditions.checkArgument(x >= 0, "x cannot be negative");
        Preconditions.checkArgument(y >= 0, "y cannot be negative");
        Preconditions.checkArgument(width > 0, "width must be positive");
        Preconditions.checkArgument(height > 0, "height must be positive");
        this.region = new Rect2i(x, y, width, height);
        return this;
    }

    public Optional<String> getTemplateImagePath() {
        return this.templateImage.left();
    }

    public String getTemplateImagePathOrThrow() {
        return this.getTemplateImagePath().orElseThrow();
    }

    public @Nullable TestScreenshotComparisonAlgorithm.RawImage<byte[]> getGrayscaleTemplateImage() {
        return this.templateImage.map(fileName -> {
            try (NativeImage image = TestScreenshotComparisonOptionsImpl.loadNativeImage(fileName);){
                if (image == null) {
                    TestScreenshotComparisonAlgorithm.RawImage rawImage = null;
                    return rawImage;
                }
                TestScreenshotComparisonAlgorithms.RawImageImpl<byte[]> rawImageImpl = new TestScreenshotComparisonAlgorithms.RawImageImpl<byte[]>(image.getWidth(), image.getHeight(), ((NativeImageHooks)((Object)image)).fabric_copyPixelsLuminance());
                return rawImageImpl;
            }
        }, image -> {
            TestScreenshotComparisonOptionsImpl.assertNoTransparency(image);
            return TestScreenshotComparisonAlgorithms.RawImageImpl.fromGrayscaleNativeImage(image);
        });
    }

    public @Nullable TestScreenshotComparisonAlgorithm.RawImage<int[]> getColorTemplateImage() {
        return this.templateImage.map(fileName -> {
            try (NativeImage image = TestScreenshotComparisonOptionsImpl.loadNativeImage(fileName);){
                if (image == null) {
                    TestScreenshotComparisonAlgorithm.RawImage rawImage = null;
                    return rawImage;
                }
                TestScreenshotComparisonAlgorithms.RawImageImpl<int[]> rawImageImpl = new TestScreenshotComparisonAlgorithms.RawImageImpl<int[]>(image.getWidth(), image.getHeight(), ((NativeImageHooks)((Object)image)).fabric_copyPixelsRgb());
                return rawImageImpl;
            }
        }, image -> {
            TestScreenshotComparisonOptionsImpl.assertNoTransparency(image);
            return TestScreenshotComparisonAlgorithms.RawImageImpl.fromColorNativeImage(image);
        });
    }

    private static @Nullable NativeImage loadNativeImage(String templateImagePath) {
        NativeImage nativeImage;
        block9: {
            Path filePath = FabricClientGameTestRunner.currentlyRunningGameTest.getProvider().findPath("templates/" + templateImagePath + ".png").orElse(null);
            if (filePath == null) {
                return null;
            }
            InputStream stream = Files.newInputStream(filePath, new OpenOption[0]);
            try {
                NativeImage image = NativeImage.read(stream);
                TestScreenshotComparisonOptionsImpl.assertNoTransparency(image);
                nativeImage = image;
                if (stream == null) break block9;
            }
            catch (Throwable throwable) {
                try {
                    if (stream != null) {
                        try {
                            stream.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (IOException e) {
                    throw new UncheckedIOException("Failed to load template image", e);
                }
            }
            stream.close();
        }
        return nativeImage;
    }

    private static void assertNoTransparency(NativeImage image) {
        if (!((NativeImageHooks)((Object)image)).fabric_isFullyOpaque()) {
            throw new AssertionError((Object)"Template image is partially transparent which is not supported");
        }
    }
}

