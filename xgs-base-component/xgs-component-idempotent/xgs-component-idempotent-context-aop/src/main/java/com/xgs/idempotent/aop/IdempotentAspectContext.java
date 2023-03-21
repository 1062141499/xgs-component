package com.xgs.idempotent.aop;

import com.xgs.idempotent.components.IdempotentRecordStore;
import com.xgs.idempotent.components.IdempotentRequestContext;
import com.xgs.idempotent.inner.IdempotentInterfaceLevelConfig;
import com.xgs.idempotent.inner.pojo.IdempotentProcessResult;
import java.util.UUID;
import lombok.Getter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * 幂等事件上下文实现类
 * @author xiongguoshuang
 */
@Getter
public class IdempotentAspectContext implements IdempotentRequestContext {

    private final ProceedingJoinPoint joinPoint;
    private final MethodSignature signature;
    private final Object[] params;

    private final String lockClientId;

    /**
     * 当前时间
     */
    private final long currentTimeMillis;

    /**
     * 方法级别配置
     */
    private IdempotentInterfaceLevelConfig idempotentInterfaceLevelConfig;

    /**
     * event事件存取
     */
    private IdempotentRecordStore recordStore;


    public IdempotentAspectContext(ProceedingJoinPoint joinPoint){
        this.joinPoint = joinPoint;

        Signature signature = joinPoint.getSignature();
        this.signature = (MethodSignature) signature;
        this.params = joinPoint.getArgs();
        this.lockClientId = UUID.randomUUID().toString();
        this.currentTimeMillis = System.currentTimeMillis();
    }

    @Override
    public IdempotentProcessResult normalProcess(){
        IdempotentProcessResult normalProcessResult = new IdempotentProcessResult();
        try{
            Object object = joinPoint.proceed();
            normalProcessResult.setResult(object);
        }catch (Throwable throwable){
            normalProcessResult.setThrowable(throwable);
        }
        return normalProcessResult;
    }

    @Override
    public IdempotentInterfaceLevelConfig levelConfig() {
        return this.idempotentInterfaceLevelConfig;
    }

    @Override
    public void setRecordStore(IdempotentRecordStore recordStore) {
        this.recordStore = recordStore;
    }


    public void setIdempotentInterfaceLevelConfig(IdempotentInterfaceLevelConfig idempotentInterfaceLevelConfig){
        this.idempotentInterfaceLevelConfig = idempotentInterfaceLevelConfig;
    }


}
