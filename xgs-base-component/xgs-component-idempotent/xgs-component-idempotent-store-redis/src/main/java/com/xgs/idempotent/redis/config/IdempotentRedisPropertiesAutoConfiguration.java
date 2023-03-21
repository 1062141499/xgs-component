package com.xgs.idempotent.redis.config;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter({AopAutoConfiguration.class})
public class IdempotentRedisPropertiesAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(IdempotentRedisRedissonSingleServerProperties.class)
    @ConfigurationProperties(prefix = "idempotent.redis.redisson.single-server-config")
    @RefreshScope
    public IdempotentRedisRedissonSingleServerProperties idempotentRedisRedissonSingleServerProperties(){
        return new IdempotentRedisRedissonSingleServerProperties();
    }
}
