package com.xgs.idempotent.components;


import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 事件存储，下面所有接口都比需保证是原子的
 * @author xiongguoshuang
 *
 * recordId = moduleName + recordKey
 */
public interface IdempotentRecordStore {

    /**
     * 加锁
     * @param lockId 客户端持有锁id（不可为空）
     * @param recordId moduleName + recordKey
     */
    boolean tryLock(@NotBlank String lockId, @NotBlank String recordId, IdempotentRequestContext idempotentRequestContext);

    /**
     * 释放锁
     * @param lockId 客户端持有锁id（不可为空）
     * @param recordId moduleName + recordKey
     */
    boolean releaseLock(@NotBlank String lockId, @NotBlank String recordId, IdempotentRequestContext idempotentRequestContext);

    /**
     * 创建空记录
     * @param lockId 客户端持有锁id（不可为空）
     * @param recordId moduleName + recordKey
     * @return
     */
    IdempotentRecord emptyRecord(@NotBlank String lockId, @NotBlank String recordId, IdempotentRequestContext idempotentRequestContext);


    /**
     * 更新记录
     * @param lockId 客户端持有锁id（不可为空）
     * @param oldRecord  原值(可空)
     * @param newRecord  新值
     * @return
     */
    boolean updateRecord(@NotBlank String lockId, IdempotentRecord oldRecord, @NotNull IdempotentRecord newRecord, IdempotentRequestContext idempotentRequestContext);


    /**
     * 根据recordKey获取记录
     * @param recordId moduleName + recordKey
     * @return
     */
    IdempotentRecord queryRecordByKey(@NotBlank String recordId, IdempotentRequestContext idempotentRequestContext);

    /**
     * 删除过期记录
     */
    boolean deleteExpiredIdempotentRecord(@NotBlank String lockId, @NotNull IdempotentRecord oldRecord, IdempotentRequestContext idempotentRequestContext);
}
