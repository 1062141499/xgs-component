package com.xgs.idempotent.jdbc.service;

import com.xgs.idempotent.components.IdempotentRecord;
import com.xgs.idempotent.jdbc.pojo.JdbcIdempotentRecord;
import com.xgs.idempotent.jdbc.serializer.ArgsBytesSerializer;
import com.xgs.idempotent.jdbc.serializer.JdkSerializer;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * @author xiongguoshuang
 */
public class JdbcIdempotentRecordService {

    private final ArgsBytesSerializer argsBytesSerializer;


    public JdbcIdempotentRecordService(ArgsBytesSerializer argsBytesSerializer){
        this.argsBytesSerializer = argsBytesSerializer;
    }

    private String clazzListToString(Class<?>[] clazzList){
        StringBuilder stringBuilder = new StringBuilder();
        for(Class<?> clazz: clazzList){
            stringBuilder.append(clazz.getName()).append(";");
        }
        return stringBuilder.toString();
    }


    public JdbcIdempotentRecord toJdbcIdempotentRecord(IdempotentRecord idempotentRecord){
        JdbcIdempotentRecord jdbcIdempotentRecord = new JdbcIdempotentRecord();
        jdbcIdempotentRecord.setKey(idempotentRecord.getKey());

        Object[] parameterValuesObj = idempotentRecord.getParameterValues();
        byte[] parameterValues = argsBytesSerializer.serialize(parameterValuesObj);
        jdbcIdempotentRecord.setParameterValues(parameterValues);
        List<Class> classList = parameterValuesObj == null ? Collections.emptyList():
                Stream.of(parameterValuesObj)
                .map(Object::getClass)
                .collect(Collectors.toList());
        Class[] clazzArray = new Class[classList.size()];
        clazzArray = classList.toArray(clazzArray);
        jdbcIdempotentRecord.setParameterTypes(clazzListToString(
                clazzArray
        ));
        byte[] result = JdkSerializer.convert(idempotentRecord.getResult());
        jdbcIdempotentRecord.setResult(result);
        byte[] throwable  =  JdkSerializer.convert(idempotentRecord.getThrowable());
        jdbcIdempotentRecord.setThrowable(throwable);
        jdbcIdempotentRecord.setSuccessCount(idempotentRecord.getSuccessCount());
        jdbcIdempotentRecord.setFailCount(idempotentRecord.getFailCount());
        jdbcIdempotentRecord.setCreateTime(new Date(idempotentRecord.getCreateTime()));
        jdbcIdempotentRecord.setUpdateTime(new Date(idempotentRecord.getUpdateTime()));
        return jdbcIdempotentRecord;

    }

    public IdempotentRecord toIdempotentRecord(JdbcIdempotentRecord redisIdempotentRecord){
        IdempotentRecord idempotentRecord = new IdempotentRecord();
        idempotentRecord.setKey(redisIdempotentRecord.getKey());

        Object[] parameterValuesObj = argsBytesSerializer.deserialize(redisIdempotentRecord.getParameterValues());
        idempotentRecord.setParameterValues(parameterValuesObj);
        Object result = JdkSerializer.convert(redisIdempotentRecord.getResult());
        idempotentRecord.setResult(result);
        Throwable throwable  = (Throwable)JdkSerializer.convert(redisIdempotentRecord.getThrowable());
        idempotentRecord.setThrowable(throwable);
        idempotentRecord.setSuccessCount(idempotentRecord.getSuccessCount());
        idempotentRecord.setFailCount(redisIdempotentRecord.getFailCount());
        idempotentRecord.setCreateTime(redisIdempotentRecord.getCreateTime().getTime());
        idempotentRecord.setUpdateTime(redisIdempotentRecord.getUpdateTime().getTime());
        return idempotentRecord;
    }
}
