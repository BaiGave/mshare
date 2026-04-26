/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

class OidcDiscoveryResponse
implements JsonSerializable<OidcDiscoveryResponse> {
    private String authorizationEndpoint;
    private String tokenEndpoint;
    private String deviceCodeEndpoint;
    private String issuer;

    OidcDiscoveryResponse() {
    }

    public static OidcDiscoveryResponse fromJson(JsonReader jsonReader) throws IOException {
        OidcDiscoveryResponse response = new OidcDiscoveryResponse();
        return jsonReader.readObject(reader -> {
            block12: while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                switch (fieldName) {
                    case "authorization_endpoint": {
                        response.authorizationEndpoint = reader.getString();
                        continue block12;
                    }
                    case "token_endpoint": {
                        response.tokenEndpoint = reader.getString();
                        continue block12;
                    }
                    case "device_authorization_endpoint": {
                        response.deviceCodeEndpoint = reader.getString();
                        continue block12;
                    }
                    case "issuer": {
                        response.issuer = reader.getString();
                        continue block12;
                    }
                }
                reader.skipChildren();
            }
            return response;
        });
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("authorization_endpoint", this.authorizationEndpoint);
        jsonWriter.writeStringField("token_endpoint", this.tokenEndpoint);
        jsonWriter.writeStringField("device_authorization_endpoint", this.deviceCodeEndpoint);
        jsonWriter.writeEndObject();
        return jsonWriter;
    }

    String authorizationEndpoint() {
        return this.authorizationEndpoint;
    }

    String tokenEndpoint() {
        return this.tokenEndpoint;
    }

    String deviceCodeEndpoint() {
        return this.deviceCodeEndpoint;
    }

    String issuer() {
        return this.issuer;
    }
}

