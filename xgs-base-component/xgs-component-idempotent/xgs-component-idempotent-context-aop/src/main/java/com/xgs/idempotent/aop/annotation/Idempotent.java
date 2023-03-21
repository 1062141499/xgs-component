package com.xgs.idempotent.aop.annotation;

import com.xgs.idempotent.aop.EmptyIdempotentKeyGetter;
import com.xgs.idempotent.aop.EmptyTryAgainRule;
import com.xgs.idempotent.components.IdempotentHitPolicyEnum;
import com.xgs.idempotent.components.IdempotentKeyGetter;
import com.xgs.idempotent.components.TryAgainRule;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 幂等服务注解
 * @author xiongguoshuang
 * @date 2023-03-11
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Repeatable(Idempotents.class)   // 加上 @Repeatable 注解使该注解可以叠加使用
public @interface Idempotent {

    /**
     * 幂等模块名，在同一个【幂等应用名】下唯一，可以不填，不填则从接口名中获取
     * @return
     */
    String moduleName() default "";

    /**
     * 幂等记录key获取策略
     * 注：要求keyGetterClass必需有无参数构造方法
     */
    Class<? extends IdempotentKeyGetter > keyGetterClass() default EmptyIdempotentKeyGetter.class;

    /**
     * 幂等记录key获取策略实例bean名称
     */
    String keyGetterBeanName() default "";


    /**
     * 再试规则
     * @return
     */
    Class<? extends TryAgainRule > retryRuleClass() default EmptyTryAgainRule.class;

    /**
     * 再试规则bean命称
     * @return
     */
    String retryRuleBeanName() default "";

    /**
     * 命中处理策略
     * @return
     */
    IdempotentHitPolicyEnum hitPolicy() default IdempotentHitPolicyEnum.ReturnLastResultPolicy;

    /**
     * 幂等记录ttl
     * -1则取全局配置的默认值
     * @return
     */
    long recordTtlSecs() default -1;

}
