package com.xgs.idempotent.inner;

import com.xgs.idempotent.components.IdempotentHitPolicyEnum;
import com.xgs.idempotent.components.IdempotentKeyGetter;
import com.xgs.idempotent.components.TryAgainRule;
import lombok.Data;

/**
 * 幂等接口级配置
 * @author xiongguoshuang
 */
@Data
public class IdempotentInterfaceLevelConfig {


    /**
     * 幂等key获取策略
     */
    private IdempotentKeyGetter keyGetter;

    /**
     * 重试规则
     * @return
     */
    private TryAgainRule retryRule;


    /**
     *  幂等命中策略
     * @return
     */
    private IdempotentHitPolicyEnum hitPolicy;



    /**
     * 幂等记录ttl
     * @return
     */
    private Long recordTtlSecs;
}
