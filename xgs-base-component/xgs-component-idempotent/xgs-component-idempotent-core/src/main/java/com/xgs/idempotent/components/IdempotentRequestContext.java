package com.xgs.idempotent.components;

import com.xgs.idempotent.inner.IdempotentInterfaceLevelConfig;
import com.xgs.idempotent.inner.pojo.IdempotentProcessResult;

/**
 * 幂等请求上下文
 * @author xiongguoshuang
 */
public interface IdempotentRequestContext {

    /**
     * 正常业务处理
     * @return
     * @throws Throwable
     */
    IdempotentProcessResult normalProcess();

    /**
     * 获取请求参数
     * @return
     */
    Object[] getParams();

    /**
     * 锁的客户端标识
     */
    String getLockClientId();

    /**
     * 当前时间
     * @return
     */
    long getCurrentTimeMillis();

    /**
     * 方法级别配置
     */
    IdempotentInterfaceLevelConfig levelConfig();


    /**
     * 获取event事件存取
     * @return
     */
    IdempotentRecordStore getRecordStore();

    /**
     * 设置event事件存取
     * @param recordStore
     */
    void setRecordStore(IdempotentRecordStore recordStore);
}
