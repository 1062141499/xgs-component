package com.xgs.idempotent.inner;

import com.xgs.idempotent.config.IdempotentProperties;
import com.xgs.idempotent.constants.IdempotentConstant;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter({AopAutoConfiguration.class})
public class IdempotentPropertiesAutoConfiguration {
    @Bean(IdempotentConstant.BEAN_NAME_IDEMPOTENT_PROPERTIES)
    @ConditionalOnMissingBean(IdempotentProperties.class)
    @ConfigurationProperties(prefix = "idempotent.common")
    @RefreshScope
    public IdempotentProperties idempotentProperties(){
        return new IdempotentProperties();
    }
}
