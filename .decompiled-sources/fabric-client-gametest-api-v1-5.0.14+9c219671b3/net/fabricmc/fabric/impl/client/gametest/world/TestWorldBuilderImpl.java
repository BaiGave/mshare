/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.gametest.world;

import com.google.common.base.Preconditions;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldBuilder;
import net.fabricmc.fabric.impl.client.gametest.context.TestDedicatedServerContextImpl;
import net.fabricmc.fabric.impl.client.gametest.context.TestSingleplayerContextImpl;
import net.fabricmc.fabric.impl.client.gametest.threading.ThreadingImpl;
import net.fabricmc.fabric.impl.client.gametest.util.ClientGameTestImpl;
import net.fabricmc.fabric.impl.client.gametest.util.DedicatedServerImplUtil;
import net.fabricmc.fabric.impl.client.gametest.world.TestWorldSaveImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestWorldBuilderImpl
implements TestWorldBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger("fabric-client-gametest-api-v1");
    private final ClientGameTestContext context;
    private boolean useConsistentSettings = true;
    private Consumer<WorldCreationUiState> settingsAdjustor = creator -> {};

    public TestWorldBuilderImpl(ClientGameTestContext context) {
        this.context = context;
    }

    @Override
    public TestWorldBuilder setUseConsistentSettings(boolean useConsistentSettings) {
        this.useConsistentSettings = useConsistentSettings;
        return this;
    }

    @Override
    public TestWorldBuilder adjustSettings(Consumer<WorldCreationUiState> settingsAdjuster) {
        Preconditions.checkNotNull(settingsAdjuster, "settingsAdjuster");
        this.settingsAdjustor = settingsAdjuster;
        return this;
    }

    @Override
    public TestSingleplayerContext create() {
        ThreadingImpl.checkOnGametestThread("create");
        Preconditions.checkState(!ThreadingImpl.isServerRunning, "Cannot create a world when a server is running");
        Path saveDirectory = this.navigateCreateWorldScreen();
        ClientGameTestImpl.waitForWorldLoad(this.context);
        MinecraftServer server = this.context.computeOnClient(Minecraft::getSingleplayerServer);
        return new TestSingleplayerContextImpl(this.context, new TestWorldSaveImpl(this.context, saveDirectory), server);
    }

    @Override
    public TestDedicatedServerContext createServer(Properties serverProperties) {
        ThreadingImpl.checkOnGametestThread("createServer");
        Preconditions.checkState(!ThreadingImpl.isServerRunning, "Cannot create a server when a server is running");
        DedicatedServerImplUtil.saveLevelDataTo = Path.of(serverProperties.getProperty("level-name", "world"), new String[0]);
        try {
            FileUtils.deleteDirectory(DedicatedServerImplUtil.saveLevelDataTo.toFile());
        }
        catch (IOException e) {
            LOGGER.error("Failed to clean up old dedicated server world", e);
        }
        try {
            this.navigateCreateWorldScreen();
        }
        finally {
            DedicatedServerImplUtil.saveLevelDataTo = null;
        }
        DedicatedServer server = DedicatedServerImplUtil.start(this.context, serverProperties);
        return new TestDedicatedServerContextImpl(this.context, server);
    }

    private Path navigateCreateWorldScreen() {
        Path saveDirectory = this.context.computeOnClient(client -> {
            Screen oldScreen = client.screen;
            CreateWorldScreen.openFresh(client, () -> client.setScreen(oldScreen));
            Screen patt0$temp = client.screen;
            if (!(patt0$temp instanceof CreateWorldScreen)) {
                throw new AssertionError((Object)"CreateWorldScreen.show did not set the current screen");
            }
            CreateWorldScreen createWorldScreen = (CreateWorldScreen)patt0$temp;
            WorldCreationUiState creator = createWorldScreen.getUiState();
            if (this.useConsistentSettings) {
                TestWorldBuilderImpl.setConsistentSettings(creator);
            }
            this.settingsAdjustor.accept(creator);
            return client.getLevelSource().getBaseDir().resolve(creator.getTargetFolder());
        });
        this.context.clickScreenButton("selectWorld.create");
        return saveDirectory;
    }

    private static void setConsistentSettings(WorldCreationUiState creator) {
        Holder.Reference<WorldPreset> flatPreset = creator.getSettings().worldgenLoadContext().lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT);
        creator.setWorldType(new WorldCreationUiState.WorldTypeEntry(flatPreset));
        creator.setSeed("1");
        creator.setGenerateStructures(false);
        creator.getGameRules().set(GameRules.ADVANCE_TIME, false, null);
        creator.getGameRules().set(GameRules.ADVANCE_WEATHER, false, null);
        creator.getGameRules().set(GameRules.SPAWN_MOBS, false, null);
    }
}

