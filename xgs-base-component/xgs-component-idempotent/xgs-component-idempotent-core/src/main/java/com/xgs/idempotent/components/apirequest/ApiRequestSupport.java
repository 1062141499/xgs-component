package com.xgs.idempotent.components.apirequest;


import com.xgs.idempotent.components.web.ApiRequest;
import com.xgs.idempotent.components.IdempotentRequestContext;
/**
 * 请求封装类
 * @author xiongguoshuang
 */
public interface ApiRequestSupport {

    default ApiRequest parseApiRequestFromParams(IdempotentRequestContext requestContext){
        Object[] params = requestContext.getParams();
        if(params == null){
            return null;
        }
        for (Object param : params) {
            if(param instanceof ApiRequest){
                return (ApiRequest) param;
            }
        }
        return null;
    }


    default Object parseApiRequestDataFromParams(IdempotentRequestContext requestContext){
        ApiRequest apiRequest = this.parseApiRequestFromParams(requestContext);
        return apiRequest == null ? null : apiRequest.getData();
    }
}
