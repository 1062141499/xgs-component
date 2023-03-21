package com.xgs.idempotent.redis.service;

import com.xgs.idempotent.components.IdempotentRecord;
import com.xgs.idempotent.components.IdempotentRequestContext;
import com.xgs.idempotent.redis.RecordTtlSecsUtils;
import com.xgs.idempotent.redis.pojo.RedisIdempotentRecord;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ClassUtils;


/**
 * @author xiongguoshuang
 */
@Slf4j
public class RedisIdempotentRecordService {

    private String clazzListToString(Class<?>[] clazzList){
        StringBuilder stringBuilder = new StringBuilder();
        for(Class<?> clazz: clazzList){
            stringBuilder.append(clazz.getName()).append(";");
        }
        return stringBuilder.toString();
    }

    private Class<?>[] stringToClazzList(String clazzStr){
        if(clazzStr == null || "".equals(clazzStr)){
            return null;
        }
        String[] strings = clazzStr.split(";");
        Class<?>[] result = new Class[strings.length];
        for(int i=0;i<strings.length;i++){
            try {
                result[0] = ClassUtils.forName(strings[0],Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException e) {
                log.warn("ClassUtils.forName error",e);
                result[0] = null;
            }
        }
        return result;
    }


    public RedisIdempotentRecord toRedisIdempotentRecord(IdempotentRecord idempotentRecord){
        RedisIdempotentRecord redisIdempotentRecord = new RedisIdempotentRecord();
        redisIdempotentRecord.setKey(idempotentRecord.getKey());

        Object[] parameterValuesObj = idempotentRecord.getParameterValues();
        redisIdempotentRecord.setParameterValues(parameterValuesObj);
        List<Class> classList = parameterValuesObj == null ? Collections.emptyList():
                Stream.of(parameterValuesObj)
                .map(Object::getClass)
                .collect(Collectors.toList());
        Class[] clazzArray = new Class[classList.size()];
        clazzArray = classList.toArray(clazzArray);
        redisIdempotentRecord.setParameterTypes(clazzListToString(
                clazzArray
        ));
        redisIdempotentRecord.setResult(idempotentRecord.getResult());
        redisIdempotentRecord.setThrowable(idempotentRecord.getThrowable());
        redisIdempotentRecord.setSuccessCount(idempotentRecord.getSuccessCount());
        redisIdempotentRecord.setFailCount(idempotentRecord.getFailCount());
        redisIdempotentRecord.setCreateTime(idempotentRecord.getCreateTime());
        redisIdempotentRecord.setUpdateTime(idempotentRecord.getUpdateTime());
        return redisIdempotentRecord;

    }

    public IdempotentRecord toIdempotentRecord(RedisIdempotentRecord redisIdempotentRecord, IdempotentRequestContext idempotentRequestContext){
        IdempotentRecord idempotentRecord = new IdempotentRecord();
        idempotentRecord.setKey(redisIdempotentRecord.getKey());


        idempotentRecord.setParameterTypes(stringToClazzList(redisIdempotentRecord.getParameterTypes()));
        Object[] parameterValuesObj = redisIdempotentRecord.getParameterValues();
        idempotentRecord.setParameterValues(parameterValuesObj);
        idempotentRecord.setResult(redisIdempotentRecord.getResult());
        idempotentRecord.setThrowable(redisIdempotentRecord.getThrowable());
        idempotentRecord.setSuccessCount(redisIdempotentRecord.getSuccessCount());
        idempotentRecord.setFailCount(redisIdempotentRecord.getFailCount());
        idempotentRecord.setCreateTime(redisIdempotentRecord.getCreateTime());
        idempotentRecord.setUpdateTime(redisIdempotentRecord.getUpdateTime());
        long recordExpiredSecs = RecordTtlSecsUtils.calRecordTtlSecs(idempotentRequestContext);
        idempotentRecord.setExpireTime(idempotentRecord.getCreateTime() + recordExpiredSecs*1000l);
        return idempotentRecord;


    }
}
