/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.realms;

import com.google.common.util.concurrent.RateLimiter;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GameNarrator;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class RepeatedNarrator {
    private final float permitsPerSecond;
    private final AtomicReference<@Nullable Params> params = new AtomicReference();

    public RepeatedNarrator(Duration repeatDelay) {
        this.permitsPerSecond = 1000.0f / (float)repeatDelay.toMillis();
    }

    public void narrate(GameNarrator narrator, Component narration) {
        Params params = this.params.updateAndGet(existing -> {
            if (existing == null || !narration.equals(existing.narration)) {
                return new Params(narration, RateLimiter.create(this.permitsPerSecond));
            }
            return existing;
        });
        if (params.rateLimiter.tryAcquire(1)) {
            narrator.saySystemNow(narration);
        }
    }

    @Environment(value=EnvType.CLIENT)
    private static class Params {
        private final Component narration;
        private final RateLimiter rateLimiter;

        Params(Component narration, RateLimiter rateLimiter) {
            this.narration = narration;
            this.rateLimiter = rateLimiter;
        }
    }
}

