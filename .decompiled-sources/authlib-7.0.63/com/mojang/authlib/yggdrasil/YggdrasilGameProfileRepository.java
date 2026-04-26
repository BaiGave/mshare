/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.authlib.yggdrasil;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.mojang.authlib.Environment;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.exceptions.MinecraftClientException;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.authlib.yggdrasil.ProfileNotFoundException;
import com.mojang.authlib.yggdrasil.response.NameAndId;
import com.mojang.authlib.yggdrasil.response.ProfileSearchResultsResponse;
import java.net.Proxy;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YggdrasilGameProfileRepository
implements GameProfileRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(YggdrasilGameProfileRepository.class);
    private static final int ENTRIES_PER_PAGE = 2;
    private static final int MAX_FAIL_COUNT = 3;
    private static final int DELAY_BETWEEN_PAGES = 100;
    private static final int DELAY_BETWEEN_FAILURES = 750;
    private final MinecraftClient client;
    private final URL searchPageUrl;
    private final String nameLookupUrl;

    public YggdrasilGameProfileRepository(Proxy proxy, Environment environment) {
        this.client = MinecraftClient.unauthenticated(proxy);
        this.searchPageUrl = HttpAuthenticationService.constantURL(environment.profilesHost() + "/minecraft/profile/lookup/bulk/byname");
        this.nameLookupUrl = environment.profilesHost() + "/minecraft/profile/lookup/name/";
    }

    @Override
    public void findProfilesByNames(String[] names, ProfileLookupCallback callback) {
        Set criteria = Arrays.stream(names).filter(name -> !Strings.isNullOrEmpty(name)).collect(Collectors.toSet());
        boolean page = false;
        for (List request : Iterables.partition(criteria, 2)) {
            boolean failed;
            List<String> normalizedRequest = request.stream().map(YggdrasilGameProfileRepository::normalizeName).toList();
            int failCount = 0;
            do {
                failed = false;
                try {
                    ProfileSearchResultsResponse response = this.client.post(this.searchPageUrl, normalizedRequest, ProfileSearchResultsResponse.class);
                    List<Object> results = response != null ? response.profiles() : List.of();
                    failCount = 0;
                    LOGGER.debug("Page {} returned {} results, parsing", (Object)0, (Object)results.size());
                    HashSet<String> received = new HashSet<String>(results.size());
                    for (NameAndId nameAndId : results) {
                        LOGGER.debug("Successfully looked up profile {}", (Object)nameAndId);
                        received.add(YggdrasilGameProfileRepository.normalizeName(nameAndId.name()));
                        callback.onProfileLookupSucceeded(nameAndId.name(), nameAndId.id());
                    }
                    for (String string : request) {
                        if (received.contains(YggdrasilGameProfileRepository.normalizeName(string))) continue;
                        LOGGER.debug("Couldn't find profile {}", (Object)string);
                        callback.onProfileLookupFailed(string, new ProfileNotFoundException("Server did not find the requested profile"));
                    }
                    try {
                        Thread.sleep(100L);
                    }
                    catch (InterruptedException interruptedException) {}
                }
                catch (MinecraftClientException e) {
                    if (++failCount == 3) {
                        for (String name3 : request) {
                            LOGGER.debug("Couldn't find profile {} because of a server error", (Object)name3);
                            callback.onProfileLookupFailed(name3, e.toAuthenticationException());
                        }
                        continue;
                    }
                    try {
                        Thread.sleep(750L);
                    }
                    catch (InterruptedException interruptedException) {
                        // empty catch block
                    }
                    failed = true;
                }
            } while (failed);
        }
    }

    @Override
    public Optional<NameAndId> findProfileByName(String name) {
        try {
            return Optional.ofNullable(this.client.get(HttpAuthenticationService.constantURL(this.nameLookupUrl + YggdrasilGameProfileRepository.normalizeName(name)), NameAndId.class));
        }
        catch (MinecraftClientException e) {
            LOGGER.warn("Couldn't find profile with name: {}", (Object)name, (Object)e);
            return Optional.empty();
        }
    }

    private static String normalizeName(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}

