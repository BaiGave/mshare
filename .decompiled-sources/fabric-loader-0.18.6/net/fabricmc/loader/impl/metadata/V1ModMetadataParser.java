/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.metadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.ContactInformation;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModDependency;
import net.fabricmc.loader.api.metadata.ModEnvironment;
import net.fabricmc.loader.api.metadata.Person;
import net.fabricmc.loader.impl.lib.gson.JsonReader;
import net.fabricmc.loader.impl.lib.gson.JsonToken;
import net.fabricmc.loader.impl.metadata.ContactInfoBackedPerson;
import net.fabricmc.loader.impl.metadata.ContactInformationImpl;
import net.fabricmc.loader.impl.metadata.CustomValueImpl;
import net.fabricmc.loader.impl.metadata.EntrypointMetadata;
import net.fabricmc.loader.impl.metadata.LoaderModMetadata;
import net.fabricmc.loader.impl.metadata.ModDependencyImpl;
import net.fabricmc.loader.impl.metadata.ModMetadataParser;
import net.fabricmc.loader.impl.metadata.NestedJarEntry;
import net.fabricmc.loader.impl.metadata.ParseMetadataException;
import net.fabricmc.loader.impl.metadata.ParseWarning;
import net.fabricmc.loader.impl.metadata.SimplePerson;
import net.fabricmc.loader.impl.metadata.V1ModMetadata;
import net.fabricmc.loader.impl.util.version.VersionParser;

