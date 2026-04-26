/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.rendertype;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class LayeringTransform {
    private final String name;
    private final @Nullable Consumer<Matrix4fStack> modifier;
    public static final LayeringTransform NO_LAYERING = new LayeringTransform("no_layering", null);
    public static final LayeringTransform VIEW_OFFSET_Z_LAYERING = new LayeringTransform("view_offset_z_layering", modelViewStack -> RenderSystem.getProjectionType().applyLayeringTransform((Matrix4f)modelViewStack, 1.0f));
    public static final LayeringTransform VIEW_OFFSET_Z_LAYERING_FORWARD = new LayeringTransform("view_offset_z_layering_forward", modelViewStack -> RenderSystem.getProjectionType().applyLayeringTransform((Matrix4f)modelViewStack, -1.0f));

    public LayeringTransform(String name, @Nullable Consumer<Matrix4fStack> modifier) {
        this.name = name;
        this.modifier = modifier;
    }

    public String toString() {
        return "LayeringTransform[" + this.name + "]";
    }

    public @Nullable Consumer<Matrix4fStack> getModifier() {
        return this.modifier;
    }
}

