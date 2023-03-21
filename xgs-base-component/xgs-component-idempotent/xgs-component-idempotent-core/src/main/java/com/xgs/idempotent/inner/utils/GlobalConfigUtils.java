package com.xgs.idempotent.inner.utils;


import com.xgs.idempotent.components.IdempotentHitPolicyEnum;
import com.xgs.idempotent.components.IdempotentKeyGetter;
import com.xgs.idempotent.components.TryAgainRule;
import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 全局级配置工具类
 * @author xiongguoshuang
 */
@Data
public class GlobalConfigUtils {
    private GlobalConfigUtils(){

    }

    /**
     * 静态类方法实现单例，懒汉模式，线程安全
     */
    private static class InstanceHolder{
        static GlobalConfigUtils inst = new GlobalConfigUtils();
    }

    public static GlobalConfigUtils getInstance(){
        return InstanceHolder.inst;
    }


    /**
     * 幂等应用名
     */
    @NotBlank
    private String appName;

    /**
     * 幂等提示语
     */
    @NotBlank
    private String limitMsg;


    /**
     * 幂等key获取策略
     */
    private IdempotentKeyGetter keyGetter;


    /**
     * 再试规则
     * @return
     */
    private TryAgainRule retryRule;


    /**
     *  幂等命中策略
     * @return
     */
    private IdempotentHitPolicyEnum hitPolicy;

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
     * 幂等功能开关
     *    true   ---  打开
     *    false  ---  关闭
     */
    private boolean idempotentSwitch = true;
}
