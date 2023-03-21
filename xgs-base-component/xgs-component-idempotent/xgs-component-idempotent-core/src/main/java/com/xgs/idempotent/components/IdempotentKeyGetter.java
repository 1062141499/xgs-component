package com.xgs.idempotent.components;


/**
 * 幂等键获取器
 * @author xiongguoshuang
 */
public interface IdempotentKeyGetter {

    /**
     *
     * 获取幂等记录key
     * @param requestContext 幂等请求上下文
     * @return
     */
    String parseRecordKey(IdempotentRequestContext requestContext);
}
