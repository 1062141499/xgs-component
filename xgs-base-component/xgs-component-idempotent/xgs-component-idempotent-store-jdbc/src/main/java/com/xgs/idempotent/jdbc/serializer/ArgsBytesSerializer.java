package com.xgs.idempotent.jdbc.serializer;


/**
 * @author xiongguoshuang
 * @date 2022-02-19
 */
public interface ArgsBytesSerializer {

    byte[] serialize(Object[] args);

    Object[] deserialize(byte[] bytes);
}
