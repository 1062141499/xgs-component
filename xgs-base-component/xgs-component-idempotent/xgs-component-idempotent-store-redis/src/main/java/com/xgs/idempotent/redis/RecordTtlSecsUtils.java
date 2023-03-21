package com.xgs.idempotent.redis;

import com.xgs.idempotent.components.IdempotentRequestContext;
import com.xgs.idempotent.inner.IdempotentInterfaceLevelConfig;
import com.xgs.idempotent.inner.utils.DefaultConfigUtils;
import com.xgs.idempotent.inner.utils.GlobalConfigUtils;

public class RecordTtlSecsUtils {

    /**
     * 计算记录超时秒数
     * @param idempotentRequestContext
     * @return
     */
    public static long calRecordTtlSecs(IdempotentRequestContext idempotentRequestContext){
        IdempotentInterfaceLevelConfig idempotentInterfaceLevelConfig = idempotentRequestContext.levelConfig();
        if(idempotentInterfaceLevelConfig.getRecordTtlSecs() != null){

            return idempotentInterfaceLevelConfig.getRecordTtlSecs();

        }else if(GlobalConfigUtils.getInstance().getRecordTtlSecs() != null){
            return GlobalConfigUtils.getInstance().getRecordTtlSecs();
        }
        return DefaultConfigUtils.getInstance().getRecordTtlSecs();
    }
}
