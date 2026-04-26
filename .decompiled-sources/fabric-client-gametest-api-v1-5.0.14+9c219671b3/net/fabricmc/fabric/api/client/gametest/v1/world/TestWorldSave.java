/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.gametest.v1.world;

import java.nio.file.Path;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface TestWorldSave {
    public Path getSaveDirectory();

    public TestSingleplayerContext open();
}

