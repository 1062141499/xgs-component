package com.xgs.idempotent.jdbc.config;

import com.xgs.idempotent.components.IdempotentRecordStore;
import com.xgs.idempotent.config.IdempotentProperties;
import com.xgs.idempotent.constants.IdempotentConstant;
import com.xgs.idempotent.inner.IdempotentPropertiesAutoConfiguration;
import com.xgs.idempotent.jdbc.JdbcIdemConstants;
import com.xgs.idempotent.jdbc.components.JdbcIdempotentRecordStore;
import com.xgs.idempotent.jdbc.serializer.DefaultArgsBytesSerializer;
import com.xgs.idempotent.jdbc.service.JdbcIdempotentRecordService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author xiongguoshuang
 */
@Configuration(/*proxyBeanMethods = false*/)
@Import(IdempotentPropertiesAutoConfiguration.class)
@AutoConfigureAfter({JdbcTemplateAutoConfiguration.class})
public class JdbcIdempotentServerConfig {

    public static final String BEAN_JDBC_IDEMPOTENT_RECORD_SERVICE = "jdbcIdempotent";

    @Bean(BEAN_JDBC_IDEMPOTENT_RECORD_SERVICE)
    public JdbcIdempotentRecordService jdbcIdempotentRecordService(){
        return new JdbcIdempotentRecordService(new DefaultArgsBytesSerializer());
    }


    @Bean(JdbcIdemConstants.BEAN_JDBC_IDEMPOTENT_RECORD_REPOSITORY)
    public IdempotentRecordStore idempotentRecordRepository(
            JdbcTemplate jdbcTemplate,
            @Qualifier(BEAN_JDBC_IDEMPOTENT_RECORD_SERVICE) JdbcIdempotentRecordService jdbcIdempotentRecordService,
            @Qualifier(IdempotentConstant.BEAN_NAME_IDEMPOTENT_PROPERTIES) IdempotentProperties idempotentProperties
    ){
        IdempotentRecordStore idempotentRecordRepository = new JdbcIdempotentRecordStore(
                jdbcTemplate,
                jdbcIdempotentRecordService,
                "idempotent_record"
        );
        return idempotentRecordRepository;
    }


}
