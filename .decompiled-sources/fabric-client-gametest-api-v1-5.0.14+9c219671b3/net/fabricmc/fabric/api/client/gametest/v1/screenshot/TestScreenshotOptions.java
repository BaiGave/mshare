/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.gametest.v1.screenshot;

import com.google.common.base.Preconditions;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotCommonOptions;
import net.fabricmc.fabric.impl.client.gametest.screenshot.TestScreenshotOptionsImpl;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface TestScreenshotOptions
extends TestScreenshotCommonOptions<TestScreenshotOptions> {
    public static TestScreenshotOptions of(String name) {
        Preconditions.checkNotNull(name, "name");
        return new TestScreenshotOptionsImpl(name);
    }
}

