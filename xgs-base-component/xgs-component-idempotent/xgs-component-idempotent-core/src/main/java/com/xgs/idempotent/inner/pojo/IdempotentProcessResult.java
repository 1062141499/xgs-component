package com.xgs.idempotent.inner.pojo;

import lombok.Data;

/**
 * 执行在业务逻辑的结果
 * @author xiongguoshuang
 */
@Data
public class IdempotentProcessResult {
    /**
     * 正常结果
     */
    Object result;
    /**
     * 异常
     */
    Throwable throwable;
}
