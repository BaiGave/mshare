/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.gametest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.impl.client.gametest.context.ClientGameTestContextImpl;
import net.fabricmc.fabric.impl.client.gametest.threading.ThreadingImpl;
import net.fabricmc.fabric.impl.client.gametest.util.WindowHooks;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;

public class FabricClientGameTestRunner {
    private static final String ENTRYPOINT_KEY = "fabric-client-gametest";
    public static EntrypointContainer<FabricClientGameTest> currentlyRunningGameTest = null;

    public static void start() {
        ((WindowHooks)((Object)Minecraft.getInstance().getWindow())).fabric_focus();
        List<EntrypointContainer<FabricClientGameTest>> gameTests = FabricClientGameTestRunner.getTestToRun();
        ThreadingImpl.runTestThread(() -> {
            ClientGameTestContextImpl context = new ClientGameTestContextImpl();
            Iterator i$ = gameTests.iterator();
            while (i$.hasNext()) {
                EntrypointContainer gameTest;
                currentlyRunningGameTest = gameTest = (EntrypointContainer)i$.next();
                try {
                    FabricClientGameTestRunner.setupInitialGameTestState(context);
                    ((FabricClientGameTest)gameTest.getEntrypoint()).runTest(context);
                    FabricClientGameTestRunner.setupAndCheckFinalGameTestState(context);
                }
                finally {
                    currentlyRunningGameTest = null;
                }
            }
        });
    }

    private static List<EntrypointContainer<FabricClientGameTest>> getTestToRun() {
        List<EntrypointContainer<FabricClientGameTest>> gameTests = FabricLoader.getInstance().getEntrypointContainers(ENTRYPOINT_KEY, FabricClientGameTest.class);
        String filter = System.getProperty("fabric.client.gametest.modid");
        if (filter == null) {
            return gameTests;
        }
        List<String> modIds = Arrays.stream(filter.split(",")).map(String::trim).filter(modId -> !modId.isEmpty()).peek(modId -> {
            if (!FabricLoader.getInstance().isModLoaded((String)modId)) {
                throw new IllegalArgumentException("Mod ID %s specified in game test filter '%s' is not loaded".formatted(modId, filter));
            }
        }).toList();
        if (modIds.isEmpty()) {
            throw new IllegalArgumentException("No valid mod IDs specified in the client game test filter");
        }
        ArrayList<EntrypointContainer<FabricClientGameTest>> filteredGameTests = new ArrayList<EntrypointContainer<FabricClientGameTest>>();
        for (EntrypointContainer<FabricClientGameTest> gameTest : gameTests) {
            if (!modIds.contains(gameTest.getProvider().getMetadata().getId())) continue;
            filteredGameTests.add(gameTest);
        }
        if (filteredGameTests.isEmpty()) {
            throw new IllegalArgumentException("No tests found for the specified mod IDs: " + String.valueOf(modIds));
        }
        return Collections.unmodifiableList(filteredGameTests);
    }

    private static void setupInitialGameTestState(ClientGameTestContext context) {
        context.restoreDefaultGameOptions();
    }

    private static void setupAndCheckFinalGameTestState(ClientGameTestContextImpl context) {
        context.getInput().clearKeysDown();
        context.runOnClient(client -> ((WindowHooks)((Object)client.getWindow())).fabric_resetSize());
        context.getInput().setCursorPos((double)context.computeOnClient(client -> client.getWindow().getScreenWidth()).intValue() * 0.5, (double)context.computeOnClient(client -> client.getWindow().getScreenHeight()).intValue() * 0.5);
        if (ThreadingImpl.isServerRunning) {
            throw new AssertionError((Object)"Client gametest %s finished while a server is still running".formatted(currentlyRunningGameTest.getDefinition()));
        }
        context.runOnClient(client -> {
            if (client.level != null) {
                throw new AssertionError((Object)"Client gametest %s finished while still connected to a server".formatted(currentlyRunningGameTest.getDefinition()));
            }
            if (!(client.screen instanceof TitleScreen)) {
                throw new AssertionError((Object)"Client gametest %s did not finish on the title screen. Current screen %s".formatted(currentlyRunningGameTest.getDefinition(), client.screen.getClass().getName()));
            }
        });
    }
}

