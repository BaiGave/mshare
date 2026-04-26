/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.authlib.yggdrasil;

import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Set;

public class TextureUrlChecker {
    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");
    private static final Set<String> ALLOWED_DOMAINS = Set.of("textures.minecraft.net");

    public static boolean isAllowedTextureDomain(String url) {
        URI uri;
        try {
            uri = new URI(url).normalize();
        }
        catch (URISyntaxException ignored) {
            return false;
        }
        String scheme = uri.getScheme();
        if (scheme == null || !ALLOWED_SCHEMES.contains(scheme)) {
            return false;
        }
        String domain = uri.getHost();
        if (domain == null) {
            return false;
        }
        String decodedDomain = IDN.toUnicode(domain);
        String lowerCaseDomain = decodedDomain.toLowerCase(Locale.ROOT);
        if (!lowerCaseDomain.equals(decodedDomain)) {
            return false;
        }
        return ALLOWED_DOMAINS.contains(decodedDomain);
    }
}

