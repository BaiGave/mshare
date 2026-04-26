/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.microsoft.aad.msal4j.Credential;
import com.microsoft.aad.msal4j.StringHelper;
import java.io.IOException;
import java.util.ArrayList;

class AccessTokenCacheEntity
extends Credential
implements JsonSerializable<Credential> {
    private String credentialType;
    protected String realm;
    private String target;
    private String cachedAt;
    private String expiresOn;
    private String extExpiresOn;
    private String refreshOn;

    AccessTokenCacheEntity() {
    }

    String getKey() {
        ArrayList<String> keyParts = new ArrayList<String>();
        keyParts.add(StringHelper.isBlank(this.homeAccountId) ? "" : this.homeAccountId);
        keyParts.add(this.environment);
        keyParts.add(this.credentialType);
        keyParts.add(this.clientId);
        keyParts.add(this.realm);
        keyParts.add(this.target);
        return String.join((CharSequence)"-", keyParts).toLowerCase();
    }

    static AccessTokenCacheEntity fromJson(JsonReader jsonReader) throws IOException {
        AccessTokenCacheEntity entity = new AccessTokenCacheEntity();
        return jsonReader.readObject(reader -> {
            block28: while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                switch (fieldName) {
                    case "home_account_id": {
                        entity.homeAccountId = reader.getString();
                        continue block28;
                    }
                    case "environment": {
                        entity.environment = reader.getString();
                        continue block28;
                    }
                    case "credential_type": {
                        entity.credentialType = reader.getString();
                        continue block28;
                    }
                    case "client_id": {
                        entity.clientId = reader.getString();
                        continue block28;
                    }
                    case "secret": {
                        entity.secret = reader.getString();
                        continue block28;
                    }
                    case "realm": {
                        entity.realm = reader.getString();
                        continue block28;
                    }
                    case "target": {
                        entity.target = reader.getString();
                        continue block28;
                    }
                    case "cached_at": {
                        entity.cachedAt = reader.getString();
                        continue block28;
                    }
                    case "expires_on": {
                        entity.expiresOn = reader.getString();
                        continue block28;
                    }
                    case "extended_expires_on": {
                        entity.extExpiresOn = reader.getString();
                        continue block28;
                    }
                    case "refresh_on": {
                        entity.refreshOn = reader.getString();
                        continue block28;
                    }
                    case "user_assertion_hash": {
                        entity.userAssertionHash = reader.getString();
                        continue block28;
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
        jsonWriter.writeStringField("target", this.target);
        jsonWriter.writeStringField("cached_at", this.cachedAt);
        jsonWriter.writeStringField("expires_on", this.expiresOn);
        jsonWriter.writeStringField("extended_expires_on", this.extExpiresOn);
        jsonWriter.writeStringField("refresh_on", this.refreshOn);
        jsonWriter.writeStringField("user_assertion_hash", this.userAssertionHash);
        jsonWriter.writeEndObject();
        return jsonWriter;
    }

    String target() {
        return this.target;
    }

    String cachedAt() {
        return this.cachedAt;
    }

    String expiresOn() {
        return this.expiresOn;
    }

    String extExpiresOn() {
        return this.extExpiresOn;
    }

    String refreshOn() {
        return this.refreshOn;
    }

    void credentialType(String credentialType) {
        this.credentialType = credentialType;
    }

    void realm(String realm) {
        this.realm = realm;
    }

    void target(String target) {
        this.target = target;
    }

    void cachedAt(String cachedAt) {
        this.cachedAt = cachedAt;
    }

    void expiresOn(String expiresOn) {
        this.expiresOn = expiresOn;
    }

    void extExpiresOn(String extExpiresOn) {
        this.extExpiresOn = extExpiresOn;
    }

    void refreshOn(String refreshOn) {
        this.refreshOn = refreshOn;
    }
}

