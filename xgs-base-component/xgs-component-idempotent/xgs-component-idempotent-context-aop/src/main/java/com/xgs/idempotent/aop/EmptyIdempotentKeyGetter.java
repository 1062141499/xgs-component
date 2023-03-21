package com.xgs.idempotent.aop;

import com.xgs.idempotent.components.IdempotentKeyGetter;
import com.xgs.idempotent.components.IdempotentRequestContext;

/**
 * @description: key值获取器
 * @author: xiongguoshuang
 * @date: 2023/02/19
 */
public class EmptyIdempotentKeyGetter implements IdempotentKeyGetter {

    @Override
    public String parseRecordKey(IdempotentRequestContext requestContext) {
        throw new UnsupportedOperationException("必需自已实现");
    }
}
