/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class ShapeRenderer {
    public static void renderShape(PoseStack poseStack, VertexConsumer builder, VoxelShape shape, double x, double y, double z, int color, float width) {
        PoseStack.Pose pose = poseStack.last();
        shape.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
            Vector3f normal = new Vector3f((float)(x2 - x1), (float)(y2 - y1), (float)(z2 - z1)).normalize();
            builder.addVertex(pose, (float)(x1 + x), (float)(y1 + y), (float)(z1 + z)).setColor(color).setNormal(pose, normal).setLineWidth(width);
            builder.addVertex(pose, (float)(x2 + x), (float)(y2 + y), (float)(z2 + z)).setColor(color).setNormal(pose, normal).setLineWidth(width);
        });
    }
}

