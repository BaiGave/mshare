/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.control;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.Control;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MoveControl
implements Control {
    public static final float MIN_SPEED = 5.0E-4f;
    public static final float MIN_SPEED_SQR = 2.5000003E-7f;
    protected static final int MAX_TURN = 90;
    protected final Mob mob;
    protected double wantedX;
    protected double wantedY;
    protected double wantedZ;
    protected double speedModifier;
    protected float strafeForwards;
    protected float strafeRight;
    protected Operation operation = Operation.WAIT;

    public MoveControl(Mob mob) {
        this.mob = mob;
    }

    public boolean hasWanted() {
        return this.operation == Operation.MOVE_TO;
    }

    public double getSpeedModifier() {
        return this.speedModifier;
    }

    public void setWantedPosition(double x, double y, double z, double speedModifier) {
        this.wantedX = x;
        this.wantedY = y;
        this.wantedZ = z;
        this.speedModifier = speedModifier;
        if (this.operation != Operation.JUMPING) {
            this.operation = Operation.MOVE_TO;
        }
    }

    public void strafe(float forwards, float right) {
        this.operation = Operation.STRAFE;
        this.strafeForwards = forwards;
        this.strafeRight = right;
        this.speedModifier = 0.25;
    }

    public void tick() {
        if (this.operation == Operation.STRAFE) {
            float dz;
            float speed = (float)this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED);
            float speedModified = (float)this.speedModifier * speed;
            float xa = this.strafeForwards;
            float za = this.strafeRight;
            float dist = Mth.sqrt(xa * xa + za * za);
            if (dist < 1.0f) {
                dist = 1.0f;
            }
            dist = speedModified / dist;
            float sin = Mth.sin(this.mob.getYRot() * ((float)Math.PI / 180));
            float cos = Mth.cos(this.mob.getYRot() * ((float)Math.PI / 180));
            float dx = (xa *= dist) * cos - (za *= dist) * sin;
            if (!this.isWalkable(dx, dz = za * cos + xa * sin)) {
                this.strafeForwards = 1.0f;
                this.strafeRight = 0.0f;
            }
            this.mob.setSpeed(speedModified);
            this.mob.setZza(this.strafeForwards);
            this.mob.setXxa(this.strafeRight);
            this.operation = Operation.WAIT;
        } else if (this.operation == Operation.MOVE_TO) {
            this.operation = Operation.WAIT;
            double xd = this.wantedX - this.mob.getX();
            double zd = this.wantedZ - this.mob.getZ();
            double yd = this.wantedY - this.mob.getY();
            double dd = xd * xd + yd * yd + zd * zd;
            if (dd < 2.500000277905201E-7) {
                this.mob.setZza(0.0f);
                return;
            }
            float yRotD = (float)(Mth.atan2(zd, xd) * 57.2957763671875) - 90.0f;
            this.mob.setYRot(this.rotlerp(this.mob.getYRot(), yRotD, 90.0f));
            this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
            BlockPos pos = this.mob.blockPosition();
            BlockState blockState = this.mob.level().getBlockState(pos);
            VoxelShape shape = blockState.getCollisionShape(this.mob.level(), pos);
            if (yd > (double)this.mob.maxUpStep() && xd * xd + zd * zd < (double)Math.max(1.0f, this.mob.getBbWidth()) || !shape.isEmpty() && this.mob.getY() < shape.max(Direction.Axis.Y) + (double)pos.getY() && !blockState.is(BlockTags.DOORS) && !blockState.is(BlockTags.FENCES)) {
                this.mob.getJumpControl().jump();
                this.operation = Operation.JUMPING;
            }
        } else if (this.operation == Operation.JUMPING) {
            this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
            if (this.mob.onGround() || this.mob.isInLiquid() && this.mob.isAffectedByFluids()) {
                this.operation = Operation.WAIT;
            }
        } else {
            this.mob.setZza(0.0f);
        }
    }

    private boolean isWalkable(float dx, float dz) {
        NodeEvaluator nodeEvaluator;
        PathNavigation pathNavigation = this.mob.getNavigation();
        return pathNavigation == null || (nodeEvaluator = pathNavigation.getNodeEvaluator()) == null || nodeEvaluator.getPathType(this.mob, BlockPos.containing(this.mob.getX() + (double)dx, this.mob.getBlockY(), this.mob.getZ() + (double)dz)) == PathType.WALKABLE;
    }

    protected float rotlerp(float a, float b, float max) {
        float result;
        float diff = Mth.wrapDegrees(b - a);
        if (diff > max) {
            diff = max;
        }
        if (diff < -max) {
            diff = -max;
        }
        if ((result = a + diff) < 0.0f) {
            result += 360.0f;
        } else if (result > 360.0f) {
            result -= 360.0f;
        }
        return result;
    }

    public double getWantedX() {
        return this.wantedX;
    }

    public double getWantedY() {
        return this.wantedY;
    }

    public double getWantedZ() {
        return this.wantedZ;
    }

    public void setWait() {
        this.operation = Operation.WAIT;
    }

    protected static enum Operation {
        WAIT,
        MOVE_TO,
        STRAFE,
        JUMPING;

    }
}

