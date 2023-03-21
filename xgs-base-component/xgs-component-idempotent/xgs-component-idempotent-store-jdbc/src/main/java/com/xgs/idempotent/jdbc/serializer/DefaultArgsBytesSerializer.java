package com.xgs.idempotent.jdbc.serializer;

/**
 * @author xiongguoshuang
 * @date 2023-02-19
 */
public class DefaultArgsBytesSerializer implements ArgsBytesSerializer {

    @Override
    public byte[] serialize(Object[] args){
        ArgsWrapper wrapper = ArgsWrapper.builder()
                .args(args)
                .build();

        return JdkSerializer.convert(wrapper);
    }

    @Override
    public Object[] deserialize(byte[] bytes){
        if(bytes == null){
            return null;
        }
        ArgsWrapper data = (ArgsWrapper) JdkSerializer.convert(bytes);
        return data.getArgs();
    }
}
