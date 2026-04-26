/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.metadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.ContactInformation;
import net.fabricmc.loader.api.metadata.ModDependency;
import net.fabricmc.loader.api.metadata.ModEnvironment;
import net.fabricmc.loader.api.metadata.Person;
import net.fabricmc.loader.impl.lib.gson.JsonReader;
import net.fabricmc.loader.impl.lib.gson.JsonToken;
import net.fabricmc.loader.impl.metadata.ContactInfoBackedPerson;
import net.fabricmc.loader.impl.metadata.ContactInformationImpl;
import net.fabricmc.loader.impl.metadata.LoaderModMetadata;
import net.fabricmc.loader.impl.metadata.ModDependencyImpl;
import net.fabricmc.loader.impl.metadata.ModMetadataParser;
import net.fabricmc.loader.impl.metadata.ParseMetadataException;
import net.fabricmc.loader.impl.metadata.ParseWarning;
import net.fabricmc.loader.impl.metadata.V0ModMetadata;
import net.fabricmc.loader.impl.util.version.VersionParser;

final class V0ModMetadataParser {
    private static final Pattern WEBSITE_PATTERN = Pattern.compile("\\((.+)\\)");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("<(.+)>");

    V0ModMetadataParser() {
    }

    public static LoaderModMetadata parse(JsonReader reader) throws IOException, ParseMetadataException {
        ArrayList<ParseWarning> warnings = new ArrayList<ParseWarning>();
        String id = null;
        Version version = null;
        ArrayList<ModDependency> dependencies = new ArrayList<ModDependency>();
        V0ModMetadata.Mixins mixins = null;
        ModEnvironment environment = ModEnvironment.UNIVERSAL;
        String initializer = null;
        ArrayList<String> initializers = new ArrayList<String>();
        String name = null;
        String description = null;
        ArrayList<Person> authors = new ArrayList<Person>();
        ArrayList<Person> contributors = new ArrayList<Person>();
        ContactInformation links = null;
        String license = null;
        block48: while (reader.hasNext()) {
            String key;
            block19 : switch (key = reader.nextName()) {
                case "schemaVersion": {
                    if (reader.peek() != JsonToken.NUMBER) {
                        throw new ParseMetadataException("Duplicate \"schemaVersion\" field is not a number", reader);
                    }
                    int read = reader.nextInt();
                    if (read == 0) continue block48;
                    throw new ParseMetadataException(String.format("Duplicate \"schemaVersion\" field does not match the predicted schema version of 0. Duplicate field value is %s", read), reader);
                }
                case "id": {
                    if (reader.peek() != JsonToken.STRING) {
                        throw new ParseMetadataException("Mod id must be a non-empty string with a length of 3-64 characters.", reader);
                    }
                    id = reader.nextString();
                    break;
                }
                case "version": {
                    if (reader.peek() != JsonToken.STRING) {
                        throw new ParseMetadataException("Version must be a non-empty string", reader);
                    }
                    String rawVersion = reader.nextString();
                    try {
                        version = VersionParser.parse(rawVersion, false);
                        break;
                    }
                    catch (VersionParsingException e) {
                        throw new ParseMetadataException(String.format("Failed to parse version: %s", rawVersion), e);
                    }
                }
                case "requires": {
                    V0ModMetadataParser.readDependenciesContainer(reader, ModDependency.Kind.DEPENDS, dependencies, "requires");
                    break;
                }
                case "conflicts": {
                    V0ModMetadataParser.readDependenciesContainer(reader, ModDependency.Kind.BREAKS, dependencies, "conflicts");
                    break;
                }
                case "mixins": {
                    mixins = V0ModMetadataParser.readMixins(warnings, reader);
                    break;
                }
                case "side": {
                    String rawEnvironment;
                    if (reader.peek() != JsonToken.STRING) {
                        throw new ParseMetadataException("Side must be a string", reader);
                    }
                    switch (rawEnvironment = reader.nextString()) {
                        case "universal": {
                            environment = ModEnvironment.UNIVERSAL;
                            break block19;
                        }
                        case "client": {
                            environment = ModEnvironment.CLIENT;
                            break block19;
                        }
                        case "server": {
                            environment = ModEnvironment.SERVER;
                            break block19;
                        }
                    }
                    warnings.add(new ParseWarning(reader.getLineNumber(), reader.getColumn(), rawEnvironment, "Invalid side type"));
                    break;
                }
                case "initializer": {
                    if (!initializers.isEmpty()) {
                        throw new ParseMetadataException("initializer and initializers should not be set at the same time! (mod ID '" + id + "')");
                    }
                    if (reader.peek() != JsonToken.STRING) {
                        throw new ParseMetadataException("Initializer must be a non-empty string", reader);
                    }
                    initializer = reader.nextString();
                    break;
                }
                case "initializers": {
                    if (initializer != null) {
                        throw new ParseMetadataException("initializer and initializers should not be set at the same time! (mod ID '" + id + "')");
                    }
                    if (reader.peek() != JsonToken.BEGIN_ARRAY) {
                        throw new ParseMetadataException("Initializers must be in a list", reader);
                    }
                    reader.beginArray();
                    while (reader.hasNext()) {
                        if (reader.peek() != JsonToken.STRING) {
                            throw new ParseMetadataException("Initializer in initializers list must be a string", reader);
                        }
                        initializers.add(reader.nextString());
                    }
                    reader.endArray();
                    break;
                }
                case "name": {
                    if (reader.peek() != JsonToken.STRING) {
                        throw new ParseMetadataException("Name must be a string", reader);
                    }
                    name = reader.nextString();
                    break;
                }
                case "description": {
                    if (reader.peek() != JsonToken.STRING) {
                        throw new ParseMetadataException("Mod description must be a string", reader);
                    }
                    description = reader.nextString();
                    break;
                }
                case "recommends": {
                    V0ModMetadataParser.readDependenciesContainer(reader, ModDependency.Kind.SUGGESTS, dependencies, "recommends");
                    break;
                }
                case "authors": {
                    V0ModMetadataParser.readPeople(warnings, reader, authors);
                    break;
                }
                case "contributors": {
                    V0ModMetadataParser.readPeople(warnings, reader, contributors);
                    break;
                }
                case "links": {
                    links = V0ModMetadataParser.readLinks(warnings, reader);
                    break;
                }
                case "license": {
                    if (reader.peek() != JsonToken.STRING) {
                        throw new ParseMetadataException("License name must be a string", reader);
                    }
                    license = reader.nextString();
                    break;
                }
                default: {
                    if (!ModMetadataParser.IGNORED_KEYS.contains(key)) {
                        warnings.add(new ParseWarning(reader.getLineNumber(), reader.getColumn(), key, "Unsupported root entry"));
                    }
                    reader.skipValue();
                }
            }
        }
        if (id == null) {
            throw new ParseMetadataException.MissingField("id");
        }
        if (version == null) {
            throw new ParseMetadataException.MissingField("version");
        }
        ModMetadataParser.logWarningMessages(id, warnings);
        if (links == null) {
            links = ContactInformation.EMPTY;
        }
        return new V0ModMetadata(id, version, dependencies, mixins, environment, initializer, initializers, name, description, authors, contributors, links, license);
    }

