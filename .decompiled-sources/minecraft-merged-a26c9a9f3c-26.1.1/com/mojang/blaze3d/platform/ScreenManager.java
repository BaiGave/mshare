/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.MonitorCreator;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWMonitorCallback;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ScreenManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Long2ObjectMap<Monitor> monitors = new Long2ObjectOpenHashMap<Monitor>();
    private final MonitorCreator monitorCreator;

    public ScreenManager(MonitorCreator monitorCreator) {
        this.monitorCreator = monitorCreator;
        GLFW.glfwSetMonitorCallback(this::onMonitorChange);
        PointerBuffer buffer = GLFW.glfwGetMonitors();
        if (buffer != null) {
            for (int i = 0; i < buffer.limit(); ++i) {
                long monitor = buffer.get(i);
                this.monitors.put(monitor, monitorCreator.createMonitor(monitor));
            }
        }
    }

    private void onMonitorChange(long monitor, int event) {
        RenderSystem.assertOnRenderThread();
        if (event == 262145) {
            this.monitors.put(monitor, this.monitorCreator.createMonitor(monitor));
            LOGGER.debug("Monitor {} connected. Current monitors: {}", (Object)monitor, (Object)this.monitors);
        } else if (event == 262146) {
            this.monitors.remove(monitor);
            LOGGER.debug("Monitor {} disconnected. Current monitors: {}", (Object)monitor, (Object)this.monitors);
        }
    }

    public @Nullable Monitor getMonitor(long monitor) {
        return (Monitor)this.monitors.get(monitor);
    }

    public @Nullable Monitor findBestMonitor(Window window) {
        long windowMonitor = GLFW.glfwGetWindowMonitor(window.handle());
        if (windowMonitor != 0L) {
            return this.getMonitor(windowMonitor);
        }
        int winMinX = window.getX();
        int winMaxX = winMinX + window.getScreenWidth();
        int winMinY = window.getY();
        int winMaxY = winMinY + window.getScreenHeight();
        int maxArea = -1;
        Monitor result = null;
        long primaryMonitor = GLFW.glfwGetPrimaryMonitor();
        LOGGER.debug("Selecting monitor - primary: {}, current monitors: {}", (Object)primaryMonitor, (Object)this.monitors);
        for (Monitor monitor : this.monitors.values()) {
            int sy;
            int monMinX = monitor.getX();
            int monMaxX = monMinX + monitor.getCurrentMode().getWidth();
            int monMinY = monitor.getY();
            int monMaxY = monMinY + monitor.getCurrentMode().getHeight();
            int minX = ScreenManager.clamp(winMinX, monMinX, monMaxX);
            int maxX = ScreenManager.clamp(winMaxX, monMinX, monMaxX);
            int minY = ScreenManager.clamp(winMinY, monMinY, monMaxY);
            int maxY = ScreenManager.clamp(winMaxY, monMinY, monMaxY);
            int sx = Math.max(0, maxX - minX);
            int area = sx * (sy = Math.max(0, maxY - minY));
            if (area > maxArea) {
                result = monitor;
                maxArea = area;
                continue;
            }
            if (area != maxArea || primaryMonitor != monitor.getMonitor()) continue;
            LOGGER.debug("Primary monitor {} is preferred to monitor {}", (Object)monitor, (Object)result);
            result = monitor;
        }
        LOGGER.debug("Selected monitor: {}", (Object)result);
        return result;
    }

    public static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    public void shutdown() {
        RenderSystem.assertOnRenderThread();
        GLFWMonitorCallback callback = GLFW.glfwSetMonitorCallback(null);
        if (callback != null) {
            callback.free();
        }
    }
}

