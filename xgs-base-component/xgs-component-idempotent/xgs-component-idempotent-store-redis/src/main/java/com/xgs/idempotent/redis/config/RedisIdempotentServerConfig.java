package com.xgs.idempotent.redis.config;

import com.xgs.idempotent.components.IdempotentRecordStore;
import com.xgs.idempotent.constants.IdempotentErrorCodeEnum;
import com.xgs.idempotent.exception.IdempotentException;
import com.xgs.idempotent.inner.IdempotentPropertiesAutoConfiguration;
import com.xgs.idempotent.redis.RedisIdemConstants;
import com.xgs.idempotent.redis.components.RedisIdempotentRecordStore;
import com.xgs.idempotent.redis.service.RedisIdempotentRecordService;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author xiongguoshuang
 */
@Configuration(/*proxyBeanMethods = false*/)
@Import({IdempotentPropertiesAutoConfiguration.class, IdempotentRedisPropertiesAutoConfiguration.class})
@AutoConfigureAfter({RedisAutoConfiguration.class, IdempotentRedisPropertiesAutoConfiguration.class})
public class RedisIdempotentServerConfig {


    public static final String BEAN_REDIS_IDEMPOTENT_RECORD_SERVICE = "redisIdempotent";


    @Bean(BEAN_REDIS_IDEMPOTENT_RECORD_SERVICE)
    public RedisIdempotentRecordService redisIdempotentRecordService(){
        return new RedisIdempotentRecordService();
    }

    /**
     * 根据配置初始化redisson连接
     * @param idempotentRedisRedissonSingleServerProperties
     * @return
     */
    private RedissonClient redissonClient(
            IdempotentRedisRedissonSingleServerProperties idempotentRedisRedissonSingleServerProperties
    ){

        // 1. Create config object
        Config config = new Config();
        SingleServerConfig singleServerConfig = config.useSingleServer();
        singleServerConfig.setAddress(idempotentRedisRedissonSingleServerProperties.getAddress());
        if(StringUtils.isNotBlank(idempotentRedisRedissonSingleServerProperties.getPassword())){
            singleServerConfig.setPassword(idempotentRedisRedissonSingleServerProperties.getPassword());
        }

        RedissonClient redissonClient = null;
        try{
            redissonClient = Redisson.create(config);
        }catch (Throwable throwable){
            throw IdempotentException.errorWithArguments(
                    IdempotentErrorCodeEnum.REDIS_CONNECT_FAIL.getCode(),
                    "创建redisson连接失败",
                    idempotentRedisRedissonSingleServerProperties
            );
        }
        return redissonClient;
    }

    @Bean(RedisIdemConstants.BEAN_REDIS_IDEMPOTENT_RECORD_REPOSITORY_FOR_REDISSON)
    @ConditionalOnBean(IdempotentRedisRedissonSingleServerProperties.class)
    public IdempotentRecordStore idempotentRecordRepositoryForRedisson(
            @Qualifier(BEAN_REDIS_IDEMPOTENT_RECORD_SERVICE) RedisIdempotentRecordService redisIdempotentRecordService,
            IdempotentRedisRedissonSingleServerProperties idempotentRedisRedissonSingleServerProperties
    ){

        RedissonClient redissonClient = redissonClient(idempotentRedisRedissonSingleServerProperties);
        IdempotentRecordStore redisIdempotentRecordStore = new RedisIdempotentRecordStore(
                redissonClient,
                redisIdempotentRecordService
        );
        return redisIdempotentRecordStore;
    }

}