final class V1ModMetadataParser {
    static LoaderModMetadata parse(JsonReader reader) throws IOException, ParseMetadataException {
        ArrayList<ParseWarning> warnings = new ArrayList<ParseWarning>();
        String id = null;
        Version version = null;
        ArrayList<String> provides = new ArrayList<String>();
        ModEnvironment environment = ModEnvironment.UNIVERSAL;
        HashMap<String, List<EntrypointMetadata>> entrypoints = new HashMap<String, List<EntrypointMetadata>>();
        ArrayList<NestedJarEntry> jars = new ArrayList<NestedJarEntry>();
        ArrayList<V1ModMetadata.MixinEntry> mixins = new ArrayList<V1ModMetadata.MixinEntry>();
        String classTweaker = null;
        ArrayList<ModDependency> dependencies = new ArrayList<ModDependency>();
        boolean hasRequires = false;
        String name = null;
        String description = null;
        ArrayList<Person> authors = new ArrayList<Person>();
        ArrayList<Person> contributors = new ArrayList<Person>();
        ContactInformation contact = null;
        ArrayList<String> license = new ArrayList<String>();
        V1ModMetadata.IconEntry icon = null;
        HashMap<String, String> languageAdapters = new HashMap<String, String>();
        HashMap<String, CustomValue> customValues = new HashMap<String, CustomValue>();
        block54: while (reader.hasNext()) {
            String key;
            switch (key = reader.nextName()) {
                case "schemaVersion": {
                    if (reader.peek() != JsonToken.NUMBER) {
                        throw new ParseMetadataException("Duplicate \"schemaVersion\" field is not a number", reader);
                    }
                    int read = reader.nextInt();
                    if (read == 1) continue block54;
                    throw new ParseMetadataException(String.format("Duplicate \"schemaVersion\" field does not match the predicted schema version of 1. Duplicate field value is %s", read), reader);
                }
                case "id": {
                    if (reader.peek() != JsonToken.STRING) {
                        throw new ParseMetadataException("Mod id must be a non-empty string with a length of 3-64 characters.", reader);
                    }
                    id = reader.nextString();
                    continue block54;
                }
                case "version": {
                    if (reader.peek() != JsonToken.STRING) {
                        throw new ParseMetadataException("Version must be a non-empty string", reader);
                    }
                    try {
                        version = VersionParser.parse(reader.nextString(), false);
                        continue block54;
                    }
                    catch (VersionParsingException e) {
                        throw new ParseMetadataException("Failed to parse version", e);
                    }
                }
                case "provides": {
                    V1ModMetadataParser.readProvides(reader, provides);
                    continue block54;
                }
                case "environment": {
                    if (reader.peek() != JsonToken.STRING) {
                        throw new ParseMetadataException("Environment must be a string", reader);
                    }
                    environment = V1ModMetadataParser.readEnvironment(reader);
                    continue block54;
                }
                case "entrypoints": {
                    V1ModMetadataParser.readEntrypoints(warnings, reader, entrypoints);
                    continue block54;
                }
                case "jars": {
                    V1ModMetadataParser.readNestedJarEntries(warnings, reader, jars);
                    continue block54;
                }
                case "mixins": {
                    V1ModMetadataParser.readMixinConfigs(warnings, reader, mixins);
                    continue block54;
                }
                case "accessWidener": {
                    if (reader.peek() != JsonToken.STRING) {
                        throw new ParseMetadataException("Access Widener file must be a string", reader);
                    }
                    classTweaker = reader.nextString();
                    continue block54;
                }
                case "depends": {
                    V1ModMetadataParser.readDependenciesContainer(reader, ModDependency.Kind.DEPENDS, dependencies);
                    continue block54;
                }
                case "recommends": {
                    V1ModMetadataParser.readDependenciesContainer(reader, ModDependency.Kind.RECOMMENDS, dependencies);
                    continue block54;
                }
                case "suggests": {
                    V1ModMetadataParser.readDependenciesContainer(reader, ModDependency.Kind.SUGGESTS, dependencies);
                    continue block54;
                }
                case "conflicts": {
                    V1ModMetadataParser.readDependenciesContainer(reader, ModDependency.Kind.CONFLICTS, dependencies);
                    continue block54;
                }
                case "breaks": {
                    V1ModMetadataParser.readDependenciesContainer(reader, ModDependency.Kind.BREAKS, dependencies);
                    continue block54;
                }
                case "requires": {
                    hasRequires = true;
                    reader.skipValue();
                    continue block54;
                }
                case "name": {
                    if (reader.peek() != JsonToken.STRING) {
                        throw new ParseMetadataException("Mod name must be a string", reader);
                    }
                    name = reader.nextString();
                    continue block54;
                }
                case "description": {
                    if (reader.peek() != JsonToken.STRING) {
                        throw new ParseMetadataException("Mod description must be a string", reader);
                    }
                    description = reader.nextString();
                    continue block54;
                }
                case "authors": {
                    V1ModMetadataParser.readPeople(warnings, reader, authors);
                    continue block54;
                }
                case "contributors": {
                    V1ModMetadataParser.readPeople(warnings, reader, contributors);
                    continue block54;
                }
                case "contact": {
                    contact = V1ModMetadataParser.readContactInfo(reader);
                    continue block54;
                }
                case "license": {
                    V1ModMetadataParser.readLicense(reader, license);
                    continue block54;
                }
                case "icon": {
                    icon = V1ModMetadataParser.readIcon(reader);
                    continue block54;
                }
                case "languageAdapters": {
                    V1ModMetadataParser.readLanguageAdapters(reader, languageAdapters);
                    continue block54;
                }
                case "custom": {
                    V1ModMetadataParser.readCustomValues(reader, customValues);
                    continue block54;
                }
            }
            if (!ModMetadataParser.IGNORED_KEYS.contains(key)) {
                warnings.add(new ParseWarning(reader.getLineNumber(), reader.getColumn(), key, "Unsupported root entry"));
            }
            reader.skipValue();
        }
        if (id == null) {
            throw new ParseMetadataException.MissingField("id");
        }
        if (version == null) {
            throw new ParseMetadataException.MissingField("version");
        }
        ModMetadataParser.logWarningMessages(id, warnings);
        return new V1ModMetadata(id, version, provides, environment, entrypoints, jars, mixins, classTweaker, dependencies, hasRequires, name, description, authors, contributors, contact, license, icon, languageAdapters, customValues);
    }

    private static void readProvides(JsonReader reader, List<String> provides) throws IOException, ParseMetadataException {
        if (reader.peek() != JsonToken.BEGIN_ARRAY) {
            throw new ParseMetadataException("Provides must be an array");
        }
        reader.beginArray();
        while (reader.hasNext()) {
            if (reader.peek() != JsonToken.STRING) {
                throw new ParseMetadataException("Provided id must be a string", reader);
            }
            provides.add(reader.nextString());
        }
        reader.endArray();
    }

