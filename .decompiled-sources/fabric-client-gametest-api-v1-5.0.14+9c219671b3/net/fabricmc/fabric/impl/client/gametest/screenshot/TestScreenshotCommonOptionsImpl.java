/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.gametest.screenshot;

import com.google.common.base.Preconditions;
import java.nio.file.Path;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotCommonOptions;
import org.joml.Vector2i;
import org.jspecify.annotations.Nullable;

public abstract class TestScreenshotCommonOptionsImpl<SELF extends TestScreenshotCommonOptions<SELF>>
implements TestScreenshotCommonOptions<SELF> {
    public boolean counterPrefix = true;
    public float deltaTicks = 1.0f;
    public @Nullable Vector2i size;
    public @Nullable Path destinationDir;

    @Override
    public SELF disableCounterPrefix() {
        this.counterPrefix = false;
        return this.getThis();
    }

    @Override
    public SELF withDeltaTicks(float deltaTicks) {
        Preconditions.checkArgument(deltaTicks >= 0.0f && deltaTicks <= 1.0f, "deltaTicks must be between 0 and 1");
        this.deltaTicks = deltaTicks;
        return this.getThis();
    }

    @Override
    public SELF withSize(int width, int height) {
        Preconditions.checkArgument(width > 0, "width must be positive");
        Preconditions.checkArgument(height > 0, "height must be positive");
        this.size = new Vector2i(width, height);
        return this.getThis();
    }

    @Override
    public SELF withDestinationDir(Path destinationDir) {
        Preconditions.checkNotNull(destinationDir, "destinationDir");
        this.destinationDir = destinationDir;
        return this.getThis();
    }

    private SELF getThis() {
        return (SELF)this;
    }
}

