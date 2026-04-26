/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.player;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.PlayerModelType;
import org.jspecify.annotations.Nullable;

public record PlayerSkin(ClientAsset.Texture body,  @Nullable ClientAsset.Texture cape,  @Nullable ClientAsset.Texture elytra, PlayerModelType model, boolean secure) {
    public static PlayerSkin insecure(ClientAsset.Texture body,  @Nullable ClientAsset.Texture cape,  @Nullable ClientAsset.Texture elytra, PlayerModelType model) {
        return new PlayerSkin(body, cape, elytra, model, false);
    }

    public PlayerSkin with(Patch patch) {
        if (patch.equals(Patch.EMPTY)) {
            return this;
        }
        return PlayerSkin.insecure(DataFixUtils.orElse(patch.body, this.body), DataFixUtils.orElse(patch.cape, this.cape), DataFixUtils.orElse(patch.elytra, this.elytra), patch.model.orElse(this.model));
    }

    public record Patch(Optional<ClientAsset.ResourceTexture> body, Optional<ClientAsset.ResourceTexture> cape, Optional<ClientAsset.ResourceTexture> elytra, Optional<PlayerModelType> model) {
        public static final Patch EMPTY = new Patch(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        public static final MapCodec<Patch> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(ClientAsset.ResourceTexture.CODEC.optionalFieldOf("texture").forGetter(Patch::body), ClientAsset.ResourceTexture.CODEC.optionalFieldOf("cape").forGetter(Patch::cape), ClientAsset.ResourceTexture.CODEC.optionalFieldOf("elytra").forGetter(Patch::elytra), PlayerModelType.CODEC.optionalFieldOf("model").forGetter(Patch::model)).apply((Applicative<Patch, ?>)i, Patch::create));
        public static final StreamCodec<ByteBuf, Patch> STREAM_CODEC = StreamCodec.composite(ClientAsset.ResourceTexture.STREAM_CODEC.apply(ByteBufCodecs::optional), Patch::body, ClientAsset.ResourceTexture.STREAM_CODEC.apply(ByteBufCodecs::optional), Patch::cape, ClientAsset.ResourceTexture.STREAM_CODEC.apply(ByteBufCodecs::optional), Patch::elytra, PlayerModelType.STREAM_CODEC.apply(ByteBufCodecs::optional), Patch::model, Patch::create);

        public static Patch create(Optional<ClientAsset.ResourceTexture> texture, Optional<ClientAsset.ResourceTexture> capeTexture, Optional<ClientAsset.ResourceTexture> elytraTexture, Optional<PlayerModelType> model) {
            if (texture.isEmpty() && capeTexture.isEmpty() && elytraTexture.isEmpty() && model.isEmpty()) {
                return EMPTY;
            }
            return new Patch(texture, capeTexture, elytraTexture, model);
        }
    }
}

