package com.xgs.idempotent.aop;

import com.xgs.idempotent.components.IdempotentRecord;
import com.xgs.idempotent.components.IdempotentRequestContext;
import com.xgs.idempotent.components.TryAgainRule;
/**
 * @description: 重试规则
 * @author: xiongguoshuang
 * @date: 2023/02/19
 */
public class EmptyTryAgainRule  implements TryAgainRule {
    @Override
    public boolean needTryAgain(IdempotentRequestContext idempotentRequestContext, IdempotentRecord idempotentRecord) {
        throw new UnsupportedOperationException("必需自已实现");
    }
}
