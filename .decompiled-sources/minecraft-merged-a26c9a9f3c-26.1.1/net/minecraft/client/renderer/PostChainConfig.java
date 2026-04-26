/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.lang.runtime.SwitchBootstraps;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.UniformValue;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;

@Environment(value=EnvType.CLIENT)
public record PostChainConfig(Map<Identifier, InternalTarget> internalTargets, List<Pass> passes) {
    public static final Codec<PostChainConfig> CODEC = RecordCodecBuilder.create(i -> i.group(Codec.unboundedMap(Identifier.CODEC, InternalTarget.CODEC).optionalFieldOf("targets", Map.of()).forGetter(PostChainConfig::internalTargets), Pass.CODEC.listOf().optionalFieldOf("passes", List.of()).forGetter(PostChainConfig::passes)).apply((Applicative<PostChainConfig, ?>)i, PostChainConfig::new));

    @Environment(value=EnvType.CLIENT)
    public record InternalTarget(Optional<Integer> width, Optional<Integer> height, boolean persistent, int clearColor) {
        public static final Codec<InternalTarget> CODEC = RecordCodecBuilder.create(i -> i.group(ExtraCodecs.POSITIVE_INT.optionalFieldOf("width").forGetter(InternalTarget::width), ExtraCodecs.POSITIVE_INT.optionalFieldOf("height").forGetter(InternalTarget::height), Codec.BOOL.optionalFieldOf("persistent", false).forGetter(InternalTarget::persistent), ExtraCodecs.ARGB_COLOR_CODEC.optionalFieldOf("clear_color", 0).forGetter(InternalTarget::clearColor)).apply((Applicative<InternalTarget, ?>)i, InternalTarget::new));
    }

    @Environment(value=EnvType.CLIENT)
    public record Pass(Identifier vertexShaderId, Identifier fragmentShaderId, List<Input> inputs, Identifier outputTarget, Map<String, List<UniformValue>> uniforms) {
        private static final Codec<List<Input>> INPUTS_CODEC = Input.CODEC.listOf().validate(inputs -> {
            ObjectArraySet samplerName = new ObjectArraySet(inputs.size());
            for (Input input : inputs) {
                if (samplerName.add(input.samplerName())) continue;
                return DataResult.error(() -> "Encountered repeated sampler name: " + input.samplerName());
            }
            return DataResult.success(inputs);
        });
        private static final Codec<Map<String, List<UniformValue>>> UNIFORM_BLOCKS_CODEC = Codec.unboundedMap(Codec.STRING, UniformValue.CODEC.listOf());
        public static final Codec<Pass> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)Identifier.CODEC.fieldOf("vertex_shader")).forGetter(Pass::vertexShaderId), ((MapCodec)Identifier.CODEC.fieldOf("fragment_shader")).forGetter(Pass::fragmentShaderId), INPUTS_CODEC.optionalFieldOf("inputs", List.of()).forGetter(Pass::inputs), ((MapCodec)Identifier.CODEC.fieldOf("output")).forGetter(Pass::outputTarget), UNIFORM_BLOCKS_CODEC.optionalFieldOf("uniforms", Map.of()).forGetter(Pass::uniforms)).apply((Applicative<Pass, ?>)i, Pass::new));

        public Stream<Identifier> referencedTargets() {
            Stream inputTargets = this.inputs.stream().flatMap(input -> input.referencedTargets().stream());
            return Stream.concat(inputTargets, Stream.of(this.outputTarget));
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record TargetInput(String samplerName, Identifier targetId, boolean useDepthBuffer, boolean bilinear) implements Input
    {
        public static final Codec<TargetInput> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)Codec.STRING.fieldOf("sampler_name")).forGetter(TargetInput::samplerName), ((MapCodec)Identifier.CODEC.fieldOf("target")).forGetter(TargetInput::targetId), Codec.BOOL.optionalFieldOf("use_depth_buffer", false).forGetter(TargetInput::useDepthBuffer), Codec.BOOL.optionalFieldOf("bilinear", false).forGetter(TargetInput::bilinear)).apply((Applicative<TargetInput, ?>)i, TargetInput::new));

        @Override
        public Set<Identifier> referencedTargets() {
            return Set.of(this.targetId);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record TextureInput(String samplerName, Identifier location, int width, int height, boolean bilinear) implements Input
    {
        public static final Codec<TextureInput> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)Codec.STRING.fieldOf("sampler_name")).forGetter(TextureInput::samplerName), ((MapCodec)Identifier.CODEC.fieldOf("location")).forGetter(TextureInput::location), ((MapCodec)ExtraCodecs.POSITIVE_INT.fieldOf("width")).forGetter(TextureInput::width), ((MapCodec)ExtraCodecs.POSITIVE_INT.fieldOf("height")).forGetter(TextureInput::height), Codec.BOOL.optionalFieldOf("bilinear", false).forGetter(TextureInput::bilinear)).apply((Applicative<TextureInput, ?>)i, TextureInput::new));

        @Override
        public Set<Identifier> referencedTargets() {
            return Set.of();
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static sealed interface Input
    permits TextureInput, TargetInput {
        public static final Codec<Input> CODEC = Codec.xor(TextureInput.CODEC, TargetInput.CODEC).xmap(either -> (Input)either.map(Function.identity(), Function.identity()), input -> {
            Input input2 = input;
            Objects.requireNonNull(input2);
            Input selector0$temp = input2;
            int index$1 = 0;
            return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{TextureInput.class, TargetInput.class}, (Input)selector0$temp, index$1)) {
                default -> throw new MatchException(null, null);
                case 0 -> {
                    TextureInput texture = (TextureInput)selector0$temp;
                    yield Either.left(texture);
                }
                case 1 -> {
                    TargetInput target = (TargetInput)selector0$temp;
                    yield Either.right(target);
                }
            };
        });

        public String samplerName();

        public Set<Identifier> referencedTargets();
    }
}

