/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.jsonrpc.api;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.jsonrpc.api.Schema;

public record ParamInfo<Param>(String name, Schema<Param> schema, boolean required) {
    public ParamInfo(String name, Schema<Param> schema) {
        this(name, schema, true);
    }

    public static <Param> MapCodec<ParamInfo<Param>> typedCodec() {
        return RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)Codec.STRING.fieldOf("name")).forGetter(ParamInfo::name), ((MapCodec)Schema.typedCodec().fieldOf("schema")).forGetter(ParamInfo::schema), ((MapCodec)Codec.BOOL.fieldOf("required")).forGetter(ParamInfo::required)).apply((Applicative<ParamInfo, ?>)i, ParamInfo::new));
    }
}

