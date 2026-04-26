/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.json.ReadValueCallback;
import com.microsoft.aad.msal4j.ClaimsRequest;
import com.microsoft.aad.msal4j.IdToken;
import com.microsoft.aad.msal4j.MsalClientException;
import com.microsoft.aad.msal4j.MsalJsonParsingException;
import com.microsoft.aad.msal4j.RequestedClaimAdditionalInfo;
import com.microsoft.aad.msal4j.StringHelper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class JsonHelper {
    private static final Logger LOG = LoggerFactory.getLogger(JsonHelper.class);

    private JsonHelper() {
    }

    static IdToken createIdTokenFromEncodedTokenString(String token) {
        return JsonHelper.convertJsonStringToJsonSerializableObject(JsonHelper.getTokenPayloadClaims(token), IdToken::fromJson);
    }

    static String getTokenPayloadClaims(String token) {
        try {
            return new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]), StandardCharsets.UTF_8);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            LOG.error("Error parsing ID token, missing payload section.");
            throw new MsalClientException("Error parsing ID token, missing payload section.", "invalid_jwt");
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    static Map<String, Object> parseJsonToMap(String jsonString) {
        if (StringHelper.isBlank(jsonString)) {
            return new HashMap<String, Object>();
        }
        try (JsonReader jsonReader = JsonProviders.createReader(jsonString);){
            jsonReader.nextToken();
            Map<String, Object> map = JsonHelper.parseJsonObject(jsonReader);
            return map;
        }
        catch (IOException e) {
            LOG.error("JSON parsing error when attempting to convert JSON into a Map.");
            throw new MsalJsonParsingException(e.getMessage(), "invalid_json");
        }
    }

    private static Map<String, Object> parseJsonObject(JsonReader jsonReader) throws IOException {
        HashMap<String, Object> object = new HashMap<String, Object>();
        while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jsonReader.getFieldName();
            Object value = JsonHelper.parseValue(jsonReader);
            object.put(fieldName, JsonHelper.handleSpecialFields(fieldName, value));
        }
        return object;
    }

    private static Object handleSpecialFields(String fieldName, Object value) {
        if ("aud".equals(fieldName) && value instanceof String) {
            ArrayList<String> list = new ArrayList<String>();
            list.add((String)value);
            return list;
        }
        if (JsonHelper.isTimestampField(fieldName) && value instanceof Number) {
            return new Date(((Number)value).longValue() * 1000L);
        }
        return value;
    }

    private static boolean isTimestampField(String fieldName) {
        return "exp".equals(fieldName) || "iat".equals(fieldName) || "nbf".equals(fieldName);
    }

    private static Object parseValue(JsonReader jsonReader) throws IOException {
        JsonToken token = jsonReader.currentToken();
        switch (token) {
            case STRING: {
                return jsonReader.getString();
            }
            case NUMBER: {
                try {
                    return jsonReader.getLong();
                }
                catch (ArithmeticException e) {
                    return jsonReader.getDouble();
                }
            }
            case BOOLEAN: {
                return jsonReader.getBoolean();
            }
            case NULL: {
                return null;
            }
            case START_ARRAY: {
                return jsonReader.readArray(JsonReader::readUntyped);
            }
            case START_OBJECT: {
                return JsonHelper.parseJsonObject(jsonReader);
            }
        }
        jsonReader.skipChildren();
        return null;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    static <T extends JsonSerializable<T>> T convertJsonStringToJsonSerializableObject(String jsonResponse, ReadValueCallback<JsonReader, T> readFunction) {
        try (JsonReader jsonReader = JsonProviders.createReader(jsonResponse);){
            JsonSerializable jsonSerializable = (JsonSerializable)((Object)readFunction.read(jsonReader));
            return (T)jsonSerializable;
        }
        catch (Exception e) {
            throw new MsalJsonParsingException(e.getMessage(), "invalid_json");
        }
    }

    static <T extends JsonSerializable<T>> String convertJsonSerializableObjectToString(T jsonSerializable) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JsonWriter jsonWriter = JsonProviders.createWriter(outputStream);
            jsonSerializable.toJson(jsonWriter);
            jsonWriter.flush();
            return outputStream.toString(StandardCharsets.UTF_8.name());
        }
        catch (Exception e) {
            throw new MsalClientException("Error serializing object to JSON: " + e.getMessage(), "invalid_json");
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    static Map<String, String> convertJsonToMap(String jsonString) {
        try (JsonReader reader = JsonProviders.createReader(jsonString);){
            reader.nextToken();
            Map<String, String> map = reader.readMap(JsonReader::getString);
            return map;
        }
        catch (IOException e) {
            throw new MsalClientException("Could not parse JSON from HttpResponse body: " + e.getMessage(), "invalid_json");
        }
    }

    static void validateJsonFormat(String jsonString) {
        try (JsonReader reader = JsonProviders.createReader(jsonString);){
            while (reader.nextToken() != JsonToken.END_DOCUMENT) {
                reader.skipChildren();
            }
        }
        catch (IOException e) {
            throw new MsalClientException(e.getMessage(), "invalid_json");
        }
    }

    public static String formCapabilitiesJson(Set<String> clientCapabilities) {
        if (clientCapabilities == null || clientCapabilities.isEmpty()) {
            return null;
        }
        ClaimsRequest cr = new ClaimsRequest();
        RequestedClaimAdditionalInfo capabilitiesValues = new RequestedClaimAdditionalInfo(false, null, new ArrayList<String>(clientCapabilities));
        cr.requestClaimInAccessToken("xms_cc", capabilitiesValues);
        return cr.formatAsJSONString();
    }

    static String mergeJSONString(String mainJsonString, String addJsonString) {
        try {
            Map<String, Object> mainMap = JsonHelper.parseJsonToMap(mainJsonString);
            Map<String, Object> addMap = JsonHelper.parseJsonToMap(addJsonString);
            JsonHelper.mergeJsonMaps(mainMap, addMap);
            return JsonHelper.writeJsonMap(mainMap);
        }
        catch (IOException e) {
            throw new MsalClientException(e.getMessage(), "invalid_json");
        }
    }

    private static void mergeJsonMaps(Map<String, Object> mainMap, Map<String, Object> addMap) {
        if (addMap == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : addMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (mainMap.containsKey(key) && mainMap.get(key) instanceof Map && value instanceof Map) {
                JsonHelper.mergeJsonMaps((Map)mainMap.get(key), (Map)value);
                continue;
            }
            mainMap.put(key, value);
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    static String writeJsonMap(Map<String, Object> map) throws IOException {
        StringWriter stringWriter = new StringWriter();
        try (JsonWriter jsonWriter = JsonProviders.createWriter(stringWriter);){
            jsonWriter.writeStartObject();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                jsonWriter.writeUntypedField(entry.getKey(), entry.getValue());
            }
            jsonWriter.writeEndObject();
            jsonWriter.flush();
            String string = stringWriter.toString();
            return string;
        }
        catch (Exception e) {
            throw new MsalClientException("Error writing JSON map to string: " + e.getMessage(), "invalid_json");
        }
    }
}