    private static ModEnvironment readEnvironment(JsonReader reader) throws ParseMetadataException, IOException {
        String environment = reader.nextString().toLowerCase(Locale.ROOT);
        if (environment.isEmpty() || environment.equals("*")) {
            return ModEnvironment.UNIVERSAL;
        }
        if (environment.equals("client")) {
            return ModEnvironment.CLIENT;
        }
        if (environment.equals("server")) {
            return ModEnvironment.SERVER;
        }
        throw new ParseMetadataException("Invalid environment type: " + environment + "!", reader);
    }

    private static void readEntrypoints(List<ParseWarning> warnings, JsonReader reader, Map<String, List<EntrypointMetadata>> entrypoints) throws IOException, ParseMetadataException {
        if (reader.peek() != JsonToken.BEGIN_OBJECT) {
            throw new ParseMetadataException("Entrypoints must be an object", reader);
        }
        reader.beginObject();
        while (reader.hasNext()) {
            String key = reader.nextName();
            ArrayList<V1ModMetadata.EntrypointMetadataImpl> metadata = new ArrayList<V1ModMetadata.EntrypointMetadataImpl>();
            if (reader.peek() != JsonToken.BEGIN_ARRAY) {
                throw new ParseMetadataException("Entrypoint list must be an array!", reader);
            }
            reader.beginArray();
            while (reader.hasNext()) {
                String adapter = "default";
                String value = null;
                switch (reader.peek()) {
                    case STRING: {
                        value = reader.nextString();
                        break;
                    }
                    case BEGIN_OBJECT: {
                        reader.beginObject();
                        block14: while (reader.hasNext()) {
                            String entryKey;
                            switch (entryKey = reader.nextName()) {
                                case "adapter": {
                                    adapter = reader.nextString();
                                    continue block14;
                                }
                                case "value": {
                                    value = reader.nextString();
                                    continue block14;
                                }
                            }
                            warnings.add(new ParseWarning(reader.getLineNumber(), reader.getColumn(), entryKey, "Invalid entry in entrypoint metadata"));
                            reader.skipValue();
                        }
                        reader.endObject();
                        break;
                    }
                    default: {
                        throw new ParseMetadataException("Entrypoint must be a string or object with \"value\" field", reader);
                    }
                }
                if (value == null) {
                    throw new ParseMetadataException.MissingField("Entrypoint value must be present");
                }
                metadata.add(new V1ModMetadata.EntrypointMetadataImpl(adapter, value));
            }
            reader.endArray();
            entrypoints.put(key, metadata);
        }
        reader.endObject();
    }

    private static void readNestedJarEntries(List<ParseWarning> warnings, JsonReader reader, List<NestedJarEntry> jars) throws IOException, ParseMetadataException {
        if (reader.peek() != JsonToken.BEGIN_ARRAY) {
            throw new ParseMetadataException("Jar entries must be in an array", reader);
        }
        reader.beginArray();
        while (reader.hasNext()) {
            if (reader.peek() != JsonToken.BEGIN_OBJECT) {
                throw new ParseMetadataException("Invalid type for JAR entry!", reader);
            }
            reader.beginObject();
            String file = null;
            while (reader.hasNext()) {
                String key = reader.nextName();
                if (key.equals("file")) {
                    if (reader.peek() != JsonToken.STRING) {
                        throw new ParseMetadataException("\"file\" entry in jar object must be a string", reader);
                    }
                    file = reader.nextString();
                    continue;
                }
                warnings.add(new ParseWarning(reader.getLineNumber(), reader.getColumn(), key, "Invalid entry in jar entry"));
                reader.skipValue();
            }
            reader.endObject();
            if (file == null) {
                throw new ParseMetadataException("Missing mandatory key 'file' in JAR entry!", reader);
            }
            jars.add(new V1ModMetadata.JarEntry(file));
        }
        reader.endArray();
    }

