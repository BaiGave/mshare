/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlackstoneReplaceProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockAgeProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.LavaSubmergedBlockProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProtectedBlockProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.RandomBlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class RuinedPortalPiece
extends TemplateStructurePiece {
    private static final float PROBABILITY_OF_GOLD_GONE = 0.3f;
    private static final float PROBABILITY_OF_MAGMA_INSTEAD_OF_NETHERRACK = 0.07f;
    private static final float PROBABILITY_OF_MAGMA_INSTEAD_OF_LAVA = 0.2f;
    private final VerticalPlacement verticalPlacement;
    private final Properties properties;

    public RuinedPortalPiece(StructureTemplateManager structureTemplateManager, BlockPos templatePosition, VerticalPlacement verticalPlacement, Properties properties, Identifier templateLocation, StructureTemplate template, Rotation rotation, Mirror mirror, BlockPos pivot) {
        super(StructurePieceType.RUINED_PORTAL, 0, structureTemplateManager, templateLocation, templateLocation.toString(), RuinedPortalPiece.makeSettings(mirror, rotation, verticalPlacement, pivot, properties), templatePosition);
        this.verticalPlacement = verticalPlacement;
        this.properties = properties;
    }

    public RuinedPortalPiece(StructureTemplateManager structureTemplateManager, CompoundTag tag) {
        super(StructurePieceType.RUINED_PORTAL, tag, structureTemplateManager, location -> RuinedPortalPiece.makeSettings(structureTemplateManager, tag, location));
        this.verticalPlacement = tag.read("VerticalPlacement", VerticalPlacement.CODEC).orElseThrow();
        this.properties = tag.read("Properties", Properties.CODEC).orElseThrow();
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        super.addAdditionalSaveData(context, tag);
        tag.store("Rotation", Rotation.LEGACY_CODEC, this.placeSettings.getRotation());
        tag.store("Mirror", Mirror.LEGACY_CODEC, this.placeSettings.getMirror());
        tag.store("VerticalPlacement", VerticalPlacement.CODEC, this.verticalPlacement);
        tag.store("Properties", Properties.CODEC, this.properties);
    }

    private static StructurePlaceSettings makeSettings(StructureTemplateManager structureTemplateManager, CompoundTag tag, Identifier location) {
        StructureTemplate template = structureTemplateManager.getOrCreate(location);
        BlockPos pivot = new BlockPos(template.getSize().getX() / 2, 0, template.getSize().getZ() / 2);
        return RuinedPortalPiece.makeSettings(tag.read("Mirror", Mirror.LEGACY_CODEC).orElseThrow(), tag.read("Rotation", Rotation.LEGACY_CODEC).orElseThrow(), tag.read("VerticalPlacement", VerticalPlacement.CODEC).orElseThrow(), pivot, (Properties)Properties.CODEC.parse(new Dynamic<Tag>(NbtOps.INSTANCE, tag.get("Properties"))).getPartialOrThrow());
    }

    private static StructurePlaceSettings makeSettings(Mirror mirror, Rotation rotation, VerticalPlacement verticalPlacement, BlockPos pivot, Properties properties) {
        BlockIgnoreProcessor ignoreProcessor = properties.airPocket ? BlockIgnoreProcessor.STRUCTURE_BLOCK : BlockIgnoreProcessor.STRUCTURE_AND_AIR;
        ArrayList<ProcessorRule> rules = Lists.newArrayList();
        rules.add(RuinedPortalPiece.getBlockReplaceRule(Blocks.GOLD_BLOCK, 0.3f, Blocks.AIR));
        rules.add(RuinedPortalPiece.getLavaProcessorRule(verticalPlacement, properties));
        if (!properties.cold) {
            rules.add(RuinedPortalPiece.getBlockReplaceRule(Blocks.NETHERRACK, 0.07f, Blocks.MAGMA_BLOCK));
        }
        StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setMirror(mirror).setRotationPivot(pivot).addProcessor(ignoreProcessor).addProcessor(new RuleProcessor(rules)).addProcessor(new BlockAgeProcessor(properties.mossiness)).addProcessor(new ProtectedBlockProcessor(BlockTags.FEATURES_CANNOT_REPLACE)).addProcessor(new LavaSubmergedBlockProcessor());
        if (properties.replaceWithBlackstone) {
            settings.addProcessor(BlackstoneReplaceProcessor.INSTANCE);
        }
        return settings;
    }

    private static ProcessorRule getLavaProcessorRule(VerticalPlacement verticalPlacement, Properties properties) {
        if (verticalPlacement == VerticalPlacement.ON_OCEAN_FLOOR) {
            return RuinedPortalPiece.getBlockReplaceRule(Blocks.LAVA, Blocks.MAGMA_BLOCK);
        }
        if (properties.cold) {
            return RuinedPortalPiece.getBlockReplaceRule(Blocks.LAVA, Blocks.NETHERRACK);
        }
        return RuinedPortalPiece.getBlockReplaceRule(Blocks.LAVA, 0.2f, Blocks.MAGMA_BLOCK);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BoundingBox chunkBB, ChunkPos chunkPos, BlockPos referencePos) {
        BoundingBox boundingBox = this.template.getBoundingBox(this.placeSettings, this.templatePosition);
        if (!chunkBB.isInside(boundingBox.getCenter())) {
            return;
        }
        chunkBB.encapsulate(boundingBox);
        super.postProcess(level, structureManager, generator, random, chunkBB, chunkPos, referencePos);
        this.spreadNetherrack(random, level);
        this.addNetherrackDripColumnsBelowPortal(random, level);
        if (this.properties.vines || this.properties.overgrown) {
            BlockPos.betweenClosedStream(this.getBoundingBox()).forEach(pos -> {
                if (this.properties.vines) {
                    this.maybeAddVines(random, level, (BlockPos)pos);
                }
                if (this.properties.overgrown) {
                    this.maybeAddLeavesAbove(random, level, (BlockPos)pos);
                }
            });
        }
    }

    @Override
    protected void handleDataMarker(String markerId, BlockPos pos, ServerLevelAccessor level, RandomSource random, BoundingBox chunkBB) {
    }

    private void maybeAddVines(RandomSource random, LevelAccessor level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.is(Blocks.VINE)) {
            return;
        }
        Direction direction = RuinedPortalPiece.getRandomHorizontalDirection(random);
        BlockPos neighbourPos = pos.relative(direction);
        BlockState neighourState = level.getBlockState(neighbourPos);
        if (!neighourState.isAir()) {
            return;
        }
        if (!Block.isFaceFull(state.getCollisionShape(level, pos), direction)) {
            return;
        }
        BooleanProperty vineDir = VineBlock.getPropertyForFace(direction.getOpposite());
        level.setBlock(neighbourPos, (BlockState)Blocks.VINE.defaultBlockState().setValue(vineDir, true), 3);
    }

    private void maybeAddLeavesAbove(RandomSource random, LevelAccessor level, BlockPos pos) {
        if (random.nextFloat() < 0.5f && level.getBlockState(pos).is(Blocks.NETHERRACK) && level.getBlockState(pos.above()).isAir()) {
            level.setBlock(pos.above(), (BlockState)Blocks.JUNGLE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true), 3);
        }
    }

    private void addNetherrackDripColumnsBelowPortal(RandomSource random, LevelAccessor level) {
        for (int x = this.boundingBox.minX() + 1; x < this.boundingBox.maxX(); ++x) {
            for (int z = this.boundingBox.minZ() + 1; z < this.boundingBox.maxZ(); ++z) {
                BlockPos pos = new BlockPos(x, this.boundingBox.minY(), z);
                if (!level.getBlockState(pos).is(Blocks.NETHERRACK)) continue;
                this.addNetherrackDripColumn(random, level, pos.below());
            }
        }
    }

    private void addNetherrackDripColumn(RandomSource random, LevelAccessor level, BlockPos pos) {
        BlockPos.MutableBlockPos currentPos = pos.mutable();
        this.placeNetherrackOrMagma(random, level, currentPos);
        for (int remainingCap = 8; remainingCap > 0 && random.nextFloat() < 0.5f; --remainingCap) {
            currentPos.move(Direction.DOWN);
            this.placeNetherrackOrMagma(random, level, currentPos);
        }
    }

    private void spreadNetherrack(RandomSource random, LevelAccessor level) {
        boolean followGroundSurface = this.verticalPlacement == VerticalPlacement.ON_LAND_SURFACE || this.verticalPlacement == VerticalPlacement.ON_OCEAN_FLOOR;
        BlockPos center = this.boundingBox.getCenter();
        int centerX = center.getX();
        int centerZ = center.getZ();
        float[] netherrackProbabilityByDistance = new float[]{1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.9f, 0.9f, 0.8f, 0.7f, 0.6f, 0.4f, 0.2f};
        int maxDistance = netherrackProbabilityByDistance.length;
        int averageWidth = (this.boundingBox.getXSpan() + this.boundingBox.getZSpan()) / 2;
        int distanceAdjustment = random.nextInt(Math.max(1, 8 - averageWidth / 2));
        int maxYDiff = 3;
        BlockPos.MutableBlockPos pos = BlockPos.ZERO.mutable();
        for (int x = centerX - maxDistance; x <= centerX + maxDistance; ++x) {
            for (int z = centerZ - maxDistance; z <= centerZ + maxDistance; ++z) {
                int distance = Math.abs(x - centerX) + Math.abs(z - centerZ);
                int adjustedDistance = Math.max(0, distance + distanceAdjustment);
                if (adjustedDistance >= maxDistance) continue;
                float probabilityOfNetherrack = netherrackProbabilityByDistance[adjustedDistance];
                if (!(random.nextDouble() < (double)probabilityOfNetherrack)) continue;
                int surfaceY = RuinedPortalPiece.getSurfaceY(level, x, z, this.verticalPlacement);
                int y = followGroundSurface ? surfaceY : Math.min(this.boundingBox.minY(), surfaceY);
                pos.set(x, y, z);
                if (Math.abs(y - this.boundingBox.minY()) > 3 || !this.canBlockBeReplacedByNetherrackOrMagma(level, pos)) continue;
                this.placeNetherrackOrMagma(random, level, pos);
                if (this.properties.overgrown) {
                    this.maybeAddLeavesAbove(random, level, pos);
                }
                this.addNetherrackDripColumn(random, level, (BlockPos)pos.below());
            }
        }
    }

    private boolean canBlockBeReplacedByNetherrackOrMagma(LevelAccessor level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.is(Blocks.AIR) && !state.is(Blocks.OBSIDIAN) && !state.is(BlockTags.FEATURES_CANNOT_REPLACE) && (this.verticalPlacement == VerticalPlacement.IN_NETHER || !state.is(Blocks.LAVA));
    }

    private void placeNetherrackOrMagma(RandomSource random, LevelAccessor level, BlockPos pos) {
        if (!this.properties.cold && random.nextFloat() < 0.07f) {
            level.setBlock(pos, Blocks.MAGMA_BLOCK.defaultBlockState(), 3);
        } else {
            level.setBlock(pos, Blocks.NETHERRACK.defaultBlockState(), 3);
        }
    }

    private static int getSurfaceY(LevelAccessor level, int x, int z, VerticalPlacement verticalPlacement) {
        return level.getHeight(RuinedPortalPiece.getHeightMapType(verticalPlacement), x, z) - 1;
    }

    public static Heightmap.Types getHeightMapType(VerticalPlacement verticalPlacement) {
        return verticalPlacement == VerticalPlacement.ON_OCEAN_FLOOR ? Heightmap.Types.OCEAN_FLOOR_WG : Heightmap.Types.WORLD_SURFACE_WG;
    }

    private static ProcessorRule getBlockReplaceRule(Block source, float probability, Block target) {
        return new ProcessorRule(new RandomBlockMatchTest(source, probability), AlwaysTrueTest.INSTANCE, target.defaultBlockState());
    }

    private static ProcessorRule getBlockReplaceRule(Block source, Block target) {
        return new ProcessorRule(new BlockMatchTest(source), AlwaysTrueTest.INSTANCE, target.defaultBlockState());
    }

    public static enum VerticalPlacement implements StringRepresentable
    {
        ON_LAND_SURFACE("on_land_surface"),
        PARTLY_BURIED("partly_buried"),
        ON_OCEAN_FLOOR("on_ocean_floor"),
        IN_MOUNTAIN("in_mountain"),
        UNDERGROUND("underground"),
        IN_NETHER("in_nether");

        public static final Codec<VerticalPlacement> CODEC;
        private final String name;

        private VerticalPlacement(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        static {
            CODEC = StringRepresentable.fromEnum(VerticalPlacement::values);
        }
    }

    public static class Properties {
        public static final Codec<Properties> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)Codec.BOOL.fieldOf("cold")).forGetter(p -> p.cold), ((MapCodec)Codec.FLOAT.fieldOf("mossiness")).forGetter(p -> Float.valueOf(p.mossiness)), ((MapCodec)Codec.BOOL.fieldOf("air_pocket")).forGetter(p -> p.airPocket), ((MapCodec)Codec.BOOL.fieldOf("overgrown")).forGetter(p -> p.overgrown), ((MapCodec)Codec.BOOL.fieldOf("vines")).forGetter(p -> p.vines), ((MapCodec)Codec.BOOL.fieldOf("replace_with_blackstone")).forGetter(p -> p.replaceWithBlackstone)).apply((Applicative<Properties, ?>)i, Properties::new));
        public boolean cold;
        public float mossiness;
        public boolean airPocket;
        public boolean overgrown;
        public boolean vines;
        public boolean replaceWithBlackstone;

        public Properties() {
        }

        public Properties(boolean cold, float mossiness, boolean airPocket, boolean overgrown, boolean vines, boolean replaceWithBlackstone) {
            this.cold = cold;
            this.mossiness = mossiness;
            this.airPocket = airPocket;
            this.overgrown = overgrown;
            this.vines = vines;
            this.replaceWithBlackstone = replaceWithBlackstone;
        }
    }
}

