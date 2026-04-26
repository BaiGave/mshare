/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.metadata;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.ModDependency;
import net.fabricmc.loader.impl.FormattedException;
import net.fabricmc.loader.impl.lib.gson.JsonReader;
import net.fabricmc.loader.impl.lib.gson.JsonToken;
import net.fabricmc.loader.impl.metadata.LoaderModMetadata;
import net.fabricmc.loader.impl.metadata.ModDependencyImpl;
import net.fabricmc.loader.impl.metadata.ParseMetadataException;
import net.fabricmc.loader.impl.util.LoaderUtil;

public final class DependencyOverrides {
    private final Map<String, List<Entry>> dependencyOverrides;

    public DependencyOverrides(Path configDir) {
        Path path = configDir.resolve("fabric_loader_dependencies.json");
        if (!Files.exists(path, new LinkOption[0])) {
            this.dependencyOverrides = Collections.emptyMap();
            return;
        }
        try (JsonReader reader = new JsonReader(new InputStreamReader(Files.newInputStream(path, new OpenOption[0]), StandardCharsets.UTF_8));){
            this.dependencyOverrides = DependencyOverrides.parse(reader);
        }
        catch (IOException | ParseMetadataException e) {
            throw FormattedException.ofLocalized("exception.parsingOverride", "Failed to parse " + LoaderUtil.normalizePath(path), e);
        }
    }

    private static Map<String, List<Entry>> parse(JsonReader reader) throws ParseMetadataException, IOException {
        if (reader.peek() != JsonToken.BEGIN_OBJECT) {
            throw new ParseMetadataException("Root must be an object", reader);
        }
        HashMap<String, List<Entry>> ret = new HashMap<String, List<Entry>>();
        reader.beginObject();
        if (!reader.nextName().equals("version")) {
            throw new ParseMetadataException("First key must be \"version\"", reader);
        }
        if (reader.peek() != JsonToken.NUMBER || reader.nextInt() != 1) {
            throw new ParseMetadataException("Unsupported \"version\", must be 1", reader);
        }
        while (reader.hasNext()) {
            String key = reader.nextName();
            if ("overrides".equals(key)) {
                reader.beginObject();
                while (reader.hasNext()) {
                    String modId = reader.nextName();
                    ret.put(modId, DependencyOverrides.readKeys(reader));
                }
                reader.endObject();
                continue;
            }
            throw new ParseMetadataException("Unsupported root key: " + key, reader);
        }
        reader.endObject();
        return ret;
    }

    private static List<Entry> readKeys(JsonReader reader) throws IOException, ParseMetadataException {
        if (reader.peek() != JsonToken.BEGIN_OBJECT) {
            throw new ParseMetadataException("Dependency container must be an object!", reader);
        }
        EnumMap<ModDependency.Kind, Map> modOverrides = new EnumMap<ModDependency.Kind, Map>(ModDependency.Kind.class);
        reader.beginObject();
        while (reader.hasNext()) {
            String key = reader.nextName();
            Operation op = null;
            for (Operation o : Operation.VALUES) {
                if (!key.startsWith(o.operator)) continue;
                op = o;
                key = key.substring(o.operator.length());
                break;
            }
            assert (op != null);
            ModDependency.Kind kind = ModDependency.Kind.parse(key);
            if (kind == null) {
                throw new ParseMetadataException(String.format("%s is not an allowed dependency key, must be one of: %s", key, Arrays.stream(ModDependency.Kind.values()).map(ModDependency.Kind::getKey).collect(Collectors.joining(", "))), reader);
            }
            List<ModDependency> deps = DependencyOverrides.readDependencies(reader, kind);
            if (deps.isEmpty() && op != Operation.REPLACE) continue;
            modOverrides.computeIfAbsent(kind, ignore -> new EnumMap(Operation.class)).put(op, deps);
        }
        reader.endObject();
        ArrayList<Entry> ret = new ArrayList<Entry>();
        for (Map.Entry entry : modOverrides.entrySet()) {
            ModDependency.Kind kind = (ModDependency.Kind)((Object)entry.getKey());
            Map map = (Map)entry.getValue();
            List values = (List)map.get((Object)Operation.REPLACE);
            if (values != null) {
                ret.add(new Entry(Operation.REPLACE, kind, values));
                continue;
            }
            values = (List)map.get((Object)Operation.REMOVE);
            if (values != null) {
                ret.add(new Entry(Operation.REMOVE, kind, values));
            }
            if ((values = (List)map.get((Object)Operation.ADD)) == null) continue;
            ret.add(new Entry(Operation.ADD, kind, values));
        }
        return ret;
    }

    private static List<ModDependency> readDependencies(JsonReader reader, ModDependency.Kind kind) throws IOException, ParseMetadataException {
        if (reader.peek() != JsonToken.BEGIN_OBJECT) {
            throw new ParseMetadataException("Dependency container must be an object!", reader);
        }
        ArrayList<ModDependency> ret = new ArrayList<ModDependency>();
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
                ret.add(new ModDependencyImpl(kind, modId, matcherStringList));
            }
            catch (VersionParsingException e) {
                throw new ParseMetadataException(e);
            }
        }
        reader.endObject();
        return ret;
    }

    public void apply(LoaderModMetadata metadata) {
        if (this.dependencyOverrides.isEmpty()) {
            return;
        }
        List<Entry> modOverrides = this.dependencyOverrides.get(metadata.getId());
        if (modOverrides == null) {
            return;
        }
        ArrayList<ModDependency> deps = new ArrayList<ModDependency>(metadata.getDependencies());
        block5: for (Entry entry : modOverrides) {
            switch (entry.operation.ordinal()) {
                case 2: {
                    ModDependency dep;
                    Iterator it = deps.iterator();
                    while (it.hasNext()) {
                        dep = (ModDependency)it.next();
                        if (dep.getKind() != entry.kind) continue;
                        it.remove();
                    }
                    deps.addAll(entry.values);
                    break;
                }
                case 1: {
                    ModDependency dep;
                    Iterator it = deps.iterator();
                    block7: while (it.hasNext()) {
                        dep = (ModDependency)it.next();
                        if (dep.getKind() != entry.kind) continue;
                        for (ModDependency value : entry.values) {
                            if (!value.getModId().equals(dep.getModId())) continue;
                            it.remove();
                            continue block7;
                        }
                    }
                    continue block5;
                }
                case 0: {
                    deps.addAll(entry.values);
                }
            }
        }
        metadata.setDependencies(deps);
    }

    public Collection<String> getAffectedModIds() {
        return this.dependencyOverrides.keySet();
    }

    private static enum Operation {
        ADD("+"),
        REMOVE("-"),
        REPLACE("");

        static final Operation[] VALUES;
        final String operator;

        private Operation(String operator) {
            this.operator = operator;
        }

        static {
            VALUES = Operation.values();
        }
    }

    private static final class Entry {
        final Operation operation;
        final ModDependency.Kind kind;
        final List<ModDependency> values;

        Entry(Operation operation, ModDependency.Kind kind, List<ModDependency> values) {
            this.operation = operation;
            this.kind = kind;
            this.values = values;
        }
    }
}

