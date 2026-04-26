/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.microsoft.aad.msal4j.InstanceDiscoveryMetadataEntry;
import java.io.IOException;
import java.util.List;

class AadInstanceDiscoveryResponse
implements JsonSerializable<AadInstanceDiscoveryResponse> {
    private String tenantDiscoveryEndpoint;
    private List<InstanceDiscoveryMetadataEntry> metadata;
    private String errorDescription;
    private List<Long> errorCodes;
    private String error;
    private String correlationId;

    AadInstanceDiscoveryResponse() {
    }

    public static AadInstanceDiscoveryResponse fromJson(JsonReader jsonReader) throws IOException {
        AadInstanceDiscoveryResponse response = new AadInstanceDiscoveryResponse();
        return jsonReader.readObject(reader -> {
            block16: while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                switch (fieldName) {
                    case "tenant_discovery_endpoint": {
                        response.tenantDiscoveryEndpoint = reader.getString();
                        continue block16;
                    }
                    case "metadata": {
                        response.metadata = reader.readArray(InstanceDiscoveryMetadataEntry::fromJson);
                        continue block16;
                    }
                    case "error_description": {
                        response.errorDescription = reader.getString();
                        continue block16;
                    }
                    case "error_codes": {
                        response.errorCodes = reader.readArray(JsonReader::getLong);
                        continue block16;
                    }
                    case "error": {
                        response.error = reader.getString();
                        continue block16;
                    }
                    case "correlation_id": {
                        response.correlationId = reader.getString();
                        continue block16;
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
        jsonWriter.writeStringField("tenant_discovery_endpoint", this.tenantDiscoveryEndpoint);
        jsonWriter.writeArrayField("metadata", this.metadata, JsonWriter::writeJson);
        jsonWriter.writeStringField("error_description", this.errorDescription);
        jsonWriter.writeArrayField("error_codes", this.errorCodes, JsonWriter::writeLong);
        jsonWriter.writeStringField("error", this.error);
        jsonWriter.writeStringField("correlation_id", this.correlationId);
        jsonWriter.writeEndObject();
        return jsonWriter;
    }

    String tenantDiscoveryEndpoint() {
        return this.tenantDiscoveryEndpoint;
    }

    List<InstanceDiscoveryMetadataEntry> metadata() {
        return this.metadata;
    }

    String errorDescription() {
        return this.errorDescription;
    }

    List<Long> errorCodes() {
        return this.errorCodes;
    }

    String error() {
        return this.error;
    }

    String correlationId() {
        return this.correlationId;
    }
}

