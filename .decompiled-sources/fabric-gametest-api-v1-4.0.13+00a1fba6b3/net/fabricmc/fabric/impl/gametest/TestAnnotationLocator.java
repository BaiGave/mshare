/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.gametest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class TestAnnotationLocator {
    private static final String ENTRYPOINT_KEY = "fabric-gametest";
    private static final Logger LOGGER = LoggerFactory.getLogger(TestAnnotationLocator.class);
    private final FabricLoader fabricLoader;
    private List<TestMethod> testMethods = null;

    TestAnnotationLocator(FabricLoader fabricLoader) {
        this.fabricLoader = fabricLoader;
    }

    public List<TestMethod> getTestMethods() {
        if (this.testMethods != null) {
            return this.testMethods;
        }
        List<EntrypointContainer<Object>> entrypointContainers = this.fabricLoader.getEntrypointContainers(ENTRYPOINT_KEY, Object.class);
        this.testMethods = entrypointContainers.stream().flatMap(entrypoint -> this.findMagicMethods((EntrypointContainer<Object>)entrypoint).stream()).toList();
        return this.testMethods;
    }

    private List<TestMethod> findMagicMethods(EntrypointContainer<Object> entrypoint) {
        Class<?> testClass = entrypoint.getEntrypoint().getClass();
        ArrayList<TestMethod> methods = new ArrayList<TestMethod>();
        this.findMagicMethods(entrypoint, testClass, methods);
        if (methods.isEmpty()) {
            LOGGER.warn("No methods with the GameTest annotation were found in {}", (Object)testClass.getName());
        }
        return methods;
    }

    private void findMagicMethods(EntrypointContainer<Object> entrypoint, Class<?> testClass, List<TestMethod> methods) {
        for (Method method : testClass.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(GameTest.class)) continue;
            if (!CustomTestMethodInvoker.class.isAssignableFrom(testClass)) {
                this.validateMethod(method);
            }
            methods.add(new TestMethod(method, method.getAnnotation(GameTest.class), entrypoint));
        }
        if (testClass.getSuperclass() != null) {
            this.findMagicMethods(entrypoint, testClass.getSuperclass(), methods);
        }
    }

    private void validateMethod(Method method) {
        ArrayList<String> issues = new ArrayList<String>();
        if (method.getParameterCount() != 1 || method.getParameterTypes()[0] != GameTestHelper.class) {
            issues.add("must have a single parameter of type TestContext");
        }
        if (!Modifier.isPublic(method.getModifiers())) {
            issues.add("must be public");
        }
        if (Modifier.isStatic(method.getModifiers())) {
            issues.add("must not be static");
        }
        if (method.getReturnType() != Void.TYPE) {
            issues.add("must return void");
        }
        if (issues.isEmpty()) {
            return;
        }
        String methodName = method.getDeclaringClass().getName() + "#" + method.getName();
        throw new UnsupportedOperationException("Test method (%s) has the following issues: %s".formatted(methodName, String.join((CharSequence)", ", issues)));
    }

    public record TestMethod(Method method, GameTest gameTest, EntrypointContainer<Object> entrypoint) {
        Identifier identifier() {
            String name = TestMethod.camelToSnake(this.entrypoint.getEntrypoint().getClass().getSimpleName() + "_" + this.method.getName());
            return Identifier.fromNamespaceAndPath(this.entrypoint.getProvider().getMetadata().getId(), name);
        }

        Consumer<GameTestHelper> testFunction() {
            return context -> {
                Object instance = this.entrypoint.getEntrypoint();
                try {
                    if (instance instanceof CustomTestMethodInvoker) {
                        CustomTestMethodInvoker customTestMethodInvoker = (CustomTestMethodInvoker)instance;
                        customTestMethodInvoker.invokeTestMethod((GameTestHelper)context, this.method);
                        return;
                    }
                    this.method.invoke(instance, context);
                }
                catch (InvocationTargetException e) {
                    Throwable patt0$temp = e.getTargetException();
                    if (patt0$temp instanceof RuntimeException) {
                        RuntimeException runtimeException = (RuntimeException)patt0$temp;
                        throw runtimeException;
                    }
                    throw new RuntimeException("Failed to invoke test method", e);
                }
                catch (ReflectiveOperationException e) {
                    throw new RuntimeException("Failed to invoke test method", e);
                }
            };
        }

        TestData<Holder<TestEnvironmentDefinition<?>>> testData(Registry<TestEnvironmentDefinition<?>> testEnvironmentDefinitionRegistry) {
            Holder.Reference<TestEnvironmentDefinition<?>> testEnvironment = testEnvironmentDefinitionRegistry.getOrThrow(ResourceKey.create(Registries.TEST_ENVIRONMENT, Identifier.parse(this.gameTest.environment())));
            return new TestData(testEnvironment, Identifier.parse(this.gameTest.structure()), this.gameTest.maxTicks(), this.gameTest.setupTicks(), this.gameTest.required(), this.gameTest.rotation(), this.gameTest.manualOnly(), this.gameTest.maxAttempts(), this.gameTest.requiredSuccesses(), this.gameTest.skyAccess(), this.gameTest.padding());
        }

        GameTestInstance testInstance(Registry<TestEnvironmentDefinition<?>> testEnvironmentDefinitionRegistry) {
            return new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, this.identifier()), this.testData(testEnvironmentDefinitionRegistry));
        }

        private static String camelToSnake(String input) {
            return input.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
        }
    }
}

