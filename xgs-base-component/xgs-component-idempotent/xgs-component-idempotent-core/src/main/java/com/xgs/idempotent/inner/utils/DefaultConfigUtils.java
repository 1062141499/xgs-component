package com.xgs.idempotent.inner.utils;

import com.xgs.idempotent.components.IdempotentHitPolicyEnum;
import com.xgs.idempotent.components.TryAgainRule;
import lombok.Data;

/**
 * 默认配置工具类
 * @author xiongguoshuang
 */
@Data
public class DefaultConfigUtils {

    private DefaultConfigUtils(){

    }

    /**
     * 静态类方法实现单例，懒汉模式，线程安全
     */
    private static class InstanceHolder{
        static DefaultConfigUtils inst = new DefaultConfigUtils();
    }

    public static DefaultConfigUtils getInstance(){
        return InstanceHolder.inst;
    }


    /**
     * 再试规则
     * @return
     */
    private TryAgainRule retryRule;


    /**
     * 幂等记录ttl时间(秒)
     */
    private Long recordTtlSecs;

    /**
     * 分布式锁ttl(秒)
     */
    private Long distributedLockTtlSecs;


    /**
     * 尝试获取超时时间（毫秒）
     */
    private Long tryDistributedLockTimeOutMillis;

    /**
     *  幂等命中策略
     * @return
     */
    private IdempotentHitPolicyEnum hitPolicy;
}
