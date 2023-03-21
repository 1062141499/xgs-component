package com.xgs.idempotent.redis;

import com.xgs.idempotent.exception.IdempotentException;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scripting.support.ResourceScriptSource;

@Slf4j
public class ResourceScriptSourceUtils {


    /**
     * key -- classpath
     */
    private static Map<String, ResourceScriptSource> CACHE_MAP = new ConcurrentHashMap<>();


    public static ResourceScriptSource get(String classpath){
        return CACHE_MAP.computeIfAbsent(classpath, key->{
            ResourceScriptSource resourceScriptSource =
                    new ResourceScriptSource(new ClassPathResource(key));
            return resourceScriptSource;
        });
    }


    public static String getScriptAsString(ResourceScriptSource resourceScriptSource){
        if(resourceScriptSource == null){
            return null;
        }
        try {
            return resourceScriptSource.getScriptAsString();
        } catch (IOException e) {
            throw IdempotentException.wrapThrowable(e);
        }
    }
}
