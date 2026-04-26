/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.microsoft.aad.msal4j.JsonHelper;
import com.microsoft.aad.msal4j.StringHelper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

class ClientInfo
implements JsonSerializable<ClientInfo> {
    private String uniqueIdentifier;
    private String uniqueTenantIdentifier;

    ClientInfo() {
    }

    public static ClientInfo createFromJson(String clientInfoJsonBase64Encoded) {
        if (StringHelper.isBlank(clientInfoJsonBase64Encoded)) {
            return null;
        }
        byte[] decodedInput = Base64.getUrlDecoder().decode(clientInfoJsonBase64Encoded.getBytes(StandardCharsets.UTF_8));
        return JsonHelper.convertJsonStringToJsonSerializableObject(new String(decodedInput, StandardCharsets.UTF_8), ClientInfo::fromJson);
    }

    static ClientInfo fromJson(JsonReader jsonReader) throws IOException {
        ClientInfo clientInfo = new ClientInfo();
        return jsonReader.readObject(reader -> {
            block8: while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                switch (fieldName) {
                    case "uid": {
                        clientInfo.uniqueIdentifier = reader.getString();
                        continue block8;
                    }
                    case "utid": {
                        clientInfo.uniqueTenantIdentifier = reader.getString();
                        continue block8;
                    }
                }
                reader.skipChildren();
            }
            return clientInfo;
        });
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("uid", this.uniqueIdentifier);
        jsonWriter.writeStringField("utid", this.uniqueTenantIdentifier);
        jsonWriter.writeEndObject();
        return jsonWriter;
    }

    String toAccountIdentifier() {
        return this.uniqueIdentifier + "." + this.uniqueTenantIdentifier;
    }

    String getUniqueIdentifier() {
        return this.uniqueIdentifier;
    }

    String getUniqueTenantIdentifier() {
        return this.uniqueTenantIdentifier;
    }
}