    private static void readMixinConfigs(List<ParseWarning> warnings, JsonReader reader, List<V1ModMetadata.MixinEntry> mixins) throws IOException, ParseMetadataException {
        if (reader.peek() != JsonToken.BEGIN_ARRAY) {
            throw new ParseMetadataException("Mixin configs must be in an array", reader);
        }
        reader.beginArray();
        block12: while (reader.hasNext()) {
            switch (reader.peek()) {
                case STRING: {
                    mixins.add(new V1ModMetadata.MixinEntry(reader.nextString(), ModEnvironment.UNIVERSAL));
                    continue block12;
                }
                case BEGIN_OBJECT: {
                    reader.beginObject();
                    String config = null;
                    ModEnvironment environment = null;
                    block13: while (reader.hasNext()) {
                        String key;
                        switch (key = reader.nextName()) {
                            case "environment": {
                                environment = V1ModMetadataParser.readEnvironment(reader);
                                continue block13;
                            }
                            case "config": {
                                if (reader.peek() != JsonToken.STRING) {
                                    throw new ParseMetadataException("Value of \"config\" must be a string", reader);
                                }
                                config = reader.nextString();
                                continue block13;
                            }
                        }
                        warnings.add(new ParseWarning(reader.getLineNumber(), reader.getColumn(), key, "Invalid entry in mixin config entry"));
                        reader.skipValue();
                    }
                    reader.endObject();
                    if (environment == null) {
                        environment = ModEnvironment.UNIVERSAL;
                    }
                    if (config == null) {
                        throw new ParseMetadataException.MissingField("Missing mandatory key 'config' in mixin entry!");
                    }
                    mixins.add(new V1ModMetadata.MixinEntry(config, environment));
                    continue block12;
                }
            }
            warnings.add(new ParseWarning(reader.getLineNumber(), reader.getColumn(), "Invalid mixin entry type"));
            reader.skipValue();
        }
        reader.endArray();
    }

