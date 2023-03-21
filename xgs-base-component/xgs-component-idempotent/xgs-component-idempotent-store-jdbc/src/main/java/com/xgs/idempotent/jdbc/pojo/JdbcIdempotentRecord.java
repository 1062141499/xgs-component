package com.xgs.idempotent.jdbc.pojo;

import lombok.Data;

import java.util.Date;

/**
 * @author xiongguoshuang
 */
@Data
public class JdbcIdempotentRecord {
    public JdbcIdempotentRecord(){

    }

    /**
     * 数据库自增主键
     */
    private Long id;

    /**
     * 记录key
     */
    private String key;

    /**
     * 执行成功次数
     */
    private int successCount;

    /**
     * 执行失败次数
     */
    private int failCount = 0;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 参数值列表
     */
    private byte[] parameterValues;

    /**
     * 参数类型列表
     */
    private String parameterTypes;

    /**
     * 结果
     */
    private byte[] result;

    /**
     * 异常对象
     */
    private byte[] throwable;

    /**
     * 当前锁id
     */
    private String lockId;

    /**
     * 锁过期时间
     */
    private Long lockExpiredMillis;
}
