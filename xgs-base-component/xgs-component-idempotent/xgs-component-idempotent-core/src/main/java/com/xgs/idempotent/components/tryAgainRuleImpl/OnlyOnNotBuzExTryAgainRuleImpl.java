package com.xgs.idempotent.components.tryAgainRuleImpl;


import com.xgs.idempotent.components.IdempotentRecord;
import com.xgs.idempotent.components.IdempotentRequestContext;
import com.xgs.idempotent.components.TryAgainRule;
import com.xgs.idempotent.exception.BusinessException;

/**
 * 仅在前一次请求抛出非BusinessException异常的情况下再试
 * @author xiongguoshuang
 */
public class OnlyOnNotBuzExTryAgainRuleImpl implements TryAgainRule {

    @Override
    public boolean needTryAgain(IdempotentRequestContext idempotentRequestContext, IdempotentRecord idempotentRecord) {
        Throwable throwable = idempotentRecord.getThrowable();
        return throwable!=null && !(throwable instanceof BusinessException);
    }
}
