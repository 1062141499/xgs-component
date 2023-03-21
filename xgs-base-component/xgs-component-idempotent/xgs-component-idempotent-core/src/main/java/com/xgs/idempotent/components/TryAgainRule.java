package com.xgs.idempotent.components;

/**
 * 再执行规则
 * @author xiongguoshuang
 */
public interface TryAgainRule {

    /**
     * 是否需要重试
     * @param idempotentRequestContext 请求上下文
     * @param idempotentRecord 幂等记录
     * @return
     *    true  - 需要
     *    false - 不需要
     */
    boolean needTryAgain(IdempotentRequestContext idempotentRequestContext, IdempotentRecord idempotentRecord);
}
