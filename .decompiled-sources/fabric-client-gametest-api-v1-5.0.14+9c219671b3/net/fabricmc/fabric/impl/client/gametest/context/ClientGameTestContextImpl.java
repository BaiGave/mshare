/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.gametest.context;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotComparisonAlgorithm;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotComparisonOptions;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldBuilder;
import net.fabricmc.fabric.impl.client.gametest.TestInputImpl;
import net.fabricmc.fabric.impl.client.gametest.TestSystemProperties;
import net.fabricmc.fabric.impl.client.gametest.screenshot.TestScreenshotCommonOptionsImpl;
import net.fabricmc.fabric.impl.client.gametest.screenshot.TestScreenshotComparisonAlgorithms;
import net.fabricmc.fabric.impl.client.gametest.screenshot.TestScreenshotComparisonOptionsImpl;
import net.fabricmc.fabric.impl.client.gametest.screenshot.TestScreenshotOptionsImpl;
import net.fabricmc.fabric.impl.client.gametest.threading.ThreadingImpl;
import net.fabricmc.fabric.impl.client.gametest.world.TestWorldBuilderImpl;
import net.fabricmc.fabric.mixin.client.gametest.gui.CycleButtonAccessor;
import net.fabricmc.fabric.mixin.client.gametest.gui.ScreenAccessor;
import net.fabricmc.fabric.mixin.client.gametest.lifecycle.OptionsAccessor;
import net.fabricmc.fabric.mixin.client.gametest.screenshot.DeltaTrackerDefaultValueAccessor;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Optionull;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;
import org.joml.Vector2i;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ClientGameTestContextImpl
implements ClientGameTestContext {
    private static final Logger LOGGER = LoggerFactory.getLogger("fabric-client-gametest-api-v1");
    private final TestInputImpl input = new TestInputImpl(this);
    private static int screenshotCounter = 0;
    private static final Map<String, Object> DEFAULT_GAME_OPTIONS = new HashMap<String, Object>();

    public static void initGameOptions(Options options) {
        options.tutorialStep = TutorialSteps.NONE;
        options.cloudStatus().set(CloudStatus.OFF);
        options.onboardAccessibility = false;
        options.renderDistance().set(5);
        options.getSoundSourceOptionInstance(SoundSource.MUSIC).set(0.0);
        options.maxAnisotropyBit().set(0);
        options.chunkSectionFadeInTime().set(0.0);
        ((OptionsAccessor)((Object)options)).invokeProcessOptions(new Options.FieldAccess(){

            @Override
            public int process(String key, int current) {
                DEFAULT_GAME_OPTIONS.put(key, current);
                return current;
            }

            @Override
            public boolean process(String key, boolean current) {
                DEFAULT_GAME_OPTIONS.put(key, current);
                return current;
            }

            @Override
            public String process(String key, String current) {
                DEFAULT_GAME_OPTIONS.put(key, current);
                return current;
            }

            @Override
            public float process(String key, float current) {
                DEFAULT_GAME_OPTIONS.put(key, Float.valueOf(current));
                return current;
            }

            @Override
            public <T> T process(String key, T current, Function<String, T> decoder, Function<T, String> encoder) {
                DEFAULT_GAME_OPTIONS.put(key, current);
                return current;
            }

            @Override
            public <T> void process(String key, OptionInstance<T> option) {
                DEFAULT_GAME_OPTIONS.put(key, option.get());
            }
        });
    }

    @Override
    public void waitTick() {
        ThreadingImpl.checkOnGametestThread("waitTick");
        ThreadingImpl.runTick();
    }

    @Override
    public void waitTicks(int ticks) {
        ThreadingImpl.checkOnGametestThread("waitTicks");
        Preconditions.checkArgument(ticks >= 0, "ticks cannot be negative");
        for (int i = 0; i < ticks; ++i) {
            ThreadingImpl.runTick();
        }
    }

    @Override
    public int waitFor(Predicate<Minecraft> predicate) {
        ThreadingImpl.checkOnGametestThread("waitFor");
        Preconditions.checkNotNull(predicate, "predicate");
        return this.waitFor(predicate, 200);
    }

    @Override
    public int waitFor(Predicate<Minecraft> predicate, int timeout) {
        ThreadingImpl.checkOnGametestThread("waitFor");
        Preconditions.checkNotNull(predicate, "predicate");
        if (timeout == -1) {
            int ticksWaited = 0;
            while (!this.computeOnClient(predicate::test).booleanValue()) {
                ++ticksWaited;
                ThreadingImpl.runTick();
            }
            return ticksWaited;
        }
        Preconditions.checkArgument(timeout > 0, "timeout must be positive");
        for (int i = 0; i < timeout; ++i) {
            if (this.computeOnClient(predicate::test).booleanValue()) {
                return i;
            }
            ThreadingImpl.runTick();
        }
        if (!this.computeOnClient(predicate::test).booleanValue()) {
            throw new AssertionError((Object)"Timed out waiting for predicate");
        }
        return timeout;
    }

    @Override
    public int waitForScreen(@Nullable Class<? extends Screen> screenClass) {
        ThreadingImpl.checkOnGametestThread("waitForScreen");
        if (screenClass == null) {
            return this.waitFor(client -> client.screen == null);
        }
        return this.waitFor(client -> screenClass.isInstance(client.screen));
    }

    @Override
    public void setScreen(Supplier<@Nullable Screen> screen) {
        ThreadingImpl.checkOnGametestThread("setScreen");
        this.runOnClient(client -> client.setScreen((Screen)screen.get()));
    }

    @Override
    public void clickScreenButton(String translationKey) {
        ThreadingImpl.checkOnGametestThread("clickScreenButton");
        Preconditions.checkNotNull(translationKey, "translationKey");
        this.runOnClient(client -> {
            if (!ClientGameTestContextImpl.tryClickScreenButtonImpl(client.screen, translationKey)) {
                throw new AssertionError((Object)"Could not find button '%s' in screen '%s'".formatted(translationKey, Optionull.map(client.screen, screen -> screen.getClass().getName())));
            }
        });
    }

    @Override
    public boolean tryClickScreenButton(String translationKey) {
        ThreadingImpl.checkOnGametestThread("tryClickScreenButton");
        Preconditions.checkNotNull(translationKey, "translationKey");
        return this.computeOnClient(client -> ClientGameTestContextImpl.tryClickScreenButtonImpl(client.screen, translationKey));
    }

    private static boolean tryClickScreenButtonImpl(@Nullable Screen screen, String translationKey) {
        if (screen == null) {
            return false;
        }
        String buttonText = Component.translatable(translationKey).getString();
        ScreenAccessor screenAccessor = (ScreenAccessor)((Object)screen);
        for (Renderable renderable : screenAccessor.getRenderables()) {
            AbstractButton button;
            if (renderable instanceof AbstractButton && ClientGameTestContextImpl.pressMatchingButton(button = (AbstractButton)renderable, buttonText)) {
                return true;
            }
            if (!(renderable instanceof LayoutElement)) continue;
            LayoutElement layoutElement = (LayoutElement)((Object)renderable);
            MutableBoolean found = new MutableBoolean(false);
            layoutElement.visitWidgets(widget -> {
                if (!found.booleanValue()) {
                    found.setValue(ClientGameTestContextImpl.pressMatchingButton(widget, buttonText));
                }
            });
            if (!found.booleanValue()) continue;
            return true;
        }
        return false;
    }

    private static boolean pressMatchingButton(AbstractWidget widget, String text) {
        CycleButtonAccessor accessor;
        AbstractButton button;
        MouseButtonInfo clickEvent = new MouseButtonInfo(-1, 0);
        if (widget instanceof Button && text.equals((button = (Button)widget).getMessage().getString())) {
            ((Button)button).onPress(clickEvent);
            return true;
        }
        if (widget instanceof CycleButton && text.equals((accessor = (CycleButtonAccessor)((Object)(button = (CycleButton)widget))).getName().getString())) {
            ((CycleButton)button).onPress(clickEvent);
            return true;
        }
        return false;
    }

    @Override
    public Path takeScreenshot(TestScreenshotOptions options) {
        ThreadingImpl.checkOnGametestThread("takeScreenshot");
        Preconditions.checkNotNull(options, "options");
        TestScreenshotOptionsImpl optionsImpl = (TestScreenshotOptionsImpl)options;
        return this.doTakeScreenshot(optionsImpl, screenshot -> ClientGameTestContextImpl.saveScreenshot(screenshot, optionsImpl.name, optionsImpl));
    }

    @Override
    public void assertScreenshotEquals(TestScreenshotComparisonOptions options) {
        ThreadingImpl.checkOnGametestThread("assertScreenshotEquals");
        Preconditions.checkNotNull(options, "options");
        this.doAssertScreenshotContains(options, (haystackImage, needleImage) -> haystackImage.width() == needleImage.width() && haystackImage.height() == needleImage.height());
    }

    @Override
    public Vector2i assertScreenshotContains(TestScreenshotComparisonOptions options) {
        ThreadingImpl.checkOnGametestThread("assertScreenshotContains");
        Preconditions.checkNotNull(options, "options");
        return this.doAssertScreenshotContains(options, (haystackImage, needleImage) -> true);
    }

    private Vector2i doAssertScreenshotContains(TestScreenshotComparisonOptions options, BiPredicate<TestScreenshotComparisonAlgorithm.RawImage<?>, TestScreenshotComparisonAlgorithm.RawImage<?>> preCheck) {
        TestScreenshotComparisonOptionsImpl optionsImpl = (TestScreenshotComparisonOptionsImpl)options;
        return this.doTakeScreenshot(optionsImpl, screenshot -> {
            Rect2i region = optionsImpl.region == null ? new Rect2i(0, 0, screenshot.getWidth(), screenshot.getHeight()) : optionsImpl.region;
            Preconditions.checkState(region.getX() + region.getWidth() <= screenshot.getWidth() && region.getY() + region.getHeight() <= screenshot.getHeight(), "Screenshot comparison region extends outside the screenshot");
            try (NativeImage subScreenshot = new NativeImage(region.getWidth(), region.getHeight(), false);){
                Object rec$;
                Vector2i result;
                screenshot.resizeSubRectTo(region.getX(), region.getY(), region.getWidth(), region.getHeight(), subScreenshot);
                if (optionsImpl.savedFileName != null) {
                    ClientGameTestContextImpl.saveScreenshot(subScreenshot, optionsImpl.savedFileName, optionsImpl);
                }
                if (optionsImpl.grayscale) {
                    templateImage = optionsImpl.getGrayscaleTemplateImage();
                    if (templateImage == null) {
                        ClientGameTestContextImpl.onTemplateImageDoesntExist(subScreenshot, optionsImpl);
                        Vector2i vector2i = new Vector2i(region.getX(), region.getY());
                        return vector2i;
                    }
                    TestScreenshotComparisonAlgorithm.RawImage<byte[]> haystackImage = TestScreenshotComparisonAlgorithms.RawImageImpl.fromGrayscaleNativeImage(subScreenshot);
                    result = preCheck.test(haystackImage, templateImage) ? optionsImpl.algorithm.findGrayscale(haystackImage, templateImage) : null;
                } else {
                    templateImage = optionsImpl.getColorTemplateImage();
                    if (templateImage == null) {
                        ClientGameTestContextImpl.onTemplateImageDoesntExist(subScreenshot, optionsImpl);
                        Vector2i haystackImage = new Vector2i(region.getX(), region.getY());
                        return haystackImage;
                    }
                    TestScreenshotComparisonAlgorithm.RawImage<int[]> haystackImage = TestScreenshotComparisonAlgorithms.RawImageImpl.fromColorNativeImage(subScreenshot);
                    result = preCheck.test(haystackImage, templateImage) ? optionsImpl.algorithm.findColor(haystackImage, templateImage) : null;
                }
                if (result == null) {
                    rec$ = " '%s'";
                    throw new AssertionError((Object)("Screenshot does not contain template" + optionsImpl.getTemplateImagePath().map(arg_0 -> ClientGameTestContextImpl.lambda$doAssertScreenshotContains$1(" '%s'", arg_0)).orElse("")));
                }
                rec$ = result.add(region.getX(), region.getY());
                return rec$;
            }
        });
    }

    private <T> T doTakeScreenshot(TestScreenshotCommonOptionsImpl<?> options, Function<NativeImage, T> screenshotConsumer) {
        ThreadingImpl.checkOnGametestThread("doTakeScreenshot");
        Vector2i prevSize = this.computeOnClient(client -> {
            int prevWidth = client.getWindow().getWidth();
            int prevHeight = client.getWindow().getHeight();
            if (options.size != null) {
                client.getWindow().setWidth(options.size.x);
                client.getWindow().setHeight(options.size.y);
                client.getMainRenderTarget().resize(options.size.x, options.size.y);
            }
            return new Vector2i(prevWidth, prevHeight);
        });
        try {
            CompletableFuture future = this.computeOnClient(client -> {
                DeltaTracker.DefaultValue deltaTracker = DeltaTrackerDefaultValueAccessor.create(options.deltaTicks);
                client.gameRenderer.extract(deltaTracker, true);
                client.gameRenderer.render(deltaTracker, true);
                CompletableFuture resultFuture = new CompletableFuture();
                Screenshot.takeScreenshot(client.getMainRenderTarget(), screenshot -> {
                    try {
                        resultFuture.complete(screenshotConsumer.apply((NativeImage)screenshot));
                    }
                    catch (Throwable e) {
                        resultFuture.completeExceptionally(e);
                    }
                });
                return resultFuture;
            });
            while (!future.isDone()) {
                this.waitTick();
            }
            Object t = future.get();
            return t;
        }
        catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        finally {
            if (options.size != null) {
                this.computeOnClient(client -> {
                    client.getWindow().setWidth(prevSize.x);
                    client.getWindow().setHeight(prevSize.y);
                    client.getMainRenderTarget().resize(prevSize.x, prevSize.y);
                    return null;
                });
            }
        }
    }

    private static Path saveScreenshot(NativeImage screenshot, String fileName, TestScreenshotCommonOptionsImpl<?> options) {
        Path destinationDir = Objects.requireNonNullElseGet(options.destinationDir, () -> FabricLoader.getInstance().getGameDir().resolve("screenshots"));
        try {
            Files.createDirectories(destinationDir, new FileAttribute[0]);
        }
        catch (IOException e) {
            throw new AssertionError("Failed to create screenshots directory", e);
        }
        String counterPrefix = options.counterPrefix ? String.format(Locale.ROOT, "%04d_", screenshotCounter++) : "";
        Path screenshotFile = destinationDir.resolve(counterPrefix + fileName + ".png");
        try {
            screenshot.writeToFile(screenshotFile);
        }
        catch (IOException e) {
            throw new AssertionError("Failed to write screenshot file", e);
        }
        return screenshotFile;
    }

    private static void onTemplateImageDoesntExist(NativeImage subScreenshot, TestScreenshotComparisonOptionsImpl options) {
        Path savePath;
        if (TestSystemProperties.TEST_MOD_RESOURCES_PATH != null) {
            savePath = Path.of(TestSystemProperties.TEST_MOD_RESOURCES_PATH, new String[0]).resolve("templates").resolve(options.getTemplateImagePathOrThrow() + ".png");
            try {
                Files.createDirectories(savePath.getParent(), new FileAttribute[0]);
                subScreenshot.writeToFile(savePath);
            }
            catch (IOException e) {
                throw new AssertionError("Failed to write screenshot file", e);
            }
        } else {
            LOGGER.error("The template image does not exist. Set the fabric.client.gametest.testModResourcesPath system property to your test mod resources file path to automatically save it");
            throw new AssertionError((Object)"Template image does not exist");
        }
        LOGGER.info("Written absent screenshot template to {}", (Object)savePath);
    }

    @Override
    public TestInputImpl getInput() {
        return this.input;
    }

    @Override
    public TestWorldBuilder worldBuilder() {
        return new TestWorldBuilderImpl(this);
    }

    @Override
    public void restoreDefaultGameOptions() {
        ThreadingImpl.checkOnGametestThread("restoreDefaultGameOptions");
        this.runOnClient(client -> ((OptionsAccessor)((Object)Minecraft.getInstance().options)).invokeProcessOptions(new Options.FieldAccess(this){
            {
                Objects.requireNonNull(this$0);
            }

            @Override
            public int process(String key, int current) {
                return (Integer)DEFAULT_GAME_OPTIONS.get(key);
            }

            @Override
            public boolean process(String key, boolean current) {
                return (Boolean)DEFAULT_GAME_OPTIONS.get(key);
            }

            @Override
            public String process(String key, String current) {
                return (String)DEFAULT_GAME_OPTIONS.get(key);
            }

            @Override
            public float process(String key, float current) {
                return ((Float)DEFAULT_GAME_OPTIONS.get(key)).floatValue();
            }

            @Override
            public <T> T process(String key, T current, Function<String, T> decoder, Function<T, String> encoder) {
                return (T)DEFAULT_GAME_OPTIONS.get(key);
            }

            @Override
            public <T> void process(String key, OptionInstance<T> option) {
                option.set(DEFAULT_GAME_OPTIONS.get(key));
            }
        }));
    }

    @Override
    public <E extends Throwable> void runOnClient(FailableConsumer<Minecraft, E> action) throws E {
        ThreadingImpl.checkOnGametestThread("runOnClient");
        Preconditions.checkNotNull(action, "action");
        ThreadingImpl.runOnClient(() -> action.accept(Minecraft.getInstance()));
    }

    @Override
    public <T, E extends Throwable> T computeOnClient(FailableFunction<Minecraft, T, E> function) throws E {
        ThreadingImpl.checkOnGametestThread("computeOnClient");
        Preconditions.checkNotNull(function, "function");
        MutableObject result = new MutableObject();
        ThreadingImpl.runOnClient(() -> result.setValue(function.apply(Minecraft.getInstance())));
        return result.getValue();
    }

    private static /* synthetic */ String lambda$doAssertScreenshotContains$1(String rec$, Object xva$0) {
        return " '%s'".formatted(xva$0);
    }
}

