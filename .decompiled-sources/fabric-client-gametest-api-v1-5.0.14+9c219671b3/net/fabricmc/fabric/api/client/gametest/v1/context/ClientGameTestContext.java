/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.gametest.v1.context;

import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.gametest.v1.TestInput;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotComparisonOptions;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableFunction;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector2i;
import org.jspecify.annotations.Nullable;

@ApiStatus.NonExtendable
public interface ClientGameTestContext {
    public static final int NO_TIMEOUT = -1;
    public static final int DEFAULT_TIMEOUT = 200;

    public void waitTick();

    public void waitTicks(int var1);

    public int waitFor(Predicate<Minecraft> var1);

    public int waitFor(Predicate<Minecraft> var1, int var2);

    public int waitForScreen(@Nullable Class<? extends Screen> var1);

    public void setScreen(Supplier<@Nullable Screen> var1);

    public void clickScreenButton(String var1);

    public boolean tryClickScreenButton(String var1);

    default public Path takeScreenshot(String name) {
        return this.takeScreenshot(TestScreenshotOptions.of(name));
    }

    public Path takeScreenshot(TestScreenshotOptions var1);

    default public void assertScreenshotEquals(String templateImage) {
        this.assertScreenshotEquals(TestScreenshotComparisonOptions.of(templateImage));
    }

    public void assertScreenshotEquals(TestScreenshotComparisonOptions var1);

    default public Vector2i assertScreenshotContains(String templateImage) {
        return this.assertScreenshotContains(TestScreenshotComparisonOptions.of(templateImage));
    }

    public Vector2i assertScreenshotContains(TestScreenshotComparisonOptions var1);

    public TestInput getInput();

    public TestWorldBuilder worldBuilder();

    public void restoreDefaultGameOptions();

    public <E extends Throwable> void runOnClient(FailableConsumer<Minecraft, E> var1) throws E;

    public <T, E extends Throwable> T computeOnClient(FailableFunction<Minecraft, T, E> var1) throws E;
}

