/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.authlib.yggdrasil;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.mojang.authlib.Environment;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.SignatureState;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.MinecraftClientException;
import com.mojang.authlib.minecraft.InsecurePublicKeyException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.ProfileActionType;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.authlib.yggdrasil.ServicesKeySet;
import com.mojang.authlib.yggdrasil.ServicesKeyType;
import com.mojang.authlib.yggdrasil.TextureUrlChecker;
import com.mojang.authlib.yggdrasil.request.JoinMinecraftServerRequest;
import com.mojang.authlib.yggdrasil.response.HasJoinedMinecraftServerResponse;
import com.mojang.authlib.yggdrasil.response.MinecraftProfilePropertiesResponse;
import com.mojang.authlib.yggdrasil.response.MinecraftTexturesPayload;
import com.mojang.authlib.yggdrasil.response.ProfileAction;
import com.mojang.util.UUIDTypeAdapter;
import com.mojang.util.UndashedUuid;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YggdrasilMinecraftSessionService
implements MinecraftSessionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(YggdrasilMinecraftSessionService.class);
    private final MinecraftClient client;
    private final ServicesKeySet servicesKeySet;
    private final String baseUrl;
    private final URL joinUrl;
    private final URL checkUrl;
    private final Gson gson = new GsonBuilder().registerTypeAdapter((Type)((Object)UUID.class), new UUIDTypeAdapter()).create();
    private final LoadingCache<UUID, Optional<ProfileResult>> insecureProfiles = CacheBuilder.newBuilder().expireAfterWrite(6L, TimeUnit.HOURS).build(new CacheLoader<UUID, Optional<ProfileResult>>(){

        @Override
        public Optional<ProfileResult> load(UUID key) {
            return Optional.ofNullable(YggdrasilMinecraftSessionService.this.fetchProfileUncached(key, false));
        }
    });

    protected YggdrasilMinecraftSessionService(ServicesKeySet servicesKeySet, Proxy proxy, Environment env) {
        this.client = MinecraftClient.unauthenticated(proxy);
        this.servicesKeySet = servicesKeySet;
        this.baseUrl = env.sessionHost() + "/session/minecraft/";
        this.joinUrl = HttpAuthenticationService.constantURL(this.baseUrl + "join");
        this.checkUrl = HttpAuthenticationService.constantURL(this.baseUrl + "hasJoined");
    }

    @Override
    public void joinServer(UUID profileId, String authenticationToken, String serverId) throws AuthenticationException {
        JoinMinecraftServerRequest request = new JoinMinecraftServerRequest(authenticationToken, profileId, serverId);
        try {
            this.client.post(this.joinUrl, request, Void.class);
        }
        catch (MinecraftClientException e) {
            throw e.toAuthenticationException();
        }
    }

    @Override
    @Nullable
    public ProfileResult hasJoinedServer(String profileName, String serverId, @Nullable InetAddress address) throws AuthenticationUnavailableException {
        HashMap<String, Object> arguments = new HashMap<String, Object>();
        arguments.put("username", profileName);
        arguments.put("serverId", serverId);
        if (address != null) {
            arguments.put("ip", address.getHostAddress());
        }
        URL url = HttpAuthenticationService.concatenateURL(this.checkUrl, HttpAuthenticationService.buildQuery(arguments));
        try {
            HasJoinedMinecraftServerResponse response = this.client.get(url, HasJoinedMinecraftServerResponse.class);
            if (response != null && response.id() != null) {
                GameProfile result = new GameProfile(response.id(), profileName, Objects.requireNonNullElse(response.properties(), PropertyMap.EMPTY));
                Set<ProfileActionType> profileActions = YggdrasilMinecraftSessionService.extractProfileActionTypes(response.profileActions());
                return new ProfileResult(result, profileActions);
            }
            return null;
        }
        catch (MinecraftClientException e) {
            AuthenticationException authenticationException = e.toAuthenticationException();
            if (authenticationException instanceof AuthenticationUnavailableException) {
                AuthenticationUnavailableException unavailable = (AuthenticationUnavailableException)authenticationException;
                throw unavailable;
            }
            return null;
        }
    }

    @Override
    @Nullable
    public Property getPackedTextures(GameProfile profile) {
        return Iterables.getFirst(profile.properties().get("textures"), null);
    }

    @Override
    public MinecraftProfileTextures unpackTextures(Property packedTextures) {
        MinecraftTexturesPayload result;
        String value = packedTextures.value();
        SignatureState signatureState = this.getPropertySignatureState(packedTextures);
        try {
            String json = new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
            result = this.gson.fromJson(json, MinecraftTexturesPayload.class);
        }
        catch (JsonParseException | IllegalArgumentException e) {
            LOGGER.error("Could not decode textures payload", e);
            return MinecraftProfileTextures.EMPTY;
        }
        if (result == null || result.textures() == null || result.textures().isEmpty()) {
            return MinecraftProfileTextures.EMPTY;
        }
        Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures = result.textures();
        for (Map.Entry<MinecraftProfileTexture.Type, MinecraftProfileTexture> entry : textures.entrySet()) {
            String url = entry.getValue().getUrl();
            if (url != null && TextureUrlChecker.isAllowedTextureDomain(url)) continue;
            LOGGER.error("Textures payload url is invalid: {}", (Object)url);
            return MinecraftProfileTextures.EMPTY;
        }
        return new MinecraftProfileTextures(textures.get((Object)MinecraftProfileTexture.Type.SKIN), textures.get((Object)MinecraftProfileTexture.Type.CAPE), textures.get((Object)MinecraftProfileTexture.Type.ELYTRA), signatureState);
    }

    @Override
    @Nullable
    public ProfileResult fetchProfile(UUID profileId, boolean requireSecure) {
        if (!requireSecure) {
            return this.insecureProfiles.getUnchecked(profileId).orElse(null);
        }
        return this.fetchProfileUncached(profileId, true);
    }

    @Override
    public String getSecurePropertyValue(Property property) throws InsecurePublicKeyException {
        switch (this.getPropertySignatureState(property)) {
            default: {
                throw new IncompatibleClassChangeError();
            }
            case UNSIGNED: {
                throw new InsecurePublicKeyException.MissingException("Missing signature from \"" + property.name() + "\"");
            }
            case INVALID: {
                throw new InsecurePublicKeyException.InvalidException("Property \"" + property.name() + "\" has been tampered with (signature invalid)");
            }
            case SIGNED: 
        }
        return property.value();
    }

    private SignatureState getPropertySignatureState(Property property) {
        if (!property.hasSignature()) {
            return SignatureState.UNSIGNED;
        }
        if (this.servicesKeySet.keys(ServicesKeyType.PROFILE_PROPERTY).stream().noneMatch(key -> key.validateProperty(property))) {
            return SignatureState.INVALID;
        }
        return SignatureState.SIGNED;
    }

    @Nullable
    private ProfileResult fetchProfileUncached(UUID profileId, boolean requireSecure) {
        try {
            URL url = HttpAuthenticationService.constantURL(this.baseUrl + "profile/" + UndashedUuid.toString(profileId));
            url = HttpAuthenticationService.concatenateURL(url, "unsigned=" + !requireSecure);
            MinecraftProfilePropertiesResponse response = this.client.get(url, MinecraftProfilePropertiesResponse.class);
            if (response == null) {
                LOGGER.debug("Couldn't fetch profile properties for {} as the profile does not exist", (Object)profileId);
                return null;
            }
            GameProfile profile = response.profile();
            Set<ProfileActionType> profileActions = YggdrasilMinecraftSessionService.extractProfileActionTypes(response.profileActions());
            LOGGER.debug("Successfully fetched profile properties for {}", (Object)profile);
            return new ProfileResult(profile, profileActions);
        }
        catch (MinecraftClientException | IllegalArgumentException e) {
            LOGGER.warn("Couldn't look up profile properties for {}", (Object)profileId, (Object)e);
            return null;
        }
    }

    private static Set<ProfileActionType> extractProfileActionTypes(Set<ProfileAction> response) {
        return response.stream().map(ProfileAction::type).collect(Collectors.toSet());
    }
}

