package com.xgs.idempotent.jdbc.serializer;

import org.springframework.core.ConfigurableObjectInputStream;
import org.springframework.core.NestedIOException;
import org.springframework.core.serializer.support.SerializationFailedException;

import java.io.*;

/**
 * @author xiongguoshuang
 * @date 2023-02-21
 */
public class JdkSerializer {

    public static byte[] convert(Object source) {
        if(source == null){
            return null;
        }
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(1024);
        try  {
            serialize(source, byteStream);
            return byteStream.toByteArray();
        }
        catch (Throwable ex) {
            throw new SerializationFailedException("Failed to serialize object using " +
                    JdkSerializer.class.getSimpleName(), ex);
        }
    }

    public static Object convert(byte[] source) {
        if(source == null){
            return null;
        }
        ByteArrayInputStream byteStream = new ByteArrayInputStream(source);
        try {
            return deserialize(byteStream);
        }
        catch (Throwable ex) {
            throw new SerializationFailedException("Failed to deserialize payload. " +
                    "Is the byte array a result of corresponding serialization for " +
                    JdkSerializer.class.getSimpleName() + "?", ex);
        }
    }

    public static void serialize(Object object, OutputStream outputStream) throws IOException {
        if (!(object instanceof Serializable)) {
            throw new IllegalArgumentException(JdkSerializer.class.getSimpleName() + " requires a Serializable payload " +
                    "but received an object of type [" + object.getClass().getName() + "]");
        }
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(object);
        objectOutputStream.flush();
    }

    public static Object deserialize(InputStream inputStream) throws IOException {
        ObjectInputStream objectInputStream = new ConfigurableObjectInputStream(inputStream, null);
        try {
            return objectInputStream.readObject();
        }
        catch (ClassNotFoundException ex) {
            throw new NestedIOException("Failed to deserialize object type", ex);
        }
    }
}
