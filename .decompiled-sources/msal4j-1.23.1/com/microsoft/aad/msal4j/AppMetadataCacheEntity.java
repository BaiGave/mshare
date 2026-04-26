/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.ArrayList;

class AppMetadataCacheEntity
implements JsonSerializable<AppMetadataCacheEntity> {
    public static final String APP_METADATA_CACHE_ENTITY_ID = "appmetadata";
    private String clientId;
    private String environment;
    private String familyId;

    AppMetadataCacheEntity() {
    }

    static AppMetadataCacheEntity fromJson(JsonReader jsonReader) throws IOException {
        AppMetadataCacheEntity entity = new AppMetadataCacheEntity();
        return jsonReader.readObject(reader -> {
            block10: while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                switch (fieldName) {
                    case "client_id": {
                        entity.clientId = reader.getString();
                        continue block10;
                    }
                    case "environment": {
                        entity.environment = reader.getString();
                        continue block10;
                    }
                    case "family_id": {
                        entity.familyId = reader.getString();
                        continue block10;
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
        jsonWriter.writeStringField("client_id", this.clientId);
        jsonWriter.writeStringField("environment", this.environment);
        jsonWriter.writeStringField("family_id", this.familyId);
        jsonWriter.writeEndObject();
        return jsonWriter;
    }

    String getKey() {
        ArrayList<String> keyParts = new ArrayList<String>();
        keyParts.add(APP_METADATA_CACHE_ENTITY_ID);
        keyParts.add(this.environment);
        keyParts.add(this.clientId);
        return String.join((CharSequence)"-", keyParts).toLowerCase();
    }

    String clientId() {
        return this.clientId;
    }

    String environment() {
        return this.environment;
    }

    String familyId() {
        return this.familyId;
    }

    void clientId(String clientId) {
        this.clientId = clientId;
    }

    void environment(String environment) {
        this.environment = environment;
    }

    void familyId(String familyId) {
        this.familyId = familyId;
    }
}

