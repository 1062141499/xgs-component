package com.xgs.idempotent.jdbc;

import com.xgs.idempotent.components.IdempotentRequestContext;
import com.xgs.idempotent.inner.utils.DefaultConfigUtils;
import com.xgs.idempotent.inner.utils.GlobalConfigUtils;
import org.springframework.util.Assert;

/**
 * 锁相关工具类
 * @author xiongguoshuang
 */
public class LockTtlSecsUtils {

    /**
     * 计算分布式锁的ttl秒数
     * @param idempotentRequestContext
     * @return
     */
    public static long calDistributedLockTtlSecs(IdempotentRequestContext idempotentRequestContext){
        Long distributedLockTtlSecs = GlobalConfigUtils.getInstance().getDistributedLockTtlSecs();
        if(distributedLockTtlSecs == null || distributedLockTtlSecs<=0){
            distributedLockTtlSecs = DefaultConfigUtils.getInstance().getDistributedLockTtlSecs();
        }

        Assert.notNull(distributedLockTtlSecs,"程序启动时需要检查distributedLockTtlSecs配置正确");
        return distributedLockTtlSecs;
    }

    /**
     * 计算分布式锁的ttl秒数
     * @param idempotentRequestContext
     * @return
     */
    public static long calTryDistributedLockTimeOutMills(IdempotentRequestContext idempotentRequestContext){
        Long tryDistributedLockTimeOutMillis = GlobalConfigUtils.getInstance().getTryDistributedLockTimeOutMillis();
        if(tryDistributedLockTimeOutMillis == null || tryDistributedLockTimeOutMillis<=0){
            tryDistributedLockTimeOutMillis = DefaultConfigUtils.getInstance().getTryDistributedLockTimeOutMillis();
        }
        Assert.notNull(tryDistributedLockTimeOutMillis,"程序启动时需要检查tryDistributedLockTimeOutMillis配置正确");
        return tryDistributedLockTimeOutMillis;
    }
}
