/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.gizmos;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.gizmos.GizmoPrimitives;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;
import org.joml.Vector4f;

@Environment(value=EnvType.CLIENT)
public class DrawableGizmoPrimitives
implements GizmoPrimitives {
    private final Group opaque = new Group(true);
    private final Group translucent = new Group(false);
    private boolean isEmpty = true;

    private Group getGroup(int color) {
        if (ARGB.alpha(color) < 255) {
            return this.translucent;
        }
        return this.opaque;
    }

    @Override
    public void addPoint(Vec3 pos, int color, float size) {
        this.getGroup((int)color).points.add(new Point(pos, color, size));
        this.isEmpty = false;
    }

    @Override
    public void addLine(Vec3 start, Vec3 end, int color, float width) {
        this.getGroup((int)color).lines.add(new Line(start, end, color, width));
        this.isEmpty = false;
    }

    @Override
    public void addTriangleFan(Vec3[] points, int color) {
        this.getGroup((int)color).triangleFans.add(new TriangleFan(points, color));
        this.isEmpty = false;
    }

    @Override
    public void addQuad(Vec3 a, Vec3 b, Vec3 c, Vec3 d, int color) {
        this.getGroup((int)color).quads.add(new Quad(a, b, c, d, color));
        this.isEmpty = false;
    }

    @Override
    public void addText(Vec3 pos, String text, TextGizmo.Style style) {
        this.getGroup((int)style.color()).texts.add(new Text(pos, text, style));
        this.isEmpty = false;
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferSource, CameraRenderState camera, Matrix4fc modelViewMatrix) {
        this.opaque.render(poseStack, bufferSource, camera, modelViewMatrix);
        this.translucent.render(poseStack, bufferSource, camera, modelViewMatrix);
    }

    public boolean isEmpty() {
        return this.isEmpty;
    }

    @Environment(value=EnvType.CLIENT)
    private record Group(boolean opaque, List<Line> lines, List<Quad> quads, List<TriangleFan> triangleFans, List<Text> texts, List<Point> points) {
        private Group(boolean opaque) {
            this(opaque, new ArrayList<Line>(), new ArrayList<Quad>(), new ArrayList<TriangleFan>(), new ArrayList<Text>(), new ArrayList<Point>());
        }

        public void render(PoseStack poseStack, MultiBufferSource bufferSource, CameraRenderState camera, Matrix4fc modelViewMatrix) {
            this.renderQuads(poseStack, bufferSource, camera);
            this.renderTriangleFans(poseStack, bufferSource, camera);
            this.renderLines(poseStack, bufferSource, camera, modelViewMatrix);
            this.renderTexts(poseStack, bufferSource, camera);
            this.renderPoints(poseStack, bufferSource, camera);
        }

        private void renderTexts(PoseStack poseStack, MultiBufferSource bufferSource, CameraRenderState camera) {
            Minecraft minecraft = Minecraft.getInstance();
            Font font = minecraft.font;
            if (!camera.initialized) {
                return;
            }
            double camX = camera.pos.x();
            double camY = camera.pos.y();
            double camZ = camera.pos.z();
            for (Text text : this.texts) {
                poseStack.pushPose();
                poseStack.translate((float)(text.pos().x() - camX), (float)(text.pos().y() - camY), (float)(text.pos().z() - camZ));
                poseStack.mulPose(camera.orientation);
                poseStack.scale(text.style.scale() / 16.0f, -text.style.scale() / 16.0f, text.style.scale() / 16.0f);
                float fontX = text.style.adjustLeft().isEmpty() ? (float)(-font.width(text.text)) / 2.0f : (float)(-text.style.adjustLeft().getAsDouble()) / text.style.scale();
                font.drawInBatch(text.text, fontX, 0.0f, text.style.color(), false, (Matrix4fc)poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
                poseStack.popPose();
            }
        }

        private void renderLines(PoseStack poseStack, MultiBufferSource bufferSource, CameraRenderState camera, Matrix4fc modelViewMatrix) {
            VertexConsumer builder = bufferSource.getBuffer(this.opaque ? RenderTypes.lines() : RenderTypes.linesTranslucent());
            PoseStack.Pose pose = poseStack.last();
            Vector4f start = new Vector4f();
            Vector4f end = new Vector4f();
            Vector4f startViewSpace = new Vector4f();
            Vector4f endViewSpace = new Vector4f();
            Vector4f intersectionInWorld = new Vector4f();
            double camX = camera.pos.x();
            double camY = camera.pos.y();
            double camZ = camera.pos.z();
            for (Line line : this.lines) {
                boolean endIsBehindCamera;
                start.set(line.start().x() - camX, line.start().y() - camY, line.start().z() - camZ, 1.0);
                end.set(line.end().x() - camX, line.end().y() - camY, line.end().z() - camZ, 1.0);
                start.mul(modelViewMatrix, startViewSpace);
                end.mul(modelViewMatrix, endViewSpace);
                boolean startIsBehindCamera = startViewSpace.z > -0.05f;
                boolean bl = endIsBehindCamera = endViewSpace.z > -0.05f;
                if (startIsBehindCamera && endIsBehindCamera) continue;
                if (startIsBehindCamera || endIsBehindCamera) {
                    float denom = endViewSpace.z - startViewSpace.z;
                    if (Math.abs(denom) < 1.0E-9f) continue;
                    float intersection = Mth.clamp((-0.05f - startViewSpace.z) / denom, 0.0f, 1.0f);
                    start.lerp(end, intersection, intersectionInWorld);
                    if (startIsBehindCamera) {
                        start.set(intersectionInWorld);
                    } else {
                        end.set(intersectionInWorld);
                    }
                }
                builder.addVertex(pose, start.x, start.y, start.z).setNormal(pose, end.x - start.x, end.y - start.y, end.z - start.z).setColor(line.color()).setLineWidth(line.width());
                builder.addVertex(pose, end.x, end.y, end.z).setNormal(pose, end.x - start.x, end.y - start.y, end.z - start.z).setColor(line.color()).setLineWidth(line.width());
            }
        }

        private void renderTriangleFans(PoseStack poseStack, MultiBufferSource bufferSource, CameraRenderState camera) {
            PoseStack.Pose pose = poseStack.last();
            double camX = camera.pos.x();
            double camY = camera.pos.y();
            double camZ = camera.pos.z();
            for (TriangleFan triangleFan : this.triangleFans) {
                VertexConsumer builder = bufferSource.getBuffer(RenderTypes.debugTriangleFan());
                for (Vec3 point : triangleFan.points()) {
                    builder.addVertex(pose, (float)(point.x() - camX), (float)(point.y() - camY), (float)(point.z() - camZ)).setColor(triangleFan.color());
                }
            }
        }

        private void renderQuads(PoseStack poseStack, MultiBufferSource bufferSource, CameraRenderState camera) {
            VertexConsumer builder = bufferSource.getBuffer(RenderTypes.debugFilledBox());
            PoseStack.Pose pose = poseStack.last();
            double camX = camera.pos.x();
            double camY = camera.pos.y();
            double camZ = camera.pos.z();
            for (Quad quad : this.quads) {
                builder.addVertex(pose, (float)(quad.a().x() - camX), (float)(quad.a().y() - camY), (float)(quad.a().z() - camZ)).setColor(quad.color());
                builder.addVertex(pose, (float)(quad.b().x() - camX), (float)(quad.b().y() - camY), (float)(quad.b().z() - camZ)).setColor(quad.color());
                builder.addVertex(pose, (float)(quad.c().x() - camX), (float)(quad.c().y() - camY), (float)(quad.c().z() - camZ)).setColor(quad.color());
                builder.addVertex(pose, (float)(quad.d().x() - camX), (float)(quad.d().y() - camY), (float)(quad.d().z() - camZ)).setColor(quad.color());
            }
        }

        private void renderPoints(PoseStack poseStack, MultiBufferSource bufferSource, CameraRenderState camera) {
            VertexConsumer builder = bufferSource.getBuffer(RenderTypes.debugPoint());
            PoseStack.Pose pose = poseStack.last();
            double camX = camera.pos.x();
            double camY = camera.pos.y();
            double camZ = camera.pos.z();
            for (Point point : this.points) {
                builder.addVertex(pose, (float)(point.pos.x() - camX), (float)(point.pos.y() - camY), (float)(point.pos.z() - camZ)).setColor(point.color()).setLineWidth(point.size());
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    private record Point(Vec3 pos, int color, float size) {
    }

    @Environment(value=EnvType.CLIENT)
    private record Line(Vec3 start, Vec3 end, int color, float width) {
    }

    @Environment(value=EnvType.CLIENT)
    private record TriangleFan(Vec3[] points, int color) {
    }

    @Environment(value=EnvType.CLIENT)
    private record Quad(Vec3 a, Vec3 b, Vec3 c, Vec3 d, int color) {
    }

    @Environment(value=EnvType.CLIENT)
    private record Text(Vec3 pos, String text, TextGizmo.Style style) {
    }
}

