package com.xgs.idempotent.components.apirequest;

import com.xgs.idempotent.components.IdempotentKeyGetter;
import com.xgs.idempotent.components.IdempotentRequestContext;
import com.xgs.idempotent.constants.IdempotentErrorCodeEnum;
import com.xgs.idempotent.exception.IdempotentException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import org.springframework.beans.BeanUtils;

/**
 * orderId 幂等键获取器
 * 获取apiRequest.getData 的orderId
 * @author xiongguoshuang
 */
public class OrderIdKeyGetter implements IdempotentKeyGetter, ApiRequestSupport{
    @Override
    public String parseRecordKey(IdempotentRequestContext requestContext) {

        Object data = this.parseApiRequestDataFromParams(requestContext);
        if(data == null){
            return null;
        }

        PropertyDescriptor propertyDescriptor = BeanUtils.getPropertyDescriptor(data.getClass(), "orderId");
        try {
            return propertyDescriptor.getReadMethod().invoke(data).toString();
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw IdempotentException.wrapThrowableWithArguments(
                    IdempotentErrorCodeEnum.PARAM_ERROR.getCode(),
                    "解析recordKey失败",
                    exception,
                    data
            );
        }
    }
}
