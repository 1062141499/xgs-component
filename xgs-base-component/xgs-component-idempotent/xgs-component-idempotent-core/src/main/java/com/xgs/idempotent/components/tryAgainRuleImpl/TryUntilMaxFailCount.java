package com.xgs.idempotent.components.tryAgainRuleImpl;

import com.xgs.idempotent.components.IdempotentRecord;
import com.xgs.idempotent.components.IdempotentRequestContext;
import com.xgs.idempotent.components.TryAgainRule;
import com.xgs.idempotent.constants.IdempotentErrorCodeEnum;
import com.xgs.idempotent.exception.IdempotentException;

/**
 * 达到最大失败次后不再重试
 * @author xiongguoshuang
 */
public class TryUntilMaxFailCount implements TryAgainRule {
    private final int maxFailCount;

    public TryUntilMaxFailCount(int maxFailCount){
        if(maxFailCount <=0){
            throw IdempotentException.error(
                    IdempotentErrorCodeEnum.WRAP_OTHER_UNKNOWN_ERROR.getCode(),
                    "maxFailCount 必需大于0， maxFailCount= " + maxFailCount
            );
        }
        this.maxFailCount = maxFailCount;
    }

    @Override
    public boolean needTryAgain(IdempotentRequestContext idempotentRequestContext, IdempotentRecord idempotentRecord) {
        return idempotentRecord.getFailCount() < this.maxFailCount;
    }
}
