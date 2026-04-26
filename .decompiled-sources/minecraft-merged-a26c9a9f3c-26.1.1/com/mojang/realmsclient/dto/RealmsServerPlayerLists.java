/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.realmsclient.dto;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.LenientJsonParser;
import net.minecraft.world.item.component.ResolvableProfile;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public record RealmsServerPlayerLists(Map<Long, List<ResolvableProfile>> servers) {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static RealmsServerPlayerLists parse(String json) {
        ImmutableMap.Builder elements = ImmutableMap.builder();
        try {
            JsonObject object = GsonHelper.parse(json);
            if (GsonHelper.isArrayNode(object, "lists")) {
                JsonArray jsonArray = object.getAsJsonArray("lists");
                for (JsonElement jsonElement : jsonArray) {
                    JsonElement element;
                    JsonObject node = jsonElement.getAsJsonObject();
                    String playerListString = JsonUtils.getStringOr("playerList", node, null);
                    List<Object> players = playerListString != null ? ((element = LenientJsonParser.parse(playerListString)).isJsonArray() ? RealmsServerPlayerLists.parsePlayers(element.getAsJsonArray()) : Lists.newArrayList()) : Lists.newArrayList();
                    elements.put(JsonUtils.getLongOr("serverId", node, -1L), players);
                }
            }
        }
        catch (Exception e) {
            LOGGER.error("Could not parse RealmsServerPlayerLists", e);
        }
        return new RealmsServerPlayerLists(elements.build());
    }

    private static List<ResolvableProfile> parsePlayers(JsonArray array) {
        ArrayList<ResolvableProfile> profiles = new ArrayList<ResolvableProfile>(array.size());
        for (JsonElement element : array) {
            UUID playerId;
            if (!element.isJsonObject() || (playerId = JsonUtils.getUuidOr("playerId", element.getAsJsonObject(), null)) == null || Minecraft.getInstance().isLocalPlayer(playerId)) continue;
            profiles.add(ResolvableProfile.createUnresolved(playerId));
        }
        return profiles;
    }

    public List<ResolvableProfile> getProfileResultsFor(long serverId) {
        List<ResolvableProfile> profileResults = this.servers.get(serverId);
        if (profileResults != null) {
            return profileResults;
        }
        return List.of();
    }
}

