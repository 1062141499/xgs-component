package com.xgs.idempotent.inner.utils;

import com.xgs.idempotent.components.IdempotentHitPolicyEnum;
import com.xgs.idempotent.components.IdempotentRecord;
import com.xgs.idempotent.components.IdempotentRequestContext;
import com.xgs.idempotent.constants.IdempotentErrorCodeEnum;
import com.xgs.idempotent.exception.IdempotentException;
import com.xgs.idempotent.inner.pojo.IdempotentProcessResult;

/**
 * 命中处理工具类
 * @author xiongguoshuang
 */
public final class IdempotentHitPolicyEnumUtils {

    private IdempotentHitPolicyEnumUtils(){

    }

    public static IdempotentProcessResult handle(IdempotentHitPolicyEnum idempotentHitPolicyEnum,
                                                 IdempotentRecord idempotentRecord, IdempotentRequestContext idempotentRequestContext
                       ) {
        IdempotentHitPolicy idempotentHitPolicy = null;
        if(idempotentHitPolicyEnum == IdempotentHitPolicyEnum.ReturnLastResultPolicy){
            idempotentHitPolicy = IdempotentHitPolicy.ReturnLastResultPolicy;
        }else if(idempotentHitPolicyEnum == IdempotentHitPolicyEnum.ReturnLastResultAndWrapperExceptionPolicy){
            idempotentHitPolicy = IdempotentHitPolicy.ReturnLastResultAndWrapperExceptionPolicy;
        }else if(idempotentHitPolicyEnum == IdempotentHitPolicyEnum.ThrowIdemException){
            idempotentHitPolicy = IdempotentHitPolicy.ThrowExceptionPolicy;
        }else{
            throw IdempotentException.error(IdempotentErrorCodeEnum.HIT_UNKNOWN_HIT_POLICY);
        }
        return idempotentHitPolicy.handle(idempotentRecord,idempotentRequestContext);

    }
}
