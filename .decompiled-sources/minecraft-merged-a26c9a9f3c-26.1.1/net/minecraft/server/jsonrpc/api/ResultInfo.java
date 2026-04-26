/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.jsonrpc.api;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.jsonrpc.api.Schema;

public record ResultInfo<Result>(String name, Schema<Result> schema) {
    public static <Result> Codec<ResultInfo<Result>> typedCodec() {
        return RecordCodecBuilder.create(i -> i.group(((MapCodec)Codec.STRING.fieldOf("name")).forGetter(ResultInfo::name), ((MapCodec)Schema.typedCodec().fieldOf("schema")).forGetter(ResultInfo::schema)).apply((Applicative<ResultInfo, ?>)i, ResultInfo::new));
    }
}

