package com.xgs.idempotent.redis.config;

import javax.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.validation.annotation.Validated;


/**
 * @description:  幂等redis单节点配置
 * @author: xiongguoshuang
 * @date: 2022/04/26
 */
@Data
@Validated
public class IdempotentRedisRedissonSingleServerProperties {

    /**
     * 单节点配置，形如 redis://127.0.0.1:6379
     * @return
     */
    @NotBlank(message = "address不能为空")
    private String address;

    /**
     * 单节点密码
     */
    private String password;
}
