/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.gametest;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import net.fabricmc.fabric.api.client.gametest.v1.TestInput;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.impl.client.gametest.threading.ThreadingImpl;
import net.fabricmc.fabric.impl.client.gametest.util.WindowHooks;
import net.fabricmc.fabric.mixin.client.gametest.input.KeyMappingAccessor;
import net.fabricmc.fabric.mixin.client.gametest.input.KeyboardHandlerAccessor;
import net.fabricmc.fabric.mixin.client.gametest.input.MouseHandlerAccessor;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.util.Util;

public final class TestInputImpl
implements TestInput {
    private static final Set<InputConstants.Key> KEYS_DOWN = new HashSet<InputConstants.Key>();
    private static final boolean IS_MACOS = Util.getPlatform() == Util.OS.OSX;
    private final ClientGameTestContext context;

    public TestInputImpl(ClientGameTestContext context) {
        this.context = context;
    }

    public static boolean isKeyDown(int keyCode) {
        return KEYS_DOWN.contains(InputConstants.Type.KEYSYM.getOrCreate(keyCode));
    }

    public void clearKeysDown() {
        for (InputConstants.Key key : new ArrayList<InputConstants.Key>(KEYS_DOWN)) {
            this.releaseKey(key);
        }
    }

    @Override
    public void holdKey(KeyMapping keyMapping) {
        ThreadingImpl.checkOnGametestThread("holdKey");
        Preconditions.checkNotNull(keyMapping, "keyMapping");
        this.holdKey(TestInputImpl.getBoundKey(keyMapping, "hold"));
    }

    @Override
    public void holdKey(Function<Options, KeyMapping> keyMappingGetter) {
        ThreadingImpl.checkOnGametestThread("holdKey");
        Preconditions.checkNotNull(keyMappingGetter, "keyMappingGetter");
        KeyMapping keyMapping = this.context.computeOnClient(client -> (KeyMapping)keyMappingGetter.apply(client.options));
        this.holdKey(keyMapping);
    }

    @Override
    public void holdKey(InputConstants.Key key) {
        ThreadingImpl.checkOnGametestThread("holdKey");
        Preconditions.checkNotNull(key, "key");
        if (KEYS_DOWN.add(key)) {
            this.context.runOnClient(client -> TestInputImpl.pressOrReleaseKey(client, key, 1));
        }
    }

    @Override
    public void holdKey(int keyCode) {
        ThreadingImpl.checkOnGametestThread("holdKey");
        this.holdKey(InputConstants.Type.KEYSYM.getOrCreate(keyCode));
    }

    @Override
    public void holdMouse(int button) {
        ThreadingImpl.checkOnGametestThread("holdMouse");
        this.holdKey(InputConstants.Type.MOUSE.getOrCreate(button));
    }

    @Override
    public void holdControl() {
        ThreadingImpl.checkOnGametestThread("holdControl");
        this.holdKey(IS_MACOS ? 343 : 341);
    }

    @Override
    public void holdShift() {
        ThreadingImpl.checkOnGametestThread("holdShift");
        this.holdKey(340);
    }

    @Override
    public void holdAlt() {
        ThreadingImpl.checkOnGametestThread("holdAlt");
        this.holdKey(342);
    }

    @Override
    public void releaseKey(KeyMapping keyMapping) {
        ThreadingImpl.checkOnGametestThread("releaseKey");
        Preconditions.checkNotNull(keyMapping, "keyMapping");
        this.releaseKey(TestInputImpl.getBoundKey(keyMapping, "release"));
    }

    @Override
    public void releaseKey(Function<Options, KeyMapping> keyMappingGetter) {
        ThreadingImpl.checkOnGametestThread("releaseKey");
        Preconditions.checkNotNull(keyMappingGetter, "keyMappingGetter");
        KeyMapping keyMapping = this.context.computeOnClient(client -> (KeyMapping)keyMappingGetter.apply(client.options));
        this.releaseKey(keyMapping);
    }

    @Override
    public void releaseKey(InputConstants.Key key) {
        ThreadingImpl.checkOnGametestThread("releaseKey");
        Preconditions.checkNotNull(key, "key");
        if (KEYS_DOWN.remove(key)) {
            this.context.runOnClient(client -> TestInputImpl.pressOrReleaseKey(client, key, 0));
        }
    }

    @Override
    public void releaseKey(int keyCode) {
        ThreadingImpl.checkOnGametestThread("releaseKey");
        this.releaseKey(InputConstants.Type.KEYSYM.getOrCreate(keyCode));
    }

    @Override
    public void releaseMouse(int button) {
        ThreadingImpl.checkOnGametestThread("releaseMouse");
        this.releaseKey(InputConstants.Type.MOUSE.getOrCreate(button));
    }

    @Override
    public void releaseControl() {
        ThreadingImpl.checkOnGametestThread("releaseControl");
        this.releaseKey(IS_MACOS ? 343 : 341);
    }

    @Override
    public void releaseShift() {
        ThreadingImpl.checkOnGametestThread("releaseShift");
        this.releaseKey(340);
    }

    @Override
    public void releaseAlt() {
        ThreadingImpl.checkOnGametestThread("releaseAlt");
        this.releaseKey(342);
    }

    private static void pressOrReleaseKey(Minecraft client, InputConstants.Key key, int action) {
        switch (key.getType()) {
            case KEYSYM: {
                ((KeyboardHandlerAccessor)((Object)client.keyboardHandler)).invokeKeyPress(client.getWindow().handle(), action, new KeyEvent(key.getValue(), 0, 0));
                break;
            }
            case SCANCODE: {
                ((KeyboardHandlerAccessor)((Object)client.keyboardHandler)).invokeKeyPress(client.getWindow().handle(), action, new KeyEvent(-1, key.getValue(), 0));
                break;
            }
            case MOUSE: {
                ((MouseHandlerAccessor)((Object)client.mouseHandler)).invokeOnButton(client.getWindow().handle(), new MouseButtonInfo(key.getValue(), 0), action);
            }
        }
    }

    @Override
    public void pressKey(KeyMapping keyMapping) {
        ThreadingImpl.checkOnGametestThread("pressKey");
        Preconditions.checkNotNull(keyMapping, "keyMapping");
        this.pressKey(TestInputImpl.getBoundKey(keyMapping, "press"));
    }

    @Override
    public void pressKey(Function<Options, KeyMapping> keyMappingGetter) {
        ThreadingImpl.checkOnGametestThread("pressKey");
        Preconditions.checkNotNull(keyMappingGetter, "keyMappingGetter");
        KeyMapping keyMapping = this.context.computeOnClient(client -> (KeyMapping)keyMappingGetter.apply(client.options));
        this.pressKey(keyMapping);
    }

    @Override
    public void pressKey(InputConstants.Key key) {
        ThreadingImpl.checkOnGametestThread("pressKey");
        Preconditions.checkNotNull(key, "key");
        this.holdKey(key);
        this.releaseKey(key);
        this.context.waitTick();
    }

    @Override
    public void pressKey(int keyCode) {
        ThreadingImpl.checkOnGametestThread("pressKey");
        this.pressKey(InputConstants.Type.KEYSYM.getOrCreate(keyCode));
    }

    @Override
    public void pressMouse(int button) {
        ThreadingImpl.checkOnGametestThread("pressMouse");
        this.pressKey(InputConstants.Type.MOUSE.getOrCreate(button));
    }

    @Override
    public void holdKeyFor(KeyMapping keyMapping, int ticks) {
        ThreadingImpl.checkOnGametestThread("holdKeyFor");
        Preconditions.checkNotNull(keyMapping, "keyMapping");
        Preconditions.checkArgument(ticks >= 0, "ticks cannot be negative");
        this.holdKeyFor(TestInputImpl.getBoundKey(keyMapping, "hold"), ticks);
    }

    @Override
    public void holdKeyFor(Function<Options, KeyMapping> keyMappingGetter, int ticks) {
        ThreadingImpl.checkOnGametestThread("holdKeyFor");
        Preconditions.checkNotNull(keyMappingGetter, "keyMappingGetter");
        Preconditions.checkArgument(ticks >= 0, "ticks cannot be negative");
        KeyMapping keyMapping = this.context.computeOnClient(client -> (KeyMapping)keyMappingGetter.apply(client.options));
        this.holdKeyFor(keyMapping, ticks);
    }

    @Override
    public void holdKeyFor(InputConstants.Key key, int ticks) {
        ThreadingImpl.checkOnGametestThread("holdKeyFor");
        Preconditions.checkNotNull(key, "key");
        Preconditions.checkArgument(ticks >= 0, "ticks cannot be negative");
        this.holdKey(key);
        this.context.waitTicks(ticks);
        this.releaseKey(key);
    }

    @Override
    public void holdKeyFor(int keyCode, int ticks) {
        ThreadingImpl.checkOnGametestThread("holdKeyFor");
        Preconditions.checkArgument(ticks >= 0, "ticks cannot be negative");
        this.holdKeyFor(InputConstants.Type.KEYSYM.getOrCreate(keyCode), ticks);
    }

    @Override
    public void holdMouseFor(int button, int ticks) {
        ThreadingImpl.checkOnGametestThread("holdMouseFor");
        Preconditions.checkArgument(ticks >= 0, "ticks cannot be negative");
        this.holdKeyFor(InputConstants.Type.MOUSE.getOrCreate(button), ticks);
    }

    @Override
    public void typeChar(int codePoint) {
        ThreadingImpl.checkOnGametestThread("typeChar");
        this.context.runOnClient(client -> ((KeyboardHandlerAccessor)((Object)client.keyboardHandler)).invokeCharTyped(client.getWindow().handle(), new CharacterEvent(codePoint)));
    }

    @Override
    public void typeChars(String chars) {
        ThreadingImpl.checkOnGametestThread("typeChars");
        this.context.runOnClient(client -> chars.chars().forEach(codePoint -> ((KeyboardHandlerAccessor)((Object)client.keyboardHandler)).invokeCharTyped(client.getWindow().handle(), new CharacterEvent(codePoint))));
    }

    @Override
    public void scroll(double amount) {
        ThreadingImpl.checkOnGametestThread("scroll");
        this.scroll(0.0, amount);
    }

    @Override
    public void scroll(double xAmount, double yAmount) {
        ThreadingImpl.checkOnGametestThread("scroll");
        this.context.runOnClient(client -> ((MouseHandlerAccessor)((Object)client.mouseHandler)).invokeOnScroll(client.getWindow().handle(), xAmount, yAmount));
    }

    @Override
    public void setCursorPos(double x, double y) {
        ThreadingImpl.checkOnGametestThread("setCursorPos");
        this.context.runOnClient(client -> ((MouseHandlerAccessor)((Object)client.mouseHandler)).invokeOnMove(client.getWindow().handle(), x, y));
    }

    @Override
    public void moveCursor(double deltaX, double deltaY) {
        ThreadingImpl.checkOnGametestThread("moveCursor");
        this.context.runOnClient(client -> {
            double newX = client.mouseHandler.xpos() + deltaX;
            double newY = client.mouseHandler.ypos() + deltaY;
            ((MouseHandlerAccessor)((Object)client.mouseHandler)).invokeOnMove(client.getWindow().handle(), newX, newY);
        });
    }

    @Override
    public void resizeWindow(int width, int height) {
        ThreadingImpl.checkOnGametestThread("resizeWindow");
        Preconditions.checkArgument(width > 0, "width must be positive");
        Preconditions.checkArgument(height > 0, "height must be positive");
        this.context.runOnClient(client -> ((WindowHooks)((Object)client.getWindow())).fabric_resize(width, height));
    }

    private static InputConstants.Key getBoundKey(KeyMapping keyMapping, String action) {
        InputConstants.Key boundKey = ((KeyMappingAccessor)((Object)keyMapping)).getKey();
        if (boundKey == InputConstants.UNKNOWN) {
            throw new AssertionError((Object)"Cannot %s binding '%s' because it isn't bound to a key".formatted(action, keyMapping.getName()));
        }
        return boundKey;
    }
}

