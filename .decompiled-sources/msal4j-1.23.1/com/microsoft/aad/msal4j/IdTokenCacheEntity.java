/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.microsoft.aad.msal4j.Credential;
import java.io.IOException;
import java.util.ArrayList;

class IdTokenCacheEntity
extends Credential {
    private String credentialType;
    protected String realm;

    IdTokenCacheEntity() {
    }

    String getKey() {
        ArrayList<String> keyParts = new ArrayList<String>();
        keyParts.add(this.homeAccountId);
        keyParts.add(this.environment);
        keyParts.add(this.credentialType);
        keyParts.add(this.clientId);
        keyParts.add(this.realm);
        keyParts.add("");
        return String.join((CharSequence)"-", keyParts).toLowerCase();
    }

    static IdTokenCacheEntity fromJson(JsonReader jsonReader) throws IOException {
        IdTokenCacheEntity entity = new IdTokenCacheEntity();
        return jsonReader.readObject(reader -> {
            block18: while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                switch (fieldName) {
                    case "home_account_id": {
                        entity.homeAccountId = reader.getString();
                        continue block18;
                    }
                    case "environment": {
                        entity.environment = reader.getString();
                        continue block18;
                    }
                    case "credential_type": {
                        entity.credentialType = reader.getString();
                        continue block18;
                    }
                    case "client_id": {
                        entity.clientId = reader.getString();
                        continue block18;
                    }
                    case "secret": {
                        entity.secret = reader.getString();
                        continue block18;
                    }
                    case "realm": {
                        entity.realm = reader.getString();
                        continue block18;
                    }
                    case "user_assertion_hash": {
                        entity.userAssertionHash = reader.getString();
                        continue block18;
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
        jsonWriter.writeStringField("home_account_id", this.homeAccountId);
        jsonWriter.writeStringField("environment", this.environment);
        jsonWriter.writeStringField("credential_type", this.credentialType);
        jsonWriter.writeStringField("client_id", this.clientId);
        jsonWriter.writeStringField("secret", this.secret);
        jsonWriter.writeStringField("realm", this.realm);
        jsonWriter.writeStringField("user_assertion_hash", this.userAssertionHash);
        jsonWriter.writeEndObject();
        return jsonWriter;
    }

    String credentialType() {
        return this.credentialType;
    }

    String realm() {
        return this.realm;
    }

    void credentialType(String credentialType) {
        this.credentialType = credentialType;
    }

    void realm(String realm) {
        this.realm = realm;
    }
}

