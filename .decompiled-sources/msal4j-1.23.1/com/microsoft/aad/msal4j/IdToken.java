/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.io.Serializable;

class IdToken
implements Serializable,
JsonSerializable<IdToken> {
    protected String issuer;
    protected String subject;
    protected String audience;
    protected Long expirationTime;
    protected Long issuedAt;
    protected Long notBefore;
    protected String name;
    protected String preferredUsername;
    protected String objectIdentifier;
    protected String tenantIdentifier;
    protected String upn;
    protected String uniqueName;

    IdToken() {
    }

    static IdToken fromJson(JsonReader jsonReader) throws IOException {
        IdToken idToken = new IdToken();
        return jsonReader.readObject(reader -> {
            block28: while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                switch (fieldName) {
                    case "iss": {
                        idToken.issuer = reader.getString();
                        continue block28;
                    }
                    case "sub": {
                        idToken.subject = reader.getString();
                        continue block28;
                    }
                    case "aud": {
                        idToken.audience = reader.getString();
                        continue block28;
                    }
                    case "exp": {
                        idToken.expirationTime = reader.getLong();
                        continue block28;
                    }
                    case "iat": {
                        idToken.issuedAt = reader.getLong();
                        continue block28;
                    }
                    case "nbf": {
                        idToken.notBefore = reader.getLong();
                        continue block28;
                    }
                    case "name": {
                        idToken.name = reader.getString();
                        continue block28;
                    }
                    case "preferred_username": {
                        idToken.preferredUsername = reader.getString();
                        continue block28;
                    }
                    case "oid": {
                        idToken.objectIdentifier = reader.getString();
                        continue block28;
                    }
                    case "tid": {
                        idToken.tenantIdentifier = reader.getString();
                        continue block28;
                    }
                    case "upn": {
                        idToken.upn = reader.getString();
                        continue block28;
                    }
                    case "unique_name": {
                        idToken.uniqueName = reader.getString();
                        continue block28;
                    }
                }
                reader.skipChildren();
            }
            return idToken;
        });
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("iss", this.issuer);
        jsonWriter.writeStringField("sub", this.subject);
        jsonWriter.writeStringField("aud", this.audience);
        jsonWriter.writeNumberField("exp", this.expirationTime);
        jsonWriter.writeNumberField("iat", this.issuedAt);
        jsonWriter.writeNumberField("nbf", this.notBefore);
        jsonWriter.writeStringField("name", this.name);
        jsonWriter.writeStringField("preferred_username", this.preferredUsername);
        jsonWriter.writeStringField("oid", this.objectIdentifier);
        jsonWriter.writeStringField("tid", this.tenantIdentifier);
        jsonWriter.writeStringField("upn", this.upn);
        jsonWriter.writeStringField("unique_name", this.uniqueName);
        jsonWriter.writeEndObject();
        return jsonWriter;
    }
}