    private static void readDependenciesContainer(JsonReader reader, ModDependency.Kind kind, List<ModDependency> out) throws IOException, ParseMetadataException {
        if (reader.peek() != JsonToken.BEGIN_OBJECT) {
            throw new ParseMetadataException("Dependency container must be an object!", reader);
        }
        reader.beginObject();
        while (reader.hasNext()) {
            String modId = reader.nextName();
            ArrayList<String> matcherStringList = new ArrayList<String>();
            switch (reader.peek()) {
                case STRING: {
                    matcherStringList.add(reader.nextString());
                    break;
                }
                case BEGIN_ARRAY: {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        if (reader.peek() != JsonToken.STRING) {
                            throw new ParseMetadataException("Dependency version range array must only contain string values", reader);
                        }
                        matcherStringList.add(reader.nextString());
                    }
                    reader.endArray();
                    break;
                }
                default: {
                    throw new ParseMetadataException("Dependency version range must be a string or string array!", reader);
                }
            }
            try {
                out.add(new ModDependencyImpl(kind, modId, matcherStringList));
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
        block12: while (reader.hasNext()) {
            switch (reader.peek()) {
                case STRING: {
                    people.add(new SimplePerson(reader.nextString()));
                    continue block12;
                }
                case BEGIN_OBJECT: {
                    reader.beginObject();
                    String personName = null;
                    ContactInformation contactInformation = null;
                    block13: while (reader.hasNext()) {
                        String key;
                        switch (key = reader.nextName()) {
                            case "name": {
                                if (reader.peek() != JsonToken.STRING) {
                                    throw new ParseMetadataException("Name of person in dependency container must be a string", reader);
                                }
                                personName = reader.nextString();
                                continue block13;
                            }
                            case "contact": {
                                contactInformation = V1ModMetadataParser.readContactInfo(reader);
                                continue block13;
                            }
                        }
                        warnings.add(new ParseWarning(reader.getLineNumber(), reader.getColumn(), key, "Invalid entry in person"));
                        reader.skipValue();
                    }
                    reader.endObject();
                    if (personName == null) {
                        throw new ParseMetadataException.MissingField("Person object must have a 'name' field!");
                    }
                    if (contactInformation == null) {
                        contactInformation = ContactInformation.EMPTY;
                    }
                    people.add(new ContactInfoBackedPerson(personName, contactInformation));
                    continue block12;
                }
            }
            throw new ParseMetadataException("Person type must be an object or string!", reader);
        }
        reader.endArray();
    }

    private static ContactInformation readContactInfo(JsonReader reader) throws IOException, ParseMetadataException {
        if (reader.peek() != JsonToken.BEGIN_OBJECT) {
            throw new ParseMetadataException("Contact info must in an object", reader);
        }
        reader.beginObject();
        HashMap<String, String> map = new HashMap<String, String>();
        while (reader.hasNext()) {
            String key = reader.nextName();
            if (reader.peek() != JsonToken.STRING) {
                throw new ParseMetadataException("Contact information entries must be a string", reader);
            }
            map.put(key, reader.nextString());
        }
        reader.endObject();
        return new ContactInformationImpl(map);
    }

    private static void readLicense(JsonReader reader, List<String> license) throws IOException, ParseMetadataException {
        switch (reader.peek()) {
            case STRING: {
                license.add(reader.nextString());
                break;
            }
            case BEGIN_ARRAY: {
                reader.beginArray();
                while (reader.hasNext()) {
                    if (reader.peek() != JsonToken.STRING) {
                        throw new ParseMetadataException("List of licenses must only contain strings", reader);
                    }
                    license.add(reader.nextString());
                }
                reader.endArray();
                break;
            }
            default: {
                throw new ParseMetadataException("License must be a string or array of strings!", reader);
            }
        }
    }

    private static V1ModMetadata.IconEntry readIcon(JsonReader reader) throws IOException, ParseMetadataException {
        switch (reader.peek()) {
            case STRING: {
                return new V1ModMetadata.Single(reader.nextString());
            }
            case BEGIN_OBJECT: {
                reader.beginObject();
                TreeMap<Integer, String> iconMap = new TreeMap<Integer, String>(Comparator.naturalOrder());
                while (reader.hasNext()) {
                    int size;
                    String key = reader.nextName();
                    try {
                        size = Integer.parseInt(key);
                    }
                    catch (NumberFormatException e) {
                        throw new ParseMetadataException("Could not parse icon size '" + key + "'!", e);
                    }
                    if (size < 1) {
                        throw new ParseMetadataException("Size must be positive!", reader);
                    }
                    if (reader.peek() != JsonToken.STRING) {
                        throw new ParseMetadataException("Icon path must be a string", reader);
                    }
                    iconMap.put(size, reader.nextString());
                }
                reader.endObject();
                if (iconMap.isEmpty()) {
                    throw new ParseMetadataException("Icon object must not be empty!", reader);
                }
                return new V1ModMetadata.MapEntry(iconMap);
            }
        }
        throw new ParseMetadataException("Icon entry must be an object or string!", reader);
    }

    private static void readLanguageAdapters(JsonReader reader, Map<String, String> languageAdapters) throws IOException, ParseMetadataException {
        if (reader.peek() != JsonToken.BEGIN_OBJECT) {
            throw new ParseMetadataException("Language adapters must be in an object", reader);
        }
        reader.beginObject();
        while (reader.hasNext()) {
            String adapter = reader.nextName();
            if (reader.peek() != JsonToken.STRING) {
                throw new ParseMetadataException("Value of language adapter entry must be a string", reader);
            }
            languageAdapters.put(adapter, reader.nextString());
        }
        reader.endObject();
    }

    private static void readCustomValues(JsonReader reader, Map<String, CustomValue> customValues) throws IOException, ParseMetadataException {
        if (reader.peek() != JsonToken.BEGIN_OBJECT) {
            throw new ParseMetadataException("Custom values must be in an object!", reader);
        }
        reader.beginObject();
        while (reader.hasNext()) {
            customValues.put(reader.nextName(), CustomValueImpl.readCustomValue(reader));
        }
        reader.endObject();
    }

    private V1ModMetadataParser() {
    }
}

