package com.xgs.idempotent.components.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 通用请求参数
 *
 * @author xiongguoshuang
 * @date 2021/06/10 17:02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiRequest<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    @Valid
    @NotNull(message = "请求数据不能为空")
    private T data;


    public static <T> ApiRequest<T> wrapperData(T data){
        ApiRequest<T> apiRequest = new ApiRequest<T>();
        apiRequest.setData(data);
        return apiRequest;
    }
}
