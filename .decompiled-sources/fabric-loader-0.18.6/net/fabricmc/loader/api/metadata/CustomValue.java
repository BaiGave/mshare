/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.api.metadata;

import java.util.Map;

public interface CustomValue {
    public CvType getType();

    public CvObject getAsObject();

    public CvArray getAsArray();

    public String getAsString();

    public Number getAsNumber();

    public boolean getAsBoolean();

    public static enum CvType {
        OBJECT,
        ARRAY,
        STRING,
        NUMBER,
        BOOLEAN,
        NULL;

    }

    public static interface CvArray
    extends Iterable<CustomValue>,
    CustomValue {
        public int size();

        public CustomValue get(int var1);
    }

    public static interface CvObject
    extends Iterable<Map.Entry<String, CustomValue>>,
    CustomValue {
        public int size();

        public boolean containsKey(String var1);

        public CustomValue get(String var1);
    }
}

