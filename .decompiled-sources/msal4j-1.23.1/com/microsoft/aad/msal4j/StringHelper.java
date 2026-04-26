/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.MsalClientException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

final class StringHelper {
    static String EMPTY_STRING = "";

    StringHelper() {
    }

    static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    static String createBase64EncodedSha256Hash(String stringToHash) {
        return StringHelper.createSha256Hash(stringToHash, true);
    }

    static String createSha256Hash(String stringToHash) {
        return StringHelper.createSha256Hash(stringToHash, false);
    }

    private static String createSha256Hash(String stringToHash, boolean base64Encode) {
        String res;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = messageDigest.digest(stringToHash.getBytes(StandardCharsets.UTF_8));
            res = base64Encode ? Base64.getUrlEncoder().withoutPadding().encodeToString(hash) : new String(hash, StandardCharsets.UTF_8);
        }
        catch (NoSuchAlgorithmException e) {
            res = null;
        }
        return res;
    }

    static String createSha256HashHexString(String stringToHash) {
        if (stringToHash == null || stringToHash.isEmpty()) {
            throw new IllegalArgumentException("String to hash cannot be null or empty");
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = messageDigest.digest(stringToHash.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new MsalClientException("Failed to create SHA-256 hash: " + e.getMessage(), "crypto_error");
        }
    }

    static boolean isNullOrBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    static String serializeQueryParameters(Map<String, String> params) {
        if (params != null && !params.isEmpty()) {
            Map<String, String> encodedParams = StringHelper.urlEncodeMap(params);
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : encodedParams.entrySet()) {
                if (entry.getKey() == null || entry.getValue() == null) continue;
                String value = entry.getValue();
                if (sb.length() > 0) {
                    sb.append('&');
                }
                sb.append(entry.getKey());
                sb.append('=');
                sb.append(value);
            }
            return sb.toString();
        }
        return "";
    }

    static Map<String, String> urlEncodeMap(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return params;
        }
        LinkedHashMap<String, String> out = new LinkedHashMap<String, String>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            try {
                String newKey = entry.getKey() != null ? URLEncoder.encode(entry.getKey(), "utf-8") : null;
                String newValue = entry.getValue() != null ? URLEncoder.encode(entry.getValue(), "utf-8") : null;
                out.put(newKey, newValue);
            }
            catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return out;
    }

    static Map<String, String> parseQueryParameters(String query) {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        if (StringHelper.isBlank(query)) {
            return params;
        }
        StringTokenizer st = new StringTokenizer(query.trim(), "&");
        while (st.hasMoreTokens()) {
            String value;
            String key;
            String param = st.nextToken();
            String[] pair = param.split("=", 2);
            try {
                key = URLDecoder.decode(pair[0], "utf-8");
                value = pair.length > 1 ? URLDecoder.decode(pair[1], "utf-8") : "";
            }
            catch (UnsupportedEncodingException | IllegalArgumentException e) {
                continue;
            }
            params.put(key, value);
        }
        return params;
    }

    static Map<String, List<String>> convertToMultiValueMap(Map<String, String> singleValueMap) {
        HashMap<String, List<String>> multiValueMap = new HashMap<String, List<String>>();
        if (singleValueMap != null) {
            for (Map.Entry<String, String> entry : singleValueMap.entrySet()) {
                multiValueMap.put(entry.getKey(), Collections.singletonList(entry.getValue()));
            }
        }
        return multiValueMap;
    }
}

