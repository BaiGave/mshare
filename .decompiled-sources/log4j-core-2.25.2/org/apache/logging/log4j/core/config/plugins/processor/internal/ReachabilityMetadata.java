/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config.plugins.processor.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.IntStream;
import org.apache.logging.log4j.core.util.JsonUtils;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class ReachabilityMetadata {
    public static final String FIELD_OR_METHOD_NAME = "name";
    public static final String PARAMETER_TYPES = "parameterTypes";
    public static final String TYPE_NAME = "name";
    public static final String FIELDS = "fields";
    public static final String METHODS = "methods";

    public static void writeReflectConfig(Collection<Type> types, OutputStream output) throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);){
            Reflection reflection = new Reflection(types);
            MinimalJsonWriter jsonWriter = new MinimalJsonWriter(writer);
            reflection.toJson(jsonWriter);
        }
    }

    private ReachabilityMetadata() {
    }

    public static final class Reflection {
        private final Collection<Type> types = new TreeSet<Type>(Comparator.comparing(Type::getType));

        public Reflection(Collection<Type> types) {
            this.types.addAll(types);
        }

        void toJson(MinimalJsonWriter jsonWriter) throws IOException {
            boolean first = true;
            jsonWriter.writeArrayStart();
            for (Type type : this.types) {
                if (!first) {
                    jsonWriter.writeSeparator();
                }
                first = false;
                type.toJson(jsonWriter);
            }
            jsonWriter.writeArrayEnd();
        }
    }

    private static final class MinimalJsonWriter {
        private final Appendable output;

        public MinimalJsonWriter(Appendable output) {
            this.output = output;
        }

        public void writeString(CharSequence input) throws IOException {
            this.output.append('\"');
            StringBuilder sb = new StringBuilder();
            JsonUtils.quoteAsString(input, sb);
            this.output.append(sb);
            this.output.append('\"');
        }

        public void writeObjectStart() throws IOException {
            this.output.append('{');
        }

        public void writeObjectEnd() throws IOException {
            this.output.append('}');
        }

        public void writeObjectKey(CharSequence key) throws IOException {
            this.writeString(key);
            this.output.append(':');
        }

        public void writeArrayStart() throws IOException {
            this.output.append('[');
        }

        public void writeSeparator() throws IOException {
            this.output.append(',');
        }

        public void writeArrayEnd() throws IOException {
            this.output.append(']');
        }
    }

    public static final class Type {
        private final String type;
        private final Collection<Method> methods = new TreeSet<Method>();
        private final Collection<Field> fields = new TreeSet<Field>();

        public Type(String type) {
            this.type = type;
        }

        public String getType() {
            return this.type;
        }

        public void addMethod(Method method) {
            this.methods.add(method);
        }

        public void addField(Field field) {
            this.fields.add(field);
        }

        void toJson(MinimalJsonWriter jsonWriter) throws IOException {
            jsonWriter.writeObjectStart();
            jsonWriter.writeObjectKey("name");
            jsonWriter.writeString(this.type);
            jsonWriter.writeSeparator();
            boolean first = true;
            jsonWriter.writeObjectKey(ReachabilityMetadata.METHODS);
            jsonWriter.writeArrayStart();
            for (Method method : this.methods) {
                if (!first) {
                    jsonWriter.writeSeparator();
                }
                first = false;
                method.toJson(jsonWriter);
            }
            jsonWriter.writeArrayEnd();
            jsonWriter.writeSeparator();
            first = true;
            jsonWriter.writeObjectKey(ReachabilityMetadata.FIELDS);
            jsonWriter.writeArrayStart();
            for (Field field : this.fields) {
                if (!first) {
                    jsonWriter.writeSeparator();
                }
                first = false;
                field.toJson(jsonWriter);
            }
            jsonWriter.writeArrayEnd();
            jsonWriter.writeObjectEnd();
        }
    }

    public static final class Method
    implements Comparable<Method> {
        private final String name;
        private final List<String> parameterTypes = new ArrayList<String>();

        public Method(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public void addParameterType(String parameterType) {
            this.parameterTypes.add(parameterType);
        }

        void toJson(MinimalJsonWriter jsonWriter) throws IOException {
            jsonWriter.writeObjectStart();
            jsonWriter.writeObjectKey("name");
            jsonWriter.writeString(this.name);
            jsonWriter.writeSeparator();
            jsonWriter.writeObjectKey(ReachabilityMetadata.PARAMETER_TYPES);
            jsonWriter.writeArrayStart();
            boolean first = true;
            for (String parameterType : this.parameterTypes) {
                if (!first) {
                    jsonWriter.writeSeparator();
                }
                first = false;
                jsonWriter.writeString(parameterType);
            }
            jsonWriter.writeArrayEnd();
            jsonWriter.writeObjectEnd();
        }

        @Override
        public int compareTo(Method other) {
            int result = this.name.compareTo(other.name);
            if (result == 0) {
                result = this.parameterTypes.size() - other.parameterTypes.size();
            }
            if (result == 0) {
                result = IntStream.range(0, this.parameterTypes.size()).map(idx -> this.parameterTypes.get(idx).compareTo(other.parameterTypes.get(idx))).filter(r -> r != 0).findFirst().orElse(0);
            }
            return result;
        }
    }

    public static final class Field
    implements Comparable<Field> {
        private final String name;

        public Field(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        void toJson(MinimalJsonWriter jsonWriter) throws IOException {
            jsonWriter.writeObjectStart();
            jsonWriter.writeObjectKey("name");
            jsonWriter.writeString(this.name);
            jsonWriter.writeObjectEnd();
        }

        @Override
        public int compareTo(Field other) {
            return this.name.compareTo(other.name);
        }
    }
}

