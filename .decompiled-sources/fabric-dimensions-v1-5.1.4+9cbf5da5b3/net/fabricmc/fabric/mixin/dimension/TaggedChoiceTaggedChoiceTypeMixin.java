/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.dimension;

import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.fabricmc.fabric.impl.dimension.TaggedChoiceTypeExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={TaggedChoice.TaggedChoiceType.class})
public class TaggedChoiceTaggedChoiceTypeMixin<K>
implements TaggedChoiceTypeExtension {
    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("TaggedChoiceType_DimDataFix");
    @Shadow
    @Final
    protected Object2ObjectMap<K, Type<?>> types;
    @Unique
    private boolean failSoft;

    @Inject(method={"getMapCodec"}, at={@At(value="HEAD")}, cancellable=true)
    private void onGetCodec(K k, CallbackInfoReturnable<DataResult<? extends MapCodec<?>>> cir) {
        if (this.failSoft && !this.types.containsKey(k)) {
            LOGGER.warn("Not recognizing key {}. Using pass-through codec. {}", (Object)k, (Object)this);
            cir.setReturnValue(DataResult.success(MapCodec.assumeMapUnsafe(Codec.PASSTHROUGH)));
        }
    }

    @Override
    public void fabric$setFailSoft(boolean cond) {
        this.failSoft = cond;
    }
}

