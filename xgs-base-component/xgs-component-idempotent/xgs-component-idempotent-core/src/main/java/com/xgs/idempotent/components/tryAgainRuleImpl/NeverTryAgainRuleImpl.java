package com.xgs.idempotent.components.tryAgainRuleImpl;

import com.xgs.idempotent.components.IdempotentRecord;
import com.xgs.idempotent.components.IdempotentRequestContext;
import com.xgs.idempotent.components.TryAgainRule;

/**
 * 总是不再试
 * @author xiongguoshuang
 */
public class NeverTryAgainRuleImpl implements TryAgainRule {

    @Override
    public boolean needTryAgain(IdempotentRequestContext idempotentRequestContext, IdempotentRecord idempotentRecord) {
        return false;
    }
}
