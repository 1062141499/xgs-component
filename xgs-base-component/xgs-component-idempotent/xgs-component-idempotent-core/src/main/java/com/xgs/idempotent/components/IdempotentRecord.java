package com.xgs.idempotent.components;


import lombok.Data;


/**
 * 幂等记录
 * @author xiongguoshuang
 */
@Data
public class IdempotentRecord {

    /**
     * key唯一
     */
    private String key;

    /**
     * 请求参数类型列表
     */
    private Object[] parameterTypes;

    /**
     * 请求参数值列表
     */
    private Object[] parameterValues;

    /**
     * 返回的结果
     */
    private Object result;

    /**
     * 返回的异常
     */
    private Throwable throwable;

    /**
     * 执行失败次数
     */
    private int failCount = 0;

    /**
     * 执行成功次数
     */
    private int successCount = 0;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 过期时间：创建时间+TTL
     */
    private Long expireTime;
}