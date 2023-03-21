package com.xgs.idempotent.components.tryAgainRuleImpl;

import com.xgs.idempotent.components.IdempotentRecord;
import com.xgs.idempotent.components.IdempotentRequestContext;
import com.xgs.idempotent.components.TryAgainRule;
import com.xgs.idempotent.constants.IdempotentErrorCodeEnum;
import com.xgs.idempotent.exception.IdempotentException;

/**
 * 达到最大次后不再重试
 * @author xiongguoshuang
 */
public class TryUntilMaxCount implements TryAgainRule {
    private final int maxCount;

    public TryUntilMaxCount(int maxCount){
        if(maxCount <=0){
            throw IdempotentException.error(
                    IdempotentErrorCodeEnum.WRAP_OTHER_UNKNOWN_ERROR.getCode(),
                    "maxFailCount 必需大于0， maxFailCount= " + maxCount
            );
        }
        this.maxCount = maxCount;
    }

    @Override
    public boolean needTryAgain(IdempotentRequestContext idempotentRequestContext, IdempotentRecord idempotentRecord) {
        int totalCount = idempotentRecord.getFailCount() + idempotentRecord.getSuccessCount();
        return totalCount < this.maxCount;
    }
}
