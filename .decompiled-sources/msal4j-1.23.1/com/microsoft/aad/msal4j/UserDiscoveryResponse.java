/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.microsoft.aad.msal4j.StringHelper;
import java.io.IOException;

class UserDiscoveryResponse
implements JsonSerializable<UserDiscoveryResponse> {
    private float version;
    private String accountType;
    private String federationMetadataUrl;
    private String federationProtocol;
    private String federationActiveAuthUrl;
    private String cloudAudienceUrn;

    UserDiscoveryResponse() {
    }

    boolean isAccountFederated() {
        return !StringHelper.isBlank(this.accountType) && this.accountType.equalsIgnoreCase("Federated");
    }

    boolean isAccountManaged() {
        return !StringHelper.isBlank(this.accountType) && this.accountType.equalsIgnoreCase("Managed");
    }

    public static UserDiscoveryResponse fromJson(JsonReader jsonReader) throws IOException {
        UserDiscoveryResponse response = new UserDiscoveryResponse();
        return jsonReader.readObject(reader -> {
            block16: while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                switch (fieldName) {
                    case "ver": {
                        response.version = Float.parseFloat(reader.getString());
                        continue block16;
                    }
                    case "account_type": {
                        response.accountType = reader.getString();
                        continue block16;
                    }
                    case "federation_metadata_url": {
                        response.federationMetadataUrl = reader.getString();
                        continue block16;
                    }
                    case "federation_protocol": {
                        response.federationProtocol = reader.getString();
                        continue block16;
                    }
                    case "federation_active_auth_url": {
                        response.federationActiveAuthUrl = reader.getString();
                        continue block16;
                    }
                    case "cloud_audience_urn": {
                        response.cloudAudienceUrn = reader.getString();
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
        jsonWriter.writeFloatField("ver", this.version);
        jsonWriter.writeStringField("account_type", this.accountType);
        jsonWriter.writeStringField("federation_metadata_url", this.federationMetadataUrl);
        jsonWriter.writeStringField("federation_protocol", this.federationProtocol);
        jsonWriter.writeStringField("federation_active_auth_url", this.federationActiveAuthUrl);
        jsonWriter.writeStringField("cloud_audience_urn", this.cloudAudienceUrn);
        jsonWriter.writeEndObject();
        return jsonWriter;
    }

    float version() {
        return this.version;
    }

    String accountType() {
        return this.accountType;
    }

    String federationMetadataUrl() {
        return this.federationMetadataUrl;
    }

    String federationProtocol() {
        return this.federationProtocol;
    }

    String federationActiveAuthUrl() {
        return this.federationActiveAuthUrl;
    }

    String cloudAudienceUrn() {
        return this.cloudAudienceUrn;
    }
}

