package com.xgs.idempotent.aop;

import com.xgs.idempotent.aop.config.IdempotentAopProperties;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter({AopAutoConfiguration.class})
public class IdempotentAopPropertiesAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(IdempotentAopProperties.class)
    @ConfigurationProperties(prefix = "idempotent.aop")
    @RefreshScope
    public IdempotentAopProperties idempotentAopProperties(){
        return new IdempotentAopProperties();
    }
}
