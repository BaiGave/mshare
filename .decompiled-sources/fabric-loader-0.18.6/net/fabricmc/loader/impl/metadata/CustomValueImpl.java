/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.metadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.impl.lib.gson.JsonReader;
import net.fabricmc.loader.impl.metadata.ParseMetadataException;

abstract class CustomValueImpl
implements CustomValue {
    static final CustomValue BOOLEAN_TRUE = new BooleanImpl(true);
    static final CustomValue BOOLEAN_FALSE = new BooleanImpl(false);
    static final CustomValue NULL = new NullImpl();

    CustomValueImpl() {
    }

    public static CustomValue readCustomValue(JsonReader reader) throws IOException, ParseMetadataException {
        switch (reader.peek()) {
            case BEGIN_OBJECT: {
                reader.beginObject();
                LinkedHashMap<String, CustomValue> values = new LinkedHashMap<String, CustomValue>();
                while (reader.hasNext()) {
                    values.put(reader.nextName(), CustomValueImpl.readCustomValue(reader));
                }
                reader.endObject();
                return new ObjectImpl(values);
            }
            case BEGIN_ARRAY: {
                reader.beginArray();
                ArrayList<CustomValue> entries = new ArrayList<CustomValue>();
                while (reader.hasNext()) {
                    entries.add(CustomValueImpl.readCustomValue(reader));
                }
                reader.endArray();
                return new ArrayImpl(entries);
            }
            case STRING: {
                return new StringImpl(reader.nextString());
            }
            case NUMBER: {
                return new NumberImpl(reader.nextDouble());
            }
            case BOOLEAN: {
                if (reader.nextBoolean()) {
                    return BOOLEAN_TRUE;
                }
                return BOOLEAN_FALSE;
            }
            case NULL: {
                reader.nextNull();
                return NULL;
            }
        }
        throw new ParseMetadataException(Objects.toString(reader.nextName()), reader);
    }

    @Override
    public final CustomValue.CvObject getAsObject() {
        if (this instanceof ObjectImpl) {
            return (ObjectImpl)this;
        }
        throw new ClassCastException("can't convert " + this.getType().name() + " to Object");
    }

    @Override
    public final CustomValue.CvArray getAsArray() {
        if (this instanceof ArrayImpl) {
            return (ArrayImpl)this;
        }
        throw new ClassCastException("can't convert " + this.getType().name() + " to Array");
    }

    @Override
    public final String getAsString() {
        if (this instanceof StringImpl) {
            return ((StringImpl)this).value;
        }
        throw new ClassCastException("can't convert " + this.getType().name() + " to String");
    }

    @Override
    public final Number getAsNumber() {
        if (this instanceof NumberImpl) {
            return ((NumberImpl)this).value;
        }
        throw new ClassCastException("can't convert " + this.getType().name() + " to Number");
    }

    @Override
    public final boolean getAsBoolean() {
        if (this instanceof BooleanImpl) {
            return ((BooleanImpl)this).value;
        }
        throw new ClassCastException("can't convert " + this.getType().name() + " to Boolean");
    }

    private static final class ObjectImpl
    extends CustomValueImpl
    implements CustomValue.CvObject {
        private final Map<String, CustomValue> entries;

        ObjectImpl(Map<String, CustomValue> entries) {
            this.entries = Collections.unmodifiableMap(entries);
        }

        @Override
        public CustomValue.CvType getType() {
            return CustomValue.CvType.OBJECT;
        }

        @Override
        public int size() {
            return this.entries.size();
        }

        @Override
        public boolean containsKey(String key) {
            return this.entries.containsKey(key);
        }

        @Override
        public CustomValue get(String key) {
            return this.entries.get(key);
        }

        @Override
        public Iterator<Map.Entry<String, CustomValue>> iterator() {
            return this.entries.entrySet().iterator();
        }
    }

    private static final class ArrayImpl
    extends CustomValueImpl
    implements CustomValue.CvArray {
        private final List<CustomValue> entries;

        ArrayImpl(List<CustomValue> entries) {
            this.entries = Collections.unmodifiableList(entries);
        }

        @Override
        public CustomValue.CvType getType() {
            return CustomValue.CvType.ARRAY;
        }

        @Override
        public int size() {
            return this.entries.size();
        }

        @Override
        public CustomValue get(int index) {
            return this.entries.get(index);
        }

        @Override
        public Iterator<CustomValue> iterator() {
            return this.entries.iterator();
        }
    }

    private static final class StringImpl
    extends CustomValueImpl {
        final String value;

        StringImpl(String value) {
            this.value = value;
        }

        @Override
        public CustomValue.CvType getType() {
            return CustomValue.CvType.STRING;
        }
    }

    private static final class NumberImpl
    extends CustomValueImpl {
        final Number value;

        NumberImpl(Number value) {
            this.value = value;
        }

        @Override
        public CustomValue.CvType getType() {
            return CustomValue.CvType.NUMBER;
        }
    }

    private static final class BooleanImpl
    extends CustomValueImpl {
        final boolean value;

        BooleanImpl(boolean value) {
            this.value = value;
        }

        @Override
        public CustomValue.CvType getType() {
            return CustomValue.CvType.BOOLEAN;
        }
    }

    private static final class NullImpl
    extends CustomValueImpl {
        private NullImpl() {
        }

        @Override
        public CustomValue.CvType getType() {
            return CustomValue.CvType.NULL;
        }
    }
}

