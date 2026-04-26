/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.gametest;

import net.fabricmc.fabric.impl.client.gametest.ClientGameTestMixinConfigPlugin;
import org.jspecify.annotations.Nullable;

public final class TestSystemProperties {
    public static final boolean ENABLED = ClientGameTestMixinConfigPlugin.ENABLED;
    public static final @Nullable String TEST_MOD_RESOURCES_PATH = System.getProperty("fabric.client.gametest.testModResourcesPath");
    public static final boolean DISABLE_NETWORK_SYNCHRONIZER = !"false".equals(System.getProperty("fabric.client.gametest.disableNetworkSynchronizer", "true"));
    public static final boolean DISABLE_JOIN_ASYNC_STACK_TRACES = System.getProperty("fabric.client.gametest.disableJoinAsyncStackTraces") != null;
    public static final String MOD_ID_FILTER_PROPERTY = "fabric.client.gametest.modid";

    private TestSystemProperties() {
    }
}

