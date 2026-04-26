/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.realmsclient.dto;

import com.google.gson.annotations.SerializedName;
import com.mojang.realmsclient.dto.ReflectionBasedSerialization;
import com.mojang.realmsclient.dto.RegionDataDto;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public record PreferredRegionsDto(@SerializedName(value="regionDataList") List<RegionDataDto> regionData) implements ReflectionBasedSerialization
{
    public static PreferredRegionsDto empty() {
        return new PreferredRegionsDto(List.of());
    }
}

