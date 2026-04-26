/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

class Credential
implements JsonSerializable<Credential> {
    protected String homeAccountId;
    protected String environment;
    protected String clientId;
    protected String secret;
    protected String userAssertionHash;

    Credential() {
    }

    static Credential fromJson(JsonReader jsonReader) throws IOException {
        Credential credential = new Credential();
        return jsonReader.readObject(reader -> {
            block14: while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                switch (fieldName) {
                    case "home_account_id": {
                        credential.homeAccountId = reader.getString();
                        continue block14;
                    }
                    case "environment": {
                        credential.environment = reader.getString();
                        continue block14;
                    }
                    case "client_id": {
                        credential.clientId = reader.getString();
                        continue block14;
                    }
                    case "secret": {
                        credential.secret = reader.getString();
                        continue block14;
                    }
                    case "user_assertion_hash": {
                        credential.userAssertionHash = reader.getString();
                        continue block14;
                    }
                }
                reader.skipChildren();
            }
            return credential;
        });
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("home_account_id", this.homeAccountId);
        jsonWriter.writeStringField("environment", this.environment);
        jsonWriter.writeStringField("client_id", this.clientId);
        jsonWriter.writeStringField("secret", this.secret);
        jsonWriter.writeStringField("user_assertion_hash", this.userAssertionHash);
        jsonWriter.writeEndObject();
        return jsonWriter;
    }

    String homeAccountId() {
        return this.homeAccountId;
    }

    String environment() {
        return this.environment;
    }

    String clientId() {
        return this.clientId;
    }

    String secret() {
        return this.secret;
    }

    String userAssertionHash() {
        return this.userAssertionHash;
    }

    void homeAccountId(String homeAccountId) {
        this.homeAccountId = homeAccountId;
    }

    void environment(String environment) {
        this.environment = environment;
    }

    void clientId(String clientId) {
        this.clientId = clientId;
    }

    void secret(String secret) {
        this.secret = secret;
    }

    void userAssertionHash(String userAssertionHash) {
        this.userAssertionHash = userAssertionHash;
    }
}

