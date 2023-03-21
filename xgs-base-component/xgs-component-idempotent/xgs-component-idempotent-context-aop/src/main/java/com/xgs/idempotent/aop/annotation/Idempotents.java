package com.xgs.idempotent.aop.annotation;

import java.lang.annotation.ElementType;
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
public @interface Idempotents {

    Idempotent[] value();
}