    private static ContactInformation readLinks(List<ParseWarning> warnings, JsonReader reader) throws IOException, ParseMetadataException {
        HashMap<String, String> contactInfo = new HashMap<String, String>();
        switch (reader.peek()) {
            case STRING: {
                contactInfo.put("homepage", reader.nextString());
                break;
            }
            case BEGIN_OBJECT: {
                reader.beginObject();
                block14: while (reader.hasNext()) {
                    String key;
                    switch (key = reader.nextName()) {
                        case "homepage": {
                            if (reader.peek() != JsonToken.STRING) {
                                throw new ParseMetadataException("homepage link must be a string", reader);
                            }
                            contactInfo.put("homepage", reader.nextString());
                            continue block14;
                        }
                        case "issues": {
                            if (reader.peek() != JsonToken.STRING) {
                                throw new ParseMetadataException("issues link must be a string", reader);
                            }
                            contactInfo.put("issues", reader.nextString());
                            continue block14;
                        }
                        case "sources": {
                            if (reader.peek() != JsonToken.STRING) {
                                throw new ParseMetadataException("sources link must be a string", reader);
                            }
                            contactInfo.put("sources", reader.nextString());
                            continue block14;
                        }
                    }
                    warnings.add(new ParseWarning(reader.getLineNumber(), reader.getColumn(), key, "Unsupported links entry"));
                    reader.skipValue();
                }
                reader.endObject();
                break;
            }
            default: {
                throw new ParseMetadataException("Expected links to be an object or string", reader);
            }
        }
        return new ContactInformationImpl(contactInfo);
    }

    private static V0ModMetadata.Mixins readMixins(List<ParseWarning> warnings, JsonReader reader) throws IOException, ParseMetadataException {
        ArrayList<String> client = new ArrayList<String>();
        ArrayList<String> common = new ArrayList<String>();
        ArrayList<String> server = new ArrayList<String>();
        if (reader.peek() != JsonToken.BEGIN_OBJECT) {
            throw new ParseMetadataException("Expected mixins to be an object.", reader);
        }
        reader.beginObject();
        block10: while (reader.hasNext()) {
            String environment;
            switch (environment = reader.nextName()) {
                case "client": {
                    client.addAll(V0ModMetadataParser.readStringArray(reader, "client"));
                    continue block10;
                }
                case "common": {
                    common.addAll(V0ModMetadataParser.readStringArray(reader, "common"));
                    continue block10;
                }
                case "server": {
                    server.addAll(V0ModMetadataParser.readStringArray(reader, "server"));
                    continue block10;
                }
            }
            warnings.add(new ParseWarning(reader.getLineNumber(), reader.getColumn(), environment, "Invalid environment type"));
            reader.skipValue();
        }
        reader.endObject();
        return new V0ModMetadata.Mixins(client, common, server);
    }

