/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.microsoft.aad.msal4j.Credential;
import com.microsoft.aad.msal4j.StringHelper;
import java.io.IOException;
import java.util.ArrayList;

class RefreshTokenCacheEntity
extends Credential {
    private String credentialType;
    private String family_id;

    RefreshTokenCacheEntity() {
    }

    static RefreshTokenCacheEntity fromJson(JsonReader jsonReader) throws IOException {
        RefreshTokenCacheEntity entity = new RefreshTokenCacheEntity();
        return jsonReader.readObject(reader -> {
            block16: while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                switch (fieldName) {
                    case "credential_type": {
                        entity.credentialType = reader.getString();
                        continue block16;
                    }
                    case "family_id": {
                        entity.family_id = reader.getString();
                        continue block16;
                    }
                    case "home_account_id": {
                        entity.homeAccountId = reader.getString();
                        continue block16;
                    }
                    case "environment": {
                        entity.environment = reader.getString();
                        continue block16;
                    }
                    case "client_id": {
                        entity.clientId = reader.getString();
                        continue block16;
                    }
                    case "secret": {
                        entity.secret = reader.getString();
                        continue block16;
                    }
                }
                reader.skipChildren();
            }
            return entity;
        });
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("credential_type", this.credentialType);
        jsonWriter.writeStringField("family_id", this.family_id);
        jsonWriter.writeStringField("home_account_id", this.homeAccountId);
        jsonWriter.writeStringField("environment", this.environment);
        jsonWriter.writeStringField("client_id", this.clientId);
        jsonWriter.writeStringField("secret", this.secret);
        jsonWriter.writeEndObject();
        return jsonWriter;
    }

    boolean isFamilyRT() {
        return !StringHelper.isBlank(this.family_id);
    }

    String getKey() {
        ArrayList<String> keyParts = new ArrayList<String>();
        keyParts.add(this.homeAccountId);
        keyParts.add(this.environment);
        keyParts.add(this.credentialType);
        if (this.isFamilyRT()) {
            keyParts.add(this.family_id);
        } else {
            keyParts.add(this.clientId);
        }
        keyParts.add("");
        keyParts.add("");
        return String.join((CharSequence)"-", keyParts).toLowerCase();
    }

    String credentialType() {
        return this.credentialType;
    }

    String family_id() {
        return this.family_id;
    }

    void credentialType(String credentialType) {
        this.credentialType = credentialType;
    }

    void family_id(String family_id) {
        this.family_id = family_id;
    }
}

