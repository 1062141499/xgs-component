package com.xgs.idempotent.inner.utils;

import com.xgs.idempotent.components.IdempotentRecord;
import com.xgs.idempotent.components.IdempotentRequestContext;
import com.xgs.idempotent.constants.IdempotentErrorCodeEnum;
import com.xgs.idempotent.exception.IdempotentException;
import com.xgs.idempotent.inner.pojo.IdempotentProcessResult;

/**
 * 幂等命中处理器
 * @author xiongguoshuang
 */
interface IdempotentHitPolicy {
    /**
     * 幂等命中处理
     * @param idempotentRecord
     * @param idempotentRequestContext
     * @return
     * @throws Throwable
     */
    IdempotentProcessResult handle(IdempotentRecord idempotentRecord, IdempotentRequestContext idempotentRequestContext);




    /**
     * 直接返回上次的结果
     */
    IdempotentHitPolicy ReturnLastResultPolicy = (idempotentRecord, idempotentRequestContext) -> {
        IdempotentProcessResult
                normalProcessResult = new IdempotentProcessResult();
        if(idempotentRecord.getThrowable() != null){
            normalProcessResult.setThrowable(idempotentRecord.getThrowable());
        }else{
            normalProcessResult.setResult(idempotentRecord.getResult());
        }
        return normalProcessResult;
    };


    IdempotentHitPolicy ReturnLastResultAndWrapperExceptionPolicy = (idempotentRecord, idempotentRequestContext) -> {
        IdempotentProcessResult
                normalProcessResult = new IdempotentProcessResult();
        if(idempotentRecord.getThrowable() != null){
            Throwable throwable = idempotentRecord.getThrowable();
            normalProcessResult.setThrowable(IdempotentException.wrapThrowable(throwable));
        }else{
            normalProcessResult.setResult(idempotentRecord.getResult());
        }
        return normalProcessResult;
    };

    /**
     * 中止
     */
    IdempotentHitPolicy ThrowExceptionPolicy  = (idempotentRecord, idempotentRequestContext) -> {
        IdempotentProcessResult
                idempotentProcessResult = new IdempotentProcessResult();
        idempotentProcessResult.setThrowable(
                IdempotentException.errorWithArguments(IdempotentErrorCodeEnum.HIT_ABORT,idempotentRecord)
        );
        return idempotentProcessResult;
    };
}
