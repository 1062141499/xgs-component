package com.xgs.idempotent.aop;


import com.xgs.idempotent.aop.config.IdempotentAopProperties;
import com.xgs.idempotent.components.IdempotentRecordStore;
import com.xgs.idempotent.components.web.ApiResponse;
import com.xgs.idempotent.components.web.GlobalExceptionHandlerUtils;
import com.xgs.idempotent.config.IdempotentProperties;
import com.xgs.idempotent.aop.filter.HttpIdempotentAspect;
import com.xgs.idempotent.exception.IdempotentException;
import com.xgs.idempotent.inner.IdempotentPropertiesAutoConfiguration;
import java.util.List;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * @description:   限流配置初始化
 * @author: xiongguoshuang
 * @date: 2023/02/28
 **/
@Configuration
@Import({IdempotentPropertiesAutoConfiguration.class, IdempotentAopPropertiesAutoConfiguration.class})
@AutoConfigureAfter({AopAutoConfiguration.class})
public class IdempotentAopAutoConfiguration implements ApplicationContextAware, InitializingBean {

    @Bean
    public IdempotentAnnotationMetaInitializer idempotentHttpService(
            IdempotentProperties idempotentProperties,
            IdempotentRecordStore idempotentRecordStore
    ) {
        List<String> basePackages = AutoConfigurationPackages.get(applicationContext);
        String[] base = basePackages.toArray(new String[]{});
        return new IdempotentAnnotationMetaInitializer(base,idempotentProperties,idempotentRecordStore);
    }


    @Bean
    public HttpIdempotentAspect httpIdempotentAspect(
            IdempotentAopProperties idempotentAopProperties,
            IdempotentAnnotationMetaInitializer idempotentAnnotationMetaInitializer

    ){
        return new HttpIdempotentAspect(
                idempotentAopProperties,
                idempotentAnnotationMetaInitializer
        );
    }


    private ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if(!GlobalExceptionHandlerUtils.hasRegistered(IdempotentException.class)){

            GlobalExceptionHandlerUtils.registerCustomExceptionHandler(
                    IdempotentException.class,
                    (idempotentException)-> ApiResponse.fail(idempotentException.getMessage())
            );
        }

    }
}
