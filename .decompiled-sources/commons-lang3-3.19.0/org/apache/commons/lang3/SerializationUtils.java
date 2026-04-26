/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Objects;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.SerializationException;

public class SerializationUtils {
    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static <T extends Serializable> T clone(T object) {
        if (object == null) {
            return null;
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(SerializationUtils.serialize(object));
        Class<T> cls = ObjectUtils.getClass(object);
        try (ClassLoaderAwareObjectInputStream in = new ClassLoaderAwareObjectInputStream(bais, cls.getClassLoader());){
            Serializable serializable = (Serializable)in.readObject();
            return (T)serializable;
        }
        catch (IOException | ClassNotFoundException ex) {
            throw new SerializationException(String.format("%s while reading cloned object data", ex.getClass().getSimpleName()), ex);
        }
    }

    public static <T> T deserialize(byte[] objectData) {
        Objects.requireNonNull(objectData, "objectData");
        return SerializationUtils.deserialize(new ByteArrayInputStream(objectData));
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static <T> T deserialize(InputStream inputStream) {
        Objects.requireNonNull(inputStream, "inputStream");
        try (ObjectInputStream in = new ObjectInputStream(inputStream);){
            Object obj;
            Object object = obj = in.readObject();
            return (T)object;
        }
        catch (IOException | ClassNotFoundException | NegativeArraySizeException ex) {
            throw new SerializationException(ex);
        }
    }

    public static <T extends Serializable> T roundtrip(T obj) {
        return (T)((Serializable)SerializationUtils.deserialize(SerializationUtils.serialize(obj)));
    }

    public static byte[] serialize(Serializable obj) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
        SerializationUtils.serialize(obj, baos);
        return baos.toByteArray();
    }

    public static void serialize(Serializable obj, OutputStream outputStream) {
        Objects.requireNonNull(outputStream, "outputStream");
        try (ObjectOutputStream out = new ObjectOutputStream(outputStream);){
            out.writeObject(obj);
        }
        catch (IOException ex) {
            throw new SerializationException(ex);
        }
    }

    @Deprecated
    public SerializationUtils() {
    }

    static final class ClassLoaderAwareObjectInputStream
    extends ObjectInputStream {
        private final ClassLoader classLoader;

        ClassLoaderAwareObjectInputStream(InputStream in, ClassLoader classLoader) throws IOException {
            super(in);
            this.classLoader = classLoader;
        }

        @Override
        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            String name = desc.getName();
            try {
                return Class.forName(name, false, this.classLoader);
            }
            catch (ClassNotFoundException ex) {
                try {
                    return Class.forName(name, false, Thread.currentThread().getContextClassLoader());
                }
                catch (ClassNotFoundException cnfe) {
                    Class<?> cls = ClassUtils.getPrimitiveClass(name);
                    if (cls != null) {
                        return cls;
                    }
                    throw cnfe;
                }
            }
        }
    }
}

