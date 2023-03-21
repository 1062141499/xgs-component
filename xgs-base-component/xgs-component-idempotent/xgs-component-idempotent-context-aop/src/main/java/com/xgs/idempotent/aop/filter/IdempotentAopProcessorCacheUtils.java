package com.xgs.idempotent.aop.filter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description: IdempotentProcessor 缓存工具类
 * @author: xiongguoshuang
 * @date: 2023/02/19
 */
public class IdempotentAopProcessorCacheUtils {

    /**
     * key -- moduleName
     */
    private static final Map<String, IdempotentAopRequestProcessor> idempotentProcessorMap = new ConcurrentHashMap<>();

    public static IdempotentAopRequestProcessor getByModuleName(String moduleName){
        return idempotentProcessorMap.get(moduleName);
    }

    public static void putModuleName(String moduleName, IdempotentAopRequestProcessor idempotentProcessor){
        idempotentProcessorMap.put(moduleName, idempotentProcessor);
    }

}
