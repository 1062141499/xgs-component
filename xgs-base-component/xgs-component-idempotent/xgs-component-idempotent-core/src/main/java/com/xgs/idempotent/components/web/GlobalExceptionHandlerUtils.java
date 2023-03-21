package com.xgs.idempotent.components.web;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GlobalExceptionHandler辅助工具类，
 * 组件用于注册异常处理器
 * @author xiongguoshuang
 */
public class GlobalExceptionHandlerUtils {

    private static Map<Class<? extends Throwable>, CustomExceptionHandler> map = new ConcurrentHashMap<>();

    /**
     * 自定义的异常处理器
     */
    public static interface CustomExceptionHandler<T extends Throwable>{
        ApiResponse handler(T throwable);
    }

    public static <T extends Throwable> void registerCustomExceptionHandler(Class<T> exClazz, CustomExceptionHandler<T> customExceptionHandler){
        map.put(exClazz,customExceptionHandler);
    }

    public static <T extends Throwable> boolean hasRegistered(Class<T> exClazz){
        if(exClazz == null){
            return false;
        }
        for (Class<? extends Throwable> aClass : map.keySet()) {
            if(exClazz == aClass){
                return true;
            }
        }
        return false;
    }

    public static CustomExceptionHandler getCustomExceptionHandler(Throwable  throwable){
        if(throwable == null){
            return null;
        }
        return map.get(throwable.getClass());
    }
}
