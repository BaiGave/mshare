/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.gametest.v1.screenshot;

import java.nio.file.Path;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface TestScreenshotCommonOptions<SELF extends TestScreenshotCommonOptions<SELF>> {
    public SELF disableCounterPrefix();

    public SELF withDeltaTicks(float var1);

    public SELF withSize(int var1, int var2);

    public SELF withDestinationDir(Path var1);
}

