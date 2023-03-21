package com.xgs.idempotent.redis.pojo;

import lombok.Data;

/**
 * https://github.com/giulio/rojo
 */
@Data
public class RedisIdempotentRecord {

    /**
     * key唯一
     */
    private String key;

    /**
     * 参数值列表
     */
    private Object[] parameterValues;

    /**
     * 参数类型列表
     */
    private String parameterTypes;

    /**
     * 业务逻辑
     */
    private Object result;

    /**
     * 返回的异常
     */
    private Throwable throwable;

    /**
     * 执行成功次数
     */
    private int successCount = 0;

    /**
     * 已重试次数
     */
    private int failCount = 0;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;
}
