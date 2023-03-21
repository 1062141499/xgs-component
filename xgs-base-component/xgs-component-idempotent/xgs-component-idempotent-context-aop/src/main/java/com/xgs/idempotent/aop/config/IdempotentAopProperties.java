package com.xgs.idempotent.aop.config;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

/**
 * 限流全属配置
 * @author xiongguoshuang
 * @date 2023-03-11
 */
@Data
@Validated
public class IdempotentAopProperties {

    /**
     * filter顺序
     */
    private int filterOrder = -500;

}
