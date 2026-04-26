/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.systems;

import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuQuery;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.OptionalLong;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class TimerQuery {
    private @Nullable CommandEncoder activeEncoder;
    private @Nullable GpuQuery activeGpuQuery;

    public static TimerQuery getInstance() {
        return TimerQueryLazyLoader.INSTANCE;
    }

    public boolean isRecording() {
        return this.activeGpuQuery != null;
    }

    public void beginProfile() {
        RenderSystem.assertOnRenderThread();
        if (this.activeGpuQuery != null) {
            throw new IllegalStateException("Current profile not ended");
        }
        this.activeEncoder = RenderSystem.getDevice().createCommandEncoder();
        this.activeGpuQuery = this.activeEncoder.timerQueryBegin();
    }

    public FrameProfile endProfile() {
        RenderSystem.assertOnRenderThread();
        if (this.activeGpuQuery == null || this.activeEncoder == null) {
            throw new IllegalStateException("endProfile called before beginProfile");
        }
        this.activeEncoder.timerQueryEnd(this.activeGpuQuery);
        FrameProfile frameProfile = new FrameProfile(this.activeGpuQuery);
        this.activeGpuQuery = null;
        this.activeEncoder = null;
        return frameProfile;
    }

    @Environment(value=EnvType.CLIENT)
    private static class TimerQueryLazyLoader {
        private static final TimerQuery INSTANCE = TimerQueryLazyLoader.instantiate();

        private TimerQueryLazyLoader() {
        }

        private static TimerQuery instantiate() {
            return new TimerQuery();
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class FrameProfile {
        private static final long NO_RESULT = 0L;
        private static final long CANCELLED_RESULT = -1L;
        private final GpuQuery gpuQuery;
        private long timerResult = 0L;

        private FrameProfile(GpuQuery gpuQuery) {
            this.gpuQuery = gpuQuery;
        }

        public void cancel() {
            RenderSystem.assertOnRenderThread();
            if (this.timerResult != 0L) {
                return;
            }
            this.timerResult = -1L;
            this.gpuQuery.close();
        }

        public boolean isDone() {
            RenderSystem.assertOnRenderThread();
            if (this.timerResult != 0L) {
                return true;
            }
            OptionalLong value = this.gpuQuery.getValue();
            if (value.isPresent()) {
                this.timerResult = value.getAsLong();
                this.gpuQuery.close();
                return true;
            }
            return false;
        }

        public long get() {
            OptionalLong value;
            RenderSystem.assertOnRenderThread();
            if (this.timerResult == 0L && (value = this.gpuQuery.getValue()).isPresent()) {
                this.timerResult = value.getAsLong();
                this.gpuQuery.close();
            }
            return this.timerResult;
        }
    }
}

