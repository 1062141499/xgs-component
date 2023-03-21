package com.xgs.idempotent.config;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;


/**
 * @description:  限流全属配置
 * @author: xiongguoshuang
 * @date: 2023/01/26
 */
@Data
@Validated
public class IdempotentProperties {
    /**
     * 幂等应用名
     */
    @NotBlank
    private String appName;

    /**
     * 幂等提示语
     */
    @NotBlank
    private String errorTipMsg;

    /**
     * 幂等功能开关
     *    true   ---  打开
     *    false  ---  关闭
     */
    private boolean idempotentSwitch = true;


    /**
     * 幂等记录ttl时间(秒)
     */
    private Long recordTtlSecs;


    /**
     * 分布式锁ttl(秒)
     */
    private Long distributedLockTtlSecs;

    /**
     * 尝试获取超时时间（毫秒）
     */
    private Long tryDistributedLockTimeOutMillis;

    /**
     * 全局重试规则类名
     */
    private String globalRetryRuleClass;

    /**
     * 全局重试规则bean名称
     */
    private String globalRetryRuleBeanName;


    /**
     * 全局key获取器的类名
     */
    private String globalKeyGetterClass;

    /**
     * 全局key获取器的bean名称
     */
    private String globalKeyGetterBeanName;


    /**
     * 全局命中策略
     */
    private String globalHitPolicy;

}
