/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util.internal;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.FilteredObjectInputStream;

public final class SerializationUtil {
    private static final String DEFAULT_FILTER_CLASS = "org.apache.logging.log4j.util.internal.DefaultObjectInputFilter";
    private static final Method setObjectInputFilter;
    private static final Method getObjectInputFilter;
    private static final Method newObjectInputFilter;
    public static final List<String> REQUIRED_JAVA_CLASSES;
    public static final List<String> REQUIRED_JAVA_PACKAGES;

    public static void writeWrappedObject(Serializable obj, ObjectOutputStream out) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bout);){
            oos.writeObject(obj);
            oos.flush();
            out.writeObject(bout.toByteArray());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SuppressFBWarnings(value={"OBJECT_DESERIALIZATION"}, justification="Object deserialization uses either Java 9 native filter or our custom filter to limit the kinds of classes deserialized.")
    public static Object readWrappedObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream ois;
        SerializationUtil.assertFiltered(in);
        byte[] data = (byte[])in.readObject();
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        if (in instanceof FilteredObjectInputStream) {
            ois = new FilteredObjectInputStream(bin, ((FilteredObjectInputStream)in).getAllowedClasses());
        } else {
            try {
                Object obj = getObjectInputFilter.invoke((Object)in, new Object[0]);
                Object filter = newObjectInputFilter.invoke(null, obj);
                ois = new ObjectInputStream(bin);
                setObjectInputFilter.invoke((Object)ois, filter);
            }
            catch (IllegalAccessException | InvocationTargetException ex) {
                throw new StreamCorruptedException("Unable to set ObjectInputFilter on stream");
            }
        }
        try {
            Object ex = ois.readObject();
            return ex;
        }
        catch (Exception | LinkageError e) {
            StatusLogger.getLogger().warn("Ignoring {} during deserialization", (Object)e.getMessage());
            Object var5_7 = null;
            return var5_7;
        }
        finally {
            ois.close();
        }
    }

    public static void assertFiltered(ObjectInputStream stream) {
        if (!(stream instanceof FilteredObjectInputStream) && setObjectInputFilter == null) {
            throw new IllegalArgumentException("readObject requires a FilteredObjectInputStream or an ObjectInputStream that accepts an ObjectInputFilter");
        }
    }

    public static String stripArray(Class<?> clazz) {
        Class<?> currentClazz = clazz;
        while (currentClazz.isArray()) {
            currentClazz = currentClazz.getComponentType();
        }
        return currentClazz.getName();
    }

    public static String stripArray(String name) {
        int offset = name.lastIndexOf(91) + 1;
        if (offset == 0) {
            return name;
        }
        if (name.charAt(offset) == 'L') {
            return name.substring(offset + 1, name.length() - 1);
        }
        switch (name.substring(offset)) {
            case "Z": {
                return "boolean";
            }
            case "B": {
                return "byte";
            }
            case "C": {
                return "char";
            }
            case "D": {
                return "double";
            }
            case "F": {
                return "float";
            }
            case "I": {
                return "int";
            }
            case "J": {
                return "long";
            }
            case "S": {
                return "short";
            }
        }
        throw new IllegalArgumentException("Unsupported array class signature '" + name + "'");
    }

    private SerializationUtil() {
    }

    static {
        Method newMethod;
        Method getMethod;
        Method setMethod;
        block5: {
            Method[] methods = ObjectInputStream.class.getMethods();
            setMethod = null;
            getMethod = null;
            for (Method method : methods) {
                if (method.getName().equals("setObjectInputFilter")) {
                    setMethod = method;
                    continue;
                }
                if (!method.getName().equals("getObjectInputFilter")) continue;
                getMethod = method;
            }
            newMethod = null;
            try {
                if (setMethod == null) break block5;
                Class<?> clazz = Class.forName(DEFAULT_FILTER_CLASS);
                for (Method method : methods = clazz.getMethods()) {
                    if (!method.getName().equals("newInstance") || !Modifier.isStatic(method.getModifiers())) continue;
                    newMethod = method;
                    break;
                }
            }
            catch (ClassNotFoundException classNotFoundException) {
                // empty catch block
            }
        }
        newObjectInputFilter = newMethod;
        setObjectInputFilter = setMethod;
        getObjectInputFilter = getMethod;
        REQUIRED_JAVA_CLASSES = Arrays.asList("java.math.BigDecimal", "java.math.BigInteger", "java.rmi.MarshalledObject", "boolean", "byte", "char", "double", "float", "int", "long", "short");
        REQUIRED_JAVA_PACKAGES = Arrays.asList("java.lang.", "java.time.", "java.util.", "org.apache.logging.log4j.");
    }
}

