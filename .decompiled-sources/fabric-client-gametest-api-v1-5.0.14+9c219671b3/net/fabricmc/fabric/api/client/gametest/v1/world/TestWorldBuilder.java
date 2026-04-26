/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.gametest.v1.world;

import java.util.Properties;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface TestWorldBuilder {
    public TestWorldBuilder setUseConsistentSettings(boolean var1);

    public TestWorldBuilder adjustSettings(Consumer<WorldCreationUiState> var1);

    public TestSingleplayerContext create();

    default public TestDedicatedServerContext createServer() {
        return this.createServer(new Properties());
    }

    public TestDedicatedServerContext createServer(Properties var1);
}

