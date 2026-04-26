/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.gametest.screenshot;

import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.fabricmc.fabric.impl.client.gametest.screenshot.TestScreenshotCommonOptionsImpl;

public final class TestScreenshotOptionsImpl
extends TestScreenshotCommonOptionsImpl<TestScreenshotOptions>
implements TestScreenshotOptions {
    public final String name;

    public TestScreenshotOptionsImpl(String name) {
        this.name = name;
    }
}

