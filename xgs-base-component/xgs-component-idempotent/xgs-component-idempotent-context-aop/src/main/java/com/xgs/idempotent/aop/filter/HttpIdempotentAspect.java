package com.xgs.idempotent.aop.filter;

import com.xgs.idempotent.aop.EmptyIdempotentKeyGetter;
import com.xgs.idempotent.aop.EmptyTryAgainRule;
import com.xgs.idempotent.aop.IdempotentAspectContext;
import com.xgs.idempotent.aop.annotation.Idempotent;
import com.xgs.idempotent.aop.config.IdempotentAopProperties;
import com.xgs.idempotent.components.IdempotentHitPolicyEnum;
import com.xgs.idempotent.components.IdempotentKeyGetter;
import com.xgs.idempotent.components.TryAgainRule;
import com.xgs.idempotent.constants.IdempotentErrorCodeEnum;
import com.xgs.idempotent.exception.IdempotentException;
import com.xgs.idempotent.inner.IdempotentInterfaceLevelConfig;
import com.xgs.idempotent.aop.IdempotentAnnotationMetaInitializer;
import com.xgs.idempotent.inner.utils.GlobalConfigUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.Ordered;


/**
 * @description: HTTP 限流
 * @author: xiongguoshuang
 * @date: 2023/01/19
 */
@Aspect
@Slf4j
public class HttpIdempotentAspect implements Ordered {

    private IdempotentAnnotationMetaInitializer idempotentAnnotationMetaInitializer;

    private final int filterOrder;

    public HttpIdempotentAspect(IdempotentAopProperties idempotentAopProperties,
                                IdempotentAnnotationMetaInitializer idempotentAnnotationMetaInitializer){
        this.idempotentAnnotationMetaInitializer = idempotentAnnotationMetaInitializer;
        this.filterOrder = idempotentAopProperties.getFilterOrder();
    }

    /**
     * ..表示包及子包 该方法代表controller层的所有方法
     */
    @Pointcut("@annotation(com.xgs.idempotent.aop.annotation.Idempotent)")
    public void idempotentMethod() {
    }


    /**
     * 方法执行前
     *
     * @param joinPoint
     * @throws Exception
     */
    @Around("idempotentMethod()")
    public Object processIdempotent(ProceedingJoinPoint joinPoint) throws Throwable {
        if(GlobalConfigUtils.getInstance().isIdempotentSwitch()){
            String idempotentMapKey = joinPoint.getSignature().getDeclaringTypeName()+ "." + joinPoint.getSignature().getName();
            Idempotent idempotent = idempotentAnnotationMetaInitializer.getIdempotentMetaByImplMethodName(idempotentMapKey);
            if(idempotent != null){
                return processIdempotent(idempotent, joinPoint);
            }
        }
        return joinPoint.proceed();
    }


    private Object processIdempotent(Idempotent idempotent, ProceedingJoinPoint joinPoint) throws Throwable {

        IdempotentAspectContext idempotentEventContext = new IdempotentAspectContext(joinPoint);
        idempotentEventContext.setRecordStore(this.idempotentAnnotationMetaInitializer.getIdempotentRecordStore());
        String moduleName = this.parseModuleName(idempotent, joinPoint);
        IdempotentAopRequestProcessor idempotentProcessor = IdempotentAopProcessorCacheUtils.getByModuleName(moduleName);
        if(idempotentProcessor == null){
            IdempotentInterfaceLevelConfig idempotentAnnotationConfiguration =
                    this.genIdempotentAnnotationConfiguration(idempotent);
            idempotentProcessor = new IdempotentAopRequestProcessor(idempotentAnnotationConfiguration, moduleName);
            IdempotentAopProcessorCacheUtils.putModuleName(moduleName, idempotentProcessor);
        }

        return idempotentProcessor.process(idempotentEventContext);
    }

    /**
     * 解析模块名
     * @param idempotent
     * @param joinPoint
     * @return
     */
    private String parseModuleName(Idempotent idempotent, ProceedingJoinPoint joinPoint){
        if(StringUtils.isNotBlank(idempotent.moduleName())){
            return idempotent.moduleName();
        }else{
            return joinPoint.getSignature().toLongString();
        }
    }






    /**
     * 生成接口级的配置
     * @param idempotent
     * @return
     */
    private IdempotentInterfaceLevelConfig genIdempotentAnnotationConfiguration(Idempotent idempotent){

        IdempotentInterfaceLevelConfig idempotentInterfaceConfig = new IdempotentInterfaceLevelConfig();

        Class<? extends IdempotentKeyGetter> keyGetterClass = idempotent.keyGetterClass();
        if(keyGetterClass == EmptyIdempotentKeyGetter.class){
            keyGetterClass = null;
        }

        /**
         * 幂等键值获取器
         */
        IdempotentKeyGetter idempotentKeyGetter =
                idempotentAnnotationMetaInitializer.getInstanceByBeanNameOrReflect(keyGetterClass, idempotent.keyGetterBeanName(), IdempotentKeyGetter.class);
        if(idempotentKeyGetter == null){
            //通过反射初始化失败
            throw IdempotentException.errorWithArguments(
                    IdempotentErrorCodeEnum.WRAP_OTHER_UNKNOWN_ERROR.getCode(),
                    "idempotentKeyGetter不能为空" ,
                    keyGetterClass, idempotent.keyGetterBeanName(), IdempotentKeyGetter.class
            );
        }
        idempotentInterfaceConfig.setKeyGetter(idempotentKeyGetter);

        Class<? extends TryAgainRule> retryRuleClass = idempotent.retryRuleClass();
        if(retryRuleClass == EmptyTryAgainRule.class){
            retryRuleClass = null;
        }

        /**
         * 再试策略
         */
        TryAgainRule tryAgainRule =
                idempotentAnnotationMetaInitializer.getInstanceByBeanNameOrReflect(retryRuleClass, idempotent.retryRuleBeanName(),
                        TryAgainRule.class);
        idempotentInterfaceConfig.setRetryRule(tryAgainRule);


        /**
         * 命中处理策略
         */
        IdempotentHitPolicyEnum hitPolicy = idempotent.hitPolicy();
        idempotentInterfaceConfig.setHitPolicy(hitPolicy);

        if(idempotent.recordTtlSecs() >0L){
            idempotentInterfaceConfig.setRecordTtlSecs(idempotent.recordTtlSecs());
        }
        return idempotentInterfaceConfig;
    }


    @Override
    public int getOrder() {
        return filterOrder;
    }
}
