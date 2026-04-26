/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.gametest.v1;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.minecraft.world.level.block.Rotation;

@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.METHOD})
@Documented
public @interface GameTest {
    public String environment() default "minecraft:default";

    public String structure() default "fabric-gametest-api-v1:empty";

    public int maxTicks() default 20;

    public int setupTicks() default 0;

    public boolean required() default true;

    public Rotation rotation() default Rotation.NONE;

    public boolean manualOnly() default false;

    public int maxAttempts() default 1;

    public int requiredSuccesses() default 1;

    public boolean skyAccess() default false;

    public int padding() default 1;
}

