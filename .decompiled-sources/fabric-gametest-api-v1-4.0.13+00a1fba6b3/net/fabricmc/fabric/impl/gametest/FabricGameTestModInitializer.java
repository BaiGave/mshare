/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.gametest;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.impl.gametest.FabricGameTestRunner;
import net.fabricmc.fabric.impl.gametest.TestAnnotationLocator;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.resources.RegistryLoadTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FabricGameTestModInitializer
implements ModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(FabricGameTestModInitializer.class);
    private static TestAnnotationLocator locator = new TestAnnotationLocator(FabricLoader.getInstance());

    @Override
    public void onInitialize() {
        if (!FabricGameTestRunner.ENABLED && !FabricLoader.getInstance().isDevelopmentEnvironment()) {
            return;
        }
        for (TestAnnotationLocator.TestMethod testMethod : locator.getTestMethods()) {
            LOGGER.debug("Registering test method: {}", (Object)testMethod.identifier());
            Registry.register(BuiltInRegistries.TEST_FUNCTION, testMethod.identifier(), testMethod.testFunction());
        }
    }

    public static void registerDynamicEntries(List<RegistryLoadTask<?>> loadTasks) {
        IdentityHashMap registries = new IdentityHashMap(loadTasks.size());
        for (RegistryLoadTask<?> entry : loadTasks) {
            registries.put(entry.registry.key(), entry.registry);
        }
        Registry testInstances = (Registry)registries.get(Registries.TEST_INSTANCE);
        Registry testEnvironmentDefinitionRegistry = Objects.requireNonNull((Registry)registries.get(Registries.TEST_ENVIRONMENT));
        for (TestAnnotationLocator.TestMethod testMethod : locator.getTestMethods()) {
            GameTestInstance testInstance = testMethod.testInstance(testEnvironmentDefinitionRegistry);
            Registry.register(testInstances, testMethod.identifier(), testInstance);
        }
    }
}