    private static List<String> readStringArray(JsonReader reader, String key) throws IOException, ParseMetadataException {
        switch (reader.peek()) {
            case NULL: {
                reader.nextNull();
                return Collections.emptyList();
            }
            case STRING: {
                return Collections.singletonList(reader.nextString());
            }
            case BEGIN_ARRAY: {
                reader.beginArray();
                ArrayList<String> list = new ArrayList<String>();
                while (reader.hasNext()) {
                    if (reader.peek() != JsonToken.STRING) {
                        throw new ParseMetadataException(String.format("Expected entries in %s to be an array of strings", key), reader);
                    }
                    list.add(reader.nextString());
                }
                reader.endArray();
                return list;
            }
        }
        throw new ParseMetadataException(String.format("Expected %s to be a string or an array of strings", key), reader);
    }

    private static void readDependenciesContainer(JsonReader reader, ModDependency.Kind kind, List<ModDependency> dependencies, String name) throws IOException, ParseMetadataException {
        if (reader.peek() != JsonToken.BEGIN_OBJECT) {
            throw new ParseMetadataException(String.format("%s must be an object containing dependencies.", name), reader);
        }
        reader.beginObject();
        while (reader.hasNext()) {
            String modId = reader.nextName();
            ArrayList<String> versionMatchers = new ArrayList<String>();
            switch (reader.peek()) {
                case STRING: {
                    versionMatchers.add(reader.nextString());
                    break;
                }
                case BEGIN_ARRAY: {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        if (reader.peek() != JsonToken.STRING) {
                            throw new ParseMetadataException("List of version requirements must be strings", reader);
                        }
                        versionMatchers.add(reader.nextString());
                    }
                    reader.endArray();
                    break;
                }
                default: {
                    throw new ParseMetadataException("Expected version to be a string or array", reader);
                }
            }
            try {
                dependencies.add(new ModDependencyImpl(kind, modId, versionMatchers));
            }
            catch (VersionParsingException e) {
                throw new ParseMetadataException(e);
            }
        }
        reader.endObject();
    }

    private static void readPeople(List<ParseWarning> warnings, JsonReader reader, List<Person> people) throws IOException, ParseMetadataException {
        if (reader.peek() != JsonToken.BEGIN_ARRAY) {
            throw new ParseMetadataException("List of people must be an array", reader);
        }
        reader.beginArray();
        while (reader.hasNext()) {
            people.add(V0ModMetadataParser.readPerson(warnings, reader));
        }
        reader.endArray();
    }

    private static Person readPerson(List<ParseWarning> warnings, JsonReader reader) throws IOException, ParseMetadataException {
        HashMap<String, String> contactMap = new HashMap<String, String>();
        String name = "";
        switch (reader.peek()) {
            case STRING: {
                Matcher emailMatcher;
                String person = reader.nextString();
                CharSequence[] parts = person.split(" ");
                Matcher websiteMatcher = WEBSITE_PATTERN.matcher(parts[parts.length - 1]);
                if (websiteMatcher.matches()) {
                    contactMap.put("website", websiteMatcher.group(1));
                    parts = Arrays.copyOf(parts, parts.length - 1);
                }
                if ((emailMatcher = EMAIL_PATTERN.matcher(parts[parts.length - 1])).matches()) {
                    contactMap.put("email", emailMatcher.group(1));
                    parts = (String[])Arrays.copyOf(parts, parts.length - 1);
                }
                name = String.join((CharSequence)" ", parts);
                return new ContactInfoBackedPerson(name, new ContactInformationImpl(contactMap));
            }
            case BEGIN_OBJECT: {
                reader.beginObject();
                while (reader.hasNext()) {
                    String key;
                    switch (key = reader.nextName()) {
                        case "name": {
                            if (reader.peek() != JsonToken.STRING) break;
                            name = reader.nextString();
                            break;
                        }
                        case "email": {
                            if (reader.peek() != JsonToken.STRING) break;
                            contactMap.put("email", reader.nextString());
                            break;
                        }
                        case "website": {
                            if (reader.peek() != JsonToken.STRING) break;
                            contactMap.put("website", reader.nextString());
                            break;
                        }
                        default: {
                            warnings.add(new ParseWarning(reader.getLineNumber(), reader.getColumn(), key, "Unsupported contact information entry"));
                            reader.skipValue();
                        }
                    }
                }
                reader.endObject();
                return new ContactInfoBackedPerson(name, new ContactInformationImpl(contactMap));
            }
        }
        throw new ParseMetadataException("Expected person to be a string or object", reader);
    }
}

