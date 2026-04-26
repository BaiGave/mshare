/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

class InstanceDiscoveryMetadataEntry
implements JsonSerializable<InstanceDiscoveryMetadataEntry> {
    String preferredNetwork;
    String preferredCache;
    Set<String> aliases;

    public InstanceDiscoveryMetadataEntry(String preferredNetwork, String preferredCache, Set<String> aliases) {
        this.preferredNetwork = preferredNetwork;
        this.preferredCache = preferredCache;
        this.aliases = aliases;
    }

    public InstanceDiscoveryMetadataEntry() {
    }

    public static InstanceDiscoveryMetadataEntry fromJson(JsonReader jsonReader) throws IOException {
        InstanceDiscoveryMetadataEntry entry = new InstanceDiscoveryMetadataEntry();
        return jsonReader.readObject(reader -> {
            block10: while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                switch (fieldName) {
                    case "preferred_network": {
                        entry.preferredNetwork = reader.getString();
                        continue block10;
                    }
                    case "preferred_cache": {
                        entry.preferredCache = reader.getString();
                        continue block10;
                    }
                    case "aliases": {
                        entry.aliases = new HashSet<String>(reader.readArray(JsonReader::getString));
                        continue block10;
                    }
                }
                reader.skipChildren();
            }
            return entry;
        });
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("preferred_network", this.preferredNetwork);
        jsonWriter.writeStringField("preferred_cache", this.preferredCache);
        jsonWriter.writeArrayField("aliases", this.aliases, JsonWriter::writeString);
        jsonWriter.writeEndObject();
        return jsonWriter;
    }

    String preferredNetwork() {
        return this.preferredNetwork;
    }

    String preferredCache() {
        return this.preferredCache;
    }

    Set<String> aliases() {
        return this.aliases;
    }
}

