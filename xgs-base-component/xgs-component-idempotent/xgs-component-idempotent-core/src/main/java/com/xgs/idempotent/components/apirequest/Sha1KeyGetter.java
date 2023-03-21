package com.xgs.idempotent.components.apirequest;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.xgs.idempotent.components.IdempotentKeyGetter;
import com.xgs.idempotent.components.IdempotentRequestContext;

/**
 * sha1 幂等键获取器
 * 将ApiRequest的data整体做sha1
 * @author xiongguoshuang
 */
public class Sha1KeyGetter implements IdempotentKeyGetter, ApiRequestSupport {
    @Override
    public String parseRecordKey(IdempotentRequestContext requestContext) {

        Object data = this.parseApiRequestDataFromParams(requestContext);
        if(data == null){
            return null;
        }
        byte[] serialize = ObjectUtil.serialize(data);
        return DigestUtil.sha1Hex(serialize).toLowerCase();
    }
}
